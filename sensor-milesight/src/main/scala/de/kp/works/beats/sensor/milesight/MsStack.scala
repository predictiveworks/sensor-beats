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
/**
 * The current implementation of the MsConsumer supports
 * the Milesight EM-300 series. [MsThings] is designed as
 * the common consumer for all Milesight sensors, and the
 * publishing of their readings to various output channels.
 */
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
       * STEP #3: Send sensor readings (payload) to the
       * configured data sinks; note, attributes are
       * restricted to [Number] fields.
       */
      val product = options.getProduct
      product match {
        case EM_300 =>
          send2Sinks(deviceId, BRAND_NAME, product.toString, sensorReadings, sinks)

        case EM_500_UDL =>
          send2Sinks(deviceId, BRAND_NAME, product.toString, sensorReadings, sinks)

        case EM_500_CO2 =>
          send2Sinks(deviceId, BRAND_NAME, product.toString, sensorReadings, sinks)

        case _ =>
          val message = s"The specified Milesight sensor product is not supported."
          getLogger.warn(message)
      }

    } catch {
      case t:Throwable =>
        val message = s"Publishing Milesight event failed: ${t.getLocalizedMessage}"
        getLogger.error(message)
    }
  }

}