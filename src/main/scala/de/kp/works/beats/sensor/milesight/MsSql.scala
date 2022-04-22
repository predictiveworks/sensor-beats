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
import com.google.gson.JsonArray
import org.apache.spark.sql.{BeatSession, BeatSql}

class MsSql(queue: SourceQueueWithComplete[String], logger:Logger) {
  /**
   * Validating the SQL statement is performed
   * using the Apache Spark SqlParser, which is
   * invoked via a Spark session
   */
  private val session = BeatSession.getSession
  private val beatSql = new BeatSql(session, logger)

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
     * columns and (optional) conditions refers to
     * an existing database specification
     */
    ???
  }
}
