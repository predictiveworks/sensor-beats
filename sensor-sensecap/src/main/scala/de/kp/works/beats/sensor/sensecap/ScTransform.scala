package de.kp.works.beats.sensor.sensecap

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

import com.google.gson.{JsonElement, JsonNull, JsonObject}
import de.kp.works.beats.sensor.sensecap.enums.ScMeasurements

import scala.collection.JavaConversions.iterableAsScalaIterable

trait ScTransform {

  def transform(sensorReadings:JsonObject, mappings:Map[String,String]):JsonElement = {
    /*
     * SenseCAP Wireless CO2 sensor (sample)
     *
     * {
     *    "err": 0,
     *    "messages": [
     *      {
     *        "measurementId": 4100,
     *        "measurementValue": 364,
     *        "type": "report_telemetry"
     *      },
     *      ...
     *    ],
     *    "payload": "010410E08D05009802",
     *    "valid": true
     * }
     */
    val err = sensorReadings.get("err").getAsInt
    val valid = sensorReadings.get("valid").getAsBoolean

    if (err != 0 || !valid) throw new Exception(s"SenseCap sensor readings is invalid.")

    val newReadings = new JsonObject
    /*
     * Restrict provided messages to those that
     * refer to telemetry data
     */
    val messages = sensorReadings.get("messages")
      .getAsJsonArray.toList
      .filter(message => {

        val messageType = message.getAsJsonObject
          .get("type").getAsString
        messageType == "report_telemetry"

      })

    if (messages.isEmpty) return JsonNull.INSTANCE
    messages.foreach(message => {

      val obj = message.getAsJsonObject
      val mid = obj.get("measurementId").getAsInt

      if (ScMeasurements.mappings.contains(mid)) {

        val attrName = ScMeasurements.mappings(mid)
        val attrValue =
          try {obj.get("measurementValue").getAsNumber} catch {case _:Throwable => null}

        if (attrValue != null) {
          newReadings.addProperty(attrName, attrValue)
        }

      }

    })

    val fields = newReadings.keySet()
    /*
     * Apply field mappings and replace those decoded field
     * names by their aliases that are specified on the
     * provided mappings
     */
    if (mappings.nonEmpty) {
      fields.foreach(name => {
        if (mappings.contains(name)) {
          val alias = mappings(name)
          val property = newReadings.remove(name)

          newReadings.addProperty(alias, property.getAsDouble)
        }
      })
    }

    newReadings

  }
}
