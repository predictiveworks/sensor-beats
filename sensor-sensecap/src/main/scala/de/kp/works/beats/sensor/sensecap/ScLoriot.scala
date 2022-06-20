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

import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.loriot.{Consumer, LoriotUplink}

/**
 * The [ScLoriot] input channel focuses on the
 * extraction of the unique device identifier
 * and the provided sensor readings
 */
class ScLoriot(options: ScOptions) extends Consumer[ScConf](options.toLoriot) with ScTransform with ScLogging {

  private val BRAND_NAME = "Sensecap"
  /**
   * The configured data sinks configured to send
   * SenseCap sensor readings to
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
      var sensorReadings = ScDecoder.decodeHex(product, message.data.get)
      /*
       * SenseCap leverage a data format that describes
       * sensor readings in form of telemetry messages
       */
      val transformed = transform(sensorReadings, options.getMappings)
      if (!transformed.isJsonNull) {

        val newReadings = transformed.getAsJsonObject
        /*
         * Note, the EUI value is used as unique device identifier
         */
        val deviceId = message.EUI
        send2Sinks(deviceId, BRAND_NAME, product.toString, newReadings, sinks)

      }

    } catch {
      case t: Throwable =>
        val message = s"Publishing LORIOT $BRAND_NAME event failed: ${t.getLocalizedMessage}"
        getLogger.error(message)
    }

  }

}
