package de.kp.works.beats.sensor

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

import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.BeatAttrs.{TIME, VALUE}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.JavaConversions.iterableAsScalaIterable
/**
 * This class supports deep learning tasks for anomaly detection
 * and time series forecasting by transforming a certain RocksDB
 * column family (table) into a Spark compliant [DataFrame]
 */
class BeatFrame(session:SparkSession, logger:Logger) {

  import session.implicits._
  private val beatSql = new BeatSql(session, logger)

  /**
   * Public method to return the entire result of
   * a certain RocksDB column family (table) as an
   * Apache Spark [DataFrame]
   */
  def readAll(table:String):DataFrame = {
    /*
     * STEP #1: Check whether RocksDB is initialized
     */
    if (!BeatRocks.isInit) {
      val message = "RocksDB is not initialized."
      logger.error(message)

      return session.emptyDataFrame
    }
    /*
     * STEP #2: Scan entire RocksDB table and
     * transform result into [DataFrame]
     */
    val rows = toRows(table)
    toDF(rows)

  }
  /**
   * Public method to retrieve the filtered result
   * of a certain RocksDB column family (table) as
   * an Apache Spark [DataFrame]
   */
  def readSql(sql:String):DataFrame = {
    /*
     * STEP #1: Check whether RocksDB is initialized
     */
   if (!BeatRocks.isInit) {
      val message = "RocksDB is not initialized."
      logger.error(message)

      return session.emptyDataFrame
    }
    /*
     * STEP #2: Validate & extract provided SQL
     * statement; [BeatSql] throws an invalid
     * argument exception if something went wrong
     */
    val json = beatSql.parse(sql)
    if (json.isJsonNull) {
      val message = s"Provided SQL statement is not complete."
      logger.warn(message)

      return session.emptyDataFrame
    }

    val obj = json.getAsJsonObject

    val table  = obj.get("table").getAsString
    val condition = obj.get("condition")
    /*
     * STEP #3: Map SQL statement onto RockDB
     * commands
     */
    if (condition.isJsonNull) return readAll(table)
    /*
     * Reminder: The (filter) condition is a JsonObject
     * and is defined as a tree with left & right nodes
     */
    val expression = condition.getAsJsonObject
    val columns = obj.get("columns").getAsJsonArray
      .map(_.getAsString).toSeq
    /*
      * Check whether one or more than one columns
      * are referenced
      */
    val rows = toRows(table)
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

    toDF(filtered)

  }

  private def toRows(table:String):Seq[(Long,Double)] = {
    BeatRocks.scanTs(table)
      .map { case (time, value) => (time, value.toDouble) }
  }

  private def toDF(rows:Seq[(Long, Double)]):DataFrame = {
    if (rows.isEmpty) return session.emptyDataFrame
    session.createDataset(rows).toDF(Seq("time", "value"): _*)
  }

}
