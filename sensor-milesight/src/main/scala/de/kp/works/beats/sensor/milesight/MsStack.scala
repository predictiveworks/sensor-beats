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
import de.kp.works.beats.sensor.milesight.enums.MsProducts._
import de.kp.works.beats.sensor.thingsstack.Consumer
import org.eclipse.paho.client.mqttv3.MqttMessage

import scala.collection.JavaConversions.asScalaSet

class MsStack(options: MsOptions) extends Consumer[MsConf](options.toStack) with MsTransform with MsLogging {

  private val BRAND_NAME = "Milesight"
  /**
   * The configured data sinks configured to send
   * Milesight sensor readings to
   */
  private val sinks = options.getSinks

  override protected def getLogger: Logger = logger

  override protected def publish(mqttMessage: MqttMessage): Unit = {

    try {

      val (deviceId, sensorReadings) = unpack(mqttMessage)
      /*
       * Convert decoded sensors that refer to textual values
       */
      val product = options.getProduct
      product match {
        case AM300 =>
          try {
            // beep
            {
              val key = "beep"
              val value = sensorReadings.remove(key).getAsString
              sensorReadings.addProperty(key, if (value == "yes") 1D else 0D)
            }
            // pir
            {
              val key = "pir"
              val value = sensorReadings.remove(key).getAsString
              sensorReadings.addProperty(key, if (value == "trigger") 1D else 0D)
            }

          } catch {
            case _: Throwable => /* Do nothing */
          }

        case EM300_MCS =>

          try {
            // door
            {
              val key = "door"
              val value = sensorReadings.remove(key).getAsString
              sensorReadings.addProperty(key, if (value == "open") 1D else 0D)
            }

          } catch {
            case _: Throwable => /* Do nothing */
          }

        case EM300_SLD | EM300_ZLD =>

          try {
            // water_leak
            {
              val key = "water_leak"
              val value = sensorReadings.remove(key).getAsString
              sensorReadings.addProperty(key, if (value == "leak") 1D else 0D)
            }

          } catch {
            case _: Throwable => /* Do nothing */
          }

        case UC500 =>
          /*
           * The TTN decoding transforms fields that are
           * decoded as [String] values into [Number].
           */
          try {

            val keys = sensorReadings.keySet().filter(key => {
              key.startsWith("gpio") || key.startsWith("chn")
            })
            keys.foreach(key => {
              val value = sensorReadings.remove(key).getAsString
              if (value == "off" || value == "on")
                sensorReadings.addProperty(key, if (value == "off") 0D else 1D)
            })

          } catch {
            case _: Throwable => /* Do nothing */
          }

        case VS121 =>
          /*
           * Remove provided fields that are not relevant
           * for measurements and computing insights
           */
          try {

            val excluded = Seq(
              "firmware_version",
              "hardware_version",
              "protocol_version",
              "sn"
            )

            excluded.foreach(exclude =>
              if (sensorReadings.has(exclude)) sensorReadings.remove(exclude)
            )

          } catch {
            case _: Throwable => /* Do nothing */
          }

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