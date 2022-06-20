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

import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.helium.{Consumer, HeliumUplink}

import java.util.Base64

class EsHelium(options: EsOptions) extends Consumer[EsConf](options.toHelium) with EsTransform with EsLogging {

  private val BRAND_NAME = "Elsys"
  /**
   * The configured data sinks configured to send
   * Elsys sensor readings to
   */
  private val sinks = options.getSinks

  override protected def getLogger: Logger = logger

  override protected def publish(message: HeliumUplink): Unit = {

    try {
      /*
       * Data transmitted by the device is a base64 encoded String.
       */
      val decodedPayload = Base64.getDecoder.decode(message.payload)
      /*
       * The uplink message provides the size of the payload and this
       * parameter is used to verify the payload
       */
      if (decodedPayload.length != message.payload_size.intValue()) return
      /*
       * Send sensor readings (payload) to the configured
       * data sinks; note, attributes are restricted to [Number]
       * fields.
       *
       * This restriction is ensured by the Milesight decoders
       * provided with this project
       */
      val product = options.getProduct

      val sensorReadings = EsDecoder.decodeHex(product, new String(decodedPayload))
      val newReadings = transform(sensorReadings, options.getMappings)
      /*
       * The `dev_eui` is used as a unique device identifier:
       *
       * LoRaWAN 64-bit Device Identifier (DevEUI) in MSB hex;
       * conventionally this is used to identify a unique device
       * within a specific application (AppEUI) or even within an
       * entire organization
       */
      val deviceId = message.dev_eui
      send2Sinks(deviceId, BRAND_NAME, product.toString, newReadings, sinks)

    } catch {
      case t: Throwable =>
        val message = s"Publishing Helium $BRAND_NAME event failed: ${t.getLocalizedMessage}"
        getLogger.error(message)
    }

  }

}
