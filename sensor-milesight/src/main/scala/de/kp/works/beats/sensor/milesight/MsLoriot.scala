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

import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.loriot.{Consumer, LoriotUplink}

import scala.collection.JavaConversions.asScalaSet
/**
 * The [MsLoriot] input channel focuses on the
 * extraction of the unique device identifier
 * and the provided sensor readings
 */
class MsLoriot(options: MsOptions) extends Consumer[MsConf](options.toLoriot) with MsLogging {

  private val BRAND_NAME = "Milesight"
  /**
   * The configured data sinks configured to send
   * Milesight sensor readings to
   */
  private val sinks = options.getSinks

  override protected def getLogger: Logger = logger

  override protected def publish(message: LoriotUplink): Unit = {

    try {
      /*
       * Make sure the extracted message is a LORIOT
       uplink message
       */
      if (message.cmd != "rx") return
      /*
       * The current implementation of SensorBeat does not
       * supported encrypted data payloads (which refers to
       * a missing APP KEY
       */
      if (message.encdata.nonEmpty || message.data.isEmpty) return
      /*
       * Send sensor readings (payload) to the configured
       * data sinks; note, attributes are restricted to [Number]
       * fields.
       *
       * This restriction is ensured by the Milesight decoders
       * provided with this project
       */
      val product = options.getProduct
      var sensorReadings = MsDecoder.decodeHex(product, message.data.get)
      /*
       * Check whether the sensor readings are wrapped
       * in a {data: ...} object
       */
      if (sensorReadings.has("data")) {
        /*
         * Flatten the sensor readings
         */
        val data = sensorReadings.remove("data").getAsJsonObject
        sensorReadings = data
      }
      /*
       * Apply field mappings and replace those decoded field
       * names by their aliases that are specified on the
       * provided mappings
       */
      val mappings = options.getMappings
      if (mappings.nonEmpty) {
        val fields = sensorReadings.keySet()
        fields.foreach(name => {
          if (mappings.contains(name)) {
            val alias = mappings(name)
            val property = sensorReadings.remove(name)

            sensorReadings.addProperty(alias, property.getAsDouble)
          }
        })
      }
      /*
       * Note, the EUI value is used as unique device identifier
       */
      val deviceId = message.EUI
      send2Sinks(deviceId, BRAND_NAME, product.toString, sensorReadings, sinks)

    } catch {
      case t: Throwable =>
        val message = s"Publishing LORIOT $BRAND_NAME event failed: ${t.getLocalizedMessage}"
        getLogger.error(message)
    }

  }

}
