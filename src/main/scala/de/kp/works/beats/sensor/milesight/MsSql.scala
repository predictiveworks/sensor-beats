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
import org.apache.spark.sql.{BeatSession, BeatSql}

import scala.collection.JavaConversions.iterableAsScalaIterable

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
    if (condition.isJsonNull) {
      /*
       * Return the full range of available
       * dots from the specified `table`
       */
      val response = new JsonArray
      msRocksApi.scan(table).foreach{case(time, value) =>

        val dot = new JsonObject
        dot.addProperty("time", time)
        dot.addProperty("value", value.toDouble)

        response.add(dot)
      }

      response.toString

    } else {
      /*
       * Reminder: The (filter) condition is a JsonObject
       * and is defined as a tree with left & right nodes
       */
      val response = new JsonArray
      // TODO
      response.toString

    }

  }

  private def checkColumn(column:String):Unit = {

    try {
      MsAttrs.withName(column)

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
