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

import com.google.gson.{JsonArray, JsonElement, JsonObject}
import de.kp.works.beats.sensor.BeatAttrs.{TIME, VALUE}
import de.kp.works.beats.sensor.ta4j.TATrend
import de.kp.works.beats.sensor._
import org.apache.spark.sql.BeatSession

import scala.collection.JavaConversions.iterableAsScalaIterable
/**
 * [MsSql] supports the `Sensor as a Table` concept,
 * that is taken from Osquery.
 */
class MsSql extends TATrend with MsLogging {
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
  private val rocksApi = BeatRocksApi.getInstance
  private val emptyResponse = new JsonArray

  private var table:String = _
  private var output:Seq[String] = _

  private var columns:Seq[String] = _
  private var condition:JsonElement = _

  def read(sql:String):String = {

    clear()
    /*
     * STEP #1: Validate & extract provided SQL
     * statement; [BeatSql] throws an invalid
     * argument exception if something went wrong
     */
    val json = beatSql.parse(sql)
    if (!isValid(json))
      return emptyResponse.toString
    /*
     * STEP #2: Map SQL statement onto RockDB
     * commands
     */
    var response = new JsonArray
    if (condition.isJsonNull) {

      response = toJson
      response.toString

    } else {

      val response = new JsonArray
      toSqlRows.foreach { case (time, value) =>
        val jo = new JsonObject
        jo.addProperty("time", time)
        jo.addProperty("value", value)

        response.add(jo)
      }

      response.toString

    }

  }

  def trend(sql:String, indicator:String, timeframe:Int=5):String = {

    clear()
    /*
     * STEP #1: Validate & extract provided SQL
     * statement; [BeatSql] throws an invalid
     * argument exception if something went wrong
     */
    val json = beatSql.parse(sql)
    if (!isValid(json))
      return emptyResponse.toString
    /*
     * STEP #2: Map SQL statement onto RockDB
     * commands and compute dots
     */
    val dots =
      if (condition.isJsonNull) toDots else toSqlDots
    /*
     * STEP #3: Compute trend with provided technical
     * indicator
     */
    val trend = analyze(dots, indicator, timeframe)

    val response = new JsonArray
    trend.foreach { dot =>
      val jo = new JsonObject
      jo.addProperty("time", dot.time)
      jo.addProperty("value", dot.value)

      response.add(jo)
    }

    response.toString

  }
  /**
   * Private helper method to reset the metadata
   * extracted from the SQL query.
   */
  private def clear():Unit = {

    table = null
    output = null

    columns = null
    condition = null

  }

  private def isValid(json:JsonElement):Boolean = {

    if (json.isJsonNull) {
      val message = s"Provided SQL statement is not complete."
      logger.warn(message)

      return false
    }
    /*
     * Check whether the extracted table, columns and
     * (optional) conditions refer to an existing database
     * specification
     */
    val obj = json.getAsJsonObject

    val sqlTable  = obj.get("table").getAsString
    checkTable(sqlTable)

    table = sqlTable

    val sqlOutput = obj.get("output").getAsJsonArray
      .map(_.getAsString).toSeq
    checkOutput(sqlOutput)

    output = sqlOutput
    condition = obj.get("condition")

    if (!obj.get("columns").isJsonNull)
      columns = obj.get("columns").getAsJsonArray
        .map(_.getAsString).toSeq

    true

  }

  private def toDots:Seq[BeatDot] = {
    rocksApi.scan(table)
      .map { case (time, value) => BeatDot(time, value.toDouble) }
  }

  private def toSqlDots:Seq[BeatDot] = {
    toSqlRows
      .map { case (time, value) => BeatDot(time, value) }
  }

  private def toRows:Seq[(Long,Double)] = {
    rocksApi.scan(table)
      .map { case (time, value) => (time, value.toDouble) }
  }

  private def toSqlRows:Seq[(Long,Double)] = {
    /*
     * Reminder: The (filter) condition is a JsonObject
     * and is defined as a tree with left & right nodes
     */
    val expression = condition.getAsJsonObject
    /*
     * Check whether one or more than one columns
     * are referenced
     */
    val rows = toRows
    val sqlRows = if (columns.size == 1) {

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

    sqlRows

  }

  private def toJson: JsonArray = {

    val response = new JsonArray
    /*
     * Return the full range of available dots
     * from the specified `table`
     */
    rocksApi.scan(table).foreach { case (time, value) =>

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
