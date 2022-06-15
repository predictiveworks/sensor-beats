package de.kp.works.beats.sensor.weather.pvlib

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

import com.google.gson.{JsonArray, JsonObject, JsonParser}
import scala.collection.JavaConversions.asScalaSet

object PVlibUtil {

  def result2Json(output:String):JsonArray = {

    try {

      val json = JsonParser.parseString(output).getAsJsonObject
      val result = new JsonArray

      val rows = json.keySet().flatMap(fname => {

        val fvalues = json.get(fname).getAsJsonObject
        fvalues.keySet().map(ts => {
          val fvalue = fvalues.get(ts).getAsDouble
          (ts.toLong, fname, fvalue)
        })

      })
        .groupBy{case(ts, _, _) => ts}
        .map { case (ts, columns) =>

          val jRow = new JsonObject
          jRow.addProperty("timestamp", ts)

          columns.foreach { case (_, name, value) => jRow.addProperty(name, value) }

          jRow

        }

      rows.foreach(result.add)
      result

    } catch {
      case _:Throwable => new JsonArray
    }

  }
}
