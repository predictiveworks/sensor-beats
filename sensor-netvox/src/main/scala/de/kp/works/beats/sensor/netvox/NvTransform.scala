package de.kp.works.beats.sensor.netvox

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

import com.google.gson.JsonObject
import de.kp.works.beats.sensor.netvox.mappings.NvMapper

import scala.collection.JavaConversions.asScalaSet

trait NvTransform {

  def transform(sensorReadings:JsonObject, mappings:Map[String,String]):JsonObject = {

    var newReadings = sensorReadings
    /*
     * The current implementation of the decoded payload
     * has the following format:
     *
     * {
     *    data: ...
     * }
     *
     * SensorBeat is based on a common {key, value} format
     */
    if (newReadings.has("data")) {
      /*
       * Flatten the sensor readings
       */
      val data = newReadings.remove("data").getAsJsonObject
      newReadings = data
    }

    // Remove fields with String values
    val removables = Seq("Cmd", "Device")
    removables.foreach(removable => {
      if (newReadings.has(removable)) {
        newReadings.remove(removable)
      }
    })

    // Volt
    if (newReadings.has("Volt")) {
      val volt = newReadings
        .remove("Volt")
        .getAsJsonPrimitive

      try {
        if (volt.isString) {
          /*
           * This `volt` is provided by `The Things Stack`
           * and is formatted as [String]
           */
          val value = volt
            .getAsString
            .replace("(low battery)", "")
            .trim
            .toDouble

          newReadings.addProperty("Volt", value)
        }
        else if (volt.isNumber) {
          /*
           * This `status` is provided by Helium or LORIOT
           */
          newReadings.addProperty("Volt", volt.getAsNumber)
        }

      } catch {
        case _:Throwable => /* Do nothing */
      }
    }

    // Status :: Success = 1 | Failure = 0
    if (newReadings.has("Status")) {
      val status = newReadings
        .remove("Status")
        .getAsJsonPrimitive

      try {

        if (status.isString) {
          /*
           * This `status` is provided by `The Things Stack`
           * and is formatted as [String]
           */
          val value =
            if (status.getAsString.toLowerCase == "success") 1 else 0

          newReadings.addProperty("Status", value)
        }
        else if (status.isNumber) {
          /*
           * This `status` is provided by Helium or LORIOT
           */
          newReadings.addProperty("Status", status.getAsNumber)
        }

      } catch {
        case _:Throwable => /* Do nothing */
      }
    }
    /*
     * The current implementation of SensorBeat supports
     * primitive JSON values only and also harmonizes the
     * respective field names.
     *
     * In addition to this internal harmonization, there is
     * an external mapping supported.
     */
    val fields = newReadings.keySet()
    fields.foreach(name => {
      val value = newReadings.remove(name)
      /*
       * Check whether the respective field
       * value is a JSON primitive
       */
      if (value.isJsonPrimitive) {

        val alias =
          if (mappings.contains(name)) {
            /*
             * Apply configured mapping
             */
            mappings(name)

          } else {
            /*
             * Leverage pre-built internal
             * field name mapping
             */
            NvMapper.harmonize(name)
          }

        newReadings.addProperty(alias, value.getAsDouble)

      }

    })

    newReadings

  }
}
