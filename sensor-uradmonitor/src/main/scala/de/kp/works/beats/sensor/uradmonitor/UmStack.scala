package de.kp.works.beats.sensor.uradmonitor

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
import de.kp.works.beats.sensor.thingsstack.Consumer
import de.kp.works.beats.sensor.uradmonitor.enums.UmProducts
import org.eclipse.paho.client.mqttv3.MqttMessage

class UmStack(options: UmOptions) extends Consumer[UmConf](options.toStack) with UmTransform with UmLogging {

  private val BRAND_NAME = "Uradmonitor"
  /**
   * The configured data sinks configured to send
   * Uradmonitor sensor readings to
   */
  private val sinks = options.getSinks

  override protected def getLogger: Logger = logger

  override protected def publish(mqttMessage: MqttMessage): Unit = {

    try {

      val (deviceId, sensorReadings) = unpack(mqttMessage)

      val product = options.getProduct
      product match {
        case UmProducts.A3 =>
          /*
           * uRADMonitor A3 provides formaldehyde and
           * ozone in parts per billion (ppb).
           *
           * The decoder used for Helium and LORIOT
           * harmonizes these values to ppm
           */

          // Formaldehyde
          val ch20 = sensorReadings.remove("ch20")
            .getAsNumber.doubleValue() * 0.001

          sensorReadings.addProperty("ch20", ch20)

          // Ozone
          val o3 = sensorReadings.remove("o3")
            .getAsNumber.doubleValue() * 0.001

          sensorReadings.addProperty("o3", o3)

        case _ => /* Do nothing */
      }

      val newReadings = transform(sensorReadings, options.getMappings)
      send2Sinks(deviceId, BRAND_NAME, product.toString, newReadings, sinks)

    } catch {
      case t: Throwable =>
        val message = s"Publishing Things Stack $BRAND_NAME event failed: ${t.getLocalizedMessage}"
        getLogger.error(message)
    }
  }

}