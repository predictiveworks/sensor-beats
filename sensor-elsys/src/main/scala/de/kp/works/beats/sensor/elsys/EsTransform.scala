package de.kp.works.beats.sensor.elsys

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
import de.kp.works.beats.sensor.elsys.mappings.EsMapper

import scala.collection.JavaConversions.asScalaSet

trait EsTransform {

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
            EsMapper.harmonize(name)
          }

        newReadings.addProperty(alias, value.getAsDouble)

      }

    })

    newReadings
  }

}
