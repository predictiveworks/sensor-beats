package de.kp.works.beats.sensor.weather.api

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
import de.kp.works.beats.sensor.BeatMessages
import de.kp.works.beats.sensor.weather.WeMessages
import de.kp.works.beats.sensor.weather.sandia.SAMRegistry
import org.apache.spark.sql.SparkSession

/**
 * The [Module] actor retrieves a certain CEC
 * module, identified by its name and manufacturer
 */
class Module(session:SparkSession) extends JsonActor {

  override def getEmpty = new JsonObject

  override def executeJson(json: JsonElement): String = {

    val req = mapper.readValue(json.toString, classOf[ModuleReq])

    val manufacturer = req.manufacturer
    val name         = req.name

    if (manufacturer.isEmpty || name.isEmpty) {
      warn(BeatMessages.emptySql())
      return emptyResponse.toString
    }

    try {

      val module = SAMRegistry.getModule(session, manufacturer, name)
      module.toString

    } catch {
      case t: Throwable =>
        error(WeMessages.moduleFailed(t))
        emptyResponse.toString
    }

  }
}

/**
 * The [Modules] actor retrieves CEC modules,
 * either the entire list of latest modules
 * or filtered by a SQL statement
  */
class Modules(session:SparkSession) extends JsonActor {
  /**
   * The response of this request is a JsonArray;
   * in case of an invalid request, an empty response
   * is returned
   */
  override def getEmpty = new JsonArray

  override def executeJson(json:JsonElement): String = {

    val req = mapper.readValue(json.toString, classOf[ModulesReq])
    val sql = req.sql

    if (sql.isEmpty) {
      warn(BeatMessages.emptySql())
      return emptyResponse.toString
    }

    try {
      /*
       * STEP #1: Retrieve CEC modules as Apache Spark
       * [DataFrame]
       */
      val dataframe = if (sql.isEmpty) {
        SAMRegistry.getModules(session)

      } else {

        val sqlS = sql
          .replace("CEC_MODULES", "global_temp.CEC_MODULES")

        SAMRegistry.getModulesBySql(session, sqlS)

      }
      /*
       * STEP #2: Transform [DataFrame] into JSONArray
       */
      val result = dataframeToJson(dataframe)
      result.toString

    } catch {
      case t: Throwable =>
        error(WeMessages.modulesFailed(t))
        emptyResponse.toString
    }

  }
}
