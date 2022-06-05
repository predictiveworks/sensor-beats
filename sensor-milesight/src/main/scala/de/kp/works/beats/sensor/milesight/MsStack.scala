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
import com.google.gson.JsonParser
import de.kp.works.beats.sensor.milesight.enums.MsProducts._
import de.kp.works.beats.sensor.thingsstack.Consumer
import org.eclipse.paho.client.mqttv3.MqttMessage

import scala.collection.JavaConversions.asScalaSet

class MsStack(options: MsOptions) extends Consumer[MsConf](options.toStack) with MsLogging {

  private val BRAND_NAME = "Milesight"
  /**
   * The configured data sinks configured to send
   * Milesight sensor readings to
   */
  private val sinks = options.getSinks

  override protected def getLogger: Logger = logger

  override protected def publish(mqttMessage: MqttMessage): Unit = {

    try {

      val payload = new String(mqttMessage.getPayload)
      val json = JsonParser.parseString(payload)
      /*
       * Extract uplink message and associated
       * decoded payload
       */
      val messageObj = json.getAsJsonObject
      /*
       * STEP #1: Extract the unique TTN device identifier,
       * which is also used to uniquely identify the sensor.
       *
       * {
       *  "end_device_ids" : {
       *    "device_id" : "dev1",                    // Device ID
       *    "application_ids" : {
       *      "application_id" : "app1"              // Application ID
       *    },
       *    "dev_eui" : "0004A30B001C0530",          // DevEUI of the end device
       *    "join_eui" : "800000000000000C",         // JoinEUI of the end device (also known as AppEUI in LoRaWAN versions below 1.1)
       *    "dev_addr" : "00BCB929"                  // Device address known by the Network Server
       * },
       *
       * ...
       */
      val endDeviceIds = messageObj
        .get(TTN_END_DEVICE_IDS).getAsJsonObject

      val deviceId = endDeviceIds
        .get(TTN_DEVICE_ID).getAsString
      /*
       * STEP #2: Extract the decoded payload from the
       * provided TTN v3 uplink message
       */
      val uplinkMessage = messageObj.get(TTN_UPLINK_MESSAGE).getAsJsonObject
      val sensorReadings = uplinkMessage.get(TTN_DECODED_PAYLOAD).getAsJsonObject
      /*
       * STEP #3: Convert decoded sensors that refer
       * to textual values
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

        case _ => /* Do nothing */

      }
      /*
       * STEP #4: Apply field mappings and replace those decoded
       * field names by their aliases that are specified on the
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

      send2Sinks(deviceId, BRAND_NAME, product.toString, sensorReadings, sinks)

    } catch {
      case t: Throwable =>
        val message = s"Publishing Things Stack $BRAND_NAME event failed: ${t.getLocalizedMessage}"
        getLogger.error(message)
    }
  }

}