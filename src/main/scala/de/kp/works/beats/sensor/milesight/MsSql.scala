package de.kp.works.beats.sensor.milesight

/**
 * Copyright (c) 2019 - 2022 Dr. Krusche & Partner PartG. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * @author Stefan Krusche, Dr. Krusche & Partner PartG
 *
 */

import akka.stream.scaladsl.SourceQueueWithComplete
import ch.qos.logback.classic.Logger
import com.google.gson.{JsonArray, JsonObject}
import de.kp.works.beats.sensor.{BeatAttrs, BeatSql}
import de.kp.works.beats.sensor.BeatAttrs.{TIME, VALUE}
import de.kp.works.beats.sensor.{TimeFilter, TimeValueFilter, ValueFilter}
import org.apache.spark.sql.BeatSession

import scala.collection.JavaConversions.iterableAsScalaIterable
/**
 * [MsSql] supports the `Sensor as a Table` concept,
 * that is taken from Osquery.
 */
class MsSql(queue: SourceQueueWithComplete[String], logger:Logger) {
  /**
   * Validating the SQL statement is performed
   * using the Apache Spark SqlParser, which is
   * invoked via a Spark session
   */
  private val session = BeatSession.getSession
  private val beatSql = new BeatSql(session, logger)
  /**
   * At this stage, the RocksDB must be initialized
   * already, therefore no `options` are provided
   */
  private val msRocksApi = MsRocksApi.getInstance
  private val emptyResponse = new JsonArray

  def read(sql:String):String = {
    /*
     * STEP #1: Validate & extract provided SQL
     * statement; [BeatSql] throws an invalid
     * argument exception if something went wrong
     */
    val json = beatSql.parse(sql)
    if (json.isJsonNull) {
      val message = s"Provided SQL statement is not complete."
      logger.warn(message)

      return emptyResponse.toString
    }
    /*
     * STEP #2: Check whether the extracted table,
     * columns and (optional) conditions refer to
     * an existing database specification
     */
    val obj = json.getAsJsonObject

    val table  = obj.get("table").getAsString
    checkTable(table)

    val output = obj.get("output").getAsJsonArray
      .map(_.getAsString).toSeq
    checkOutput(output)

    val condition = obj.get("condition")
    /*
     * STEP #3: Map SQL statement onto RockDB
     * commands
     */
    var response = new JsonArray
    if (condition.isJsonNull) {

      response = scanToJson(table)
      response.toString

    } else {
      /*
       * Reminder: The (filter) condition is a JsonObject
       * and is defined as a tree with left & right nodes
       */
      val expression = condition.getAsJsonObject
      val response = new JsonArray

      val columns = obj.get("columns").getAsJsonArray
        .map(_.getAsString).toSeq

      /*
       * Check whether one or more than one columns
       * are referenced
       */
      val rows = scanToFilter(table)
      val filtered = if (columns.size == 1) {

        BeatAttrs.withName(columns.head) match {
          case TIME =>
            rows.filter { case (time, _) =>
              TimeFilter.filter(time, expression)
            }

          case VALUE =>
            rows.filter { case (_, value) =>
              ValueFilter.filter(value, expression)
            }

          case _ => rows

        }

      } else {
        rows.filter { case (time, value) =>
          TimeValueFilter.filter(time, value, expression)
        }

      }

      filtered.foreach { case (time, value) =>
        val dot = new JsonObject
        dot.addProperty("time", time)
        dot.addProperty("value", value)

        response.add(dot)
      }

      response.toString

    }

  }
  /**
   * Private scan method to support the application
   * of filter conditions
   */
  private def scanToFilter(table:String):Seq[(Long, Double)] = {
    msRocksApi.scan(table)
      .map { case (time, value) => (time, value.toDouble) }
  }

  private def scanToJson(table: String): JsonArray = {

    val response = new JsonArray
    /*
     * Return the full range of available dots
     * from the specified `table`
     */
    msRocksApi.scan(table).foreach { case (time, value) =>

      val dot = new JsonObject
      dot.addProperty("time", time)
      dot.addProperty("value", value.toDouble)

      response.add(dot)

    }

    response

  }

  private def checkColumn(column:String):Unit = {

    try {
      BeatAttrs.withName(column)

    } catch {
      case _:Throwable =>
        val message = s"Unknown column `$column` detected."
        logger.error(message)

        new IllegalArgumentException(message)
    }

  }

  private def checkOutput(output:Seq[String]):Unit = {

    if (output.head == "*") {
      /* Do nothing */

    } else {
      output.foreach(checkColumn)

    }

  }

  private def checkTable(table:String):Unit = {

    try {
      MsTables.withName(table)

    } catch {
      case _:Throwable =>
        val message = s"Unknown table `$table` detected."
        logger.error(message)

        new IllegalArgumentException(message)
    }

  }
}
