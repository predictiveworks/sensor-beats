package de.kp.works.beats.sensor.ellenex

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
import de.kp.works.beats.sensor.ellenex.enums.ExProducts
import de.kp.works.beats.sensor.thingsstack.Consumer
import org.eclipse.paho.client.mqttv3.MqttMessage

class ExStack(options: ExOptions) extends Consumer[ExConf](options.toStack) with ExTransform with ExLogging {

  private val BRAND_NAME = "Ellenex"
  /**
   * The configured data sinks configured to send
   * Ellenex sensor readings to
   */
  private val sinks = options.getSinks

  override protected def getLogger: Logger = logger

  override protected def publish(mqttMessage: MqttMessage): Unit = {

    try {

      val (deviceId, sensorReadings) = unpack(mqttMessage)
      val product = options.getProduct
      product match {
        case ExProducts.PTD2_L =>

          if (sensorReadings.has("primarySense")) {

            val value = sensorReadings.remove("primarySense")
            sensorReadings.add("pressure", value)

          }

          if (sensorReadings.has("secondarySense")) {

            val value = sensorReadings.remove("secondarySense")
            sensorReadings.add("temperature", value)

          }

      }
      /*
       * Send sensor readings (payload) to the configured data
       * sinks; note, attributes are restricted to [Number] fields.
       */
      val newReadings = transform(sensorReadings, options.getMappings)
      send2Sinks(deviceId, BRAND_NAME, product.toString, newReadings, sinks)

    } catch {
      case t: Throwable =>
        val message = s"Publishing Things Stack $BRAND_NAME event failed: ${t.getLocalizedMessage}"
        getLogger.error(message)
    }
  }

}