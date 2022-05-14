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
import com.google.gson.{JsonObject, JsonParser}
import de.kp.works.beats.sensor._
import de.kp.works.beats.sensor.milesight.MsProducts.{EM_300, EM_500_UDL}
import de.kp.works.beats.sensor.thingsstack.Consumer
import org.eclipse.paho.client.mqttv3.MqttMessage

import scala.collection.JavaConversions.collectionAsScalaIterable
/**
 * The current implementation of the MsConsumer supports
 * the Milesight EM-300 series. [MsThings] is designed as
 * the common consumer for all Milesight sensors, and the
 * publishing of their readings to various output channels.
 */
class MsInput(options: MsOptions) extends Consumer[MsConf](options.toThings) with MsLogging {

  private val BRAND_NAME = "Milesight"
  override protected def getLogger: Logger = logger
  /**
   * Public method to persist the content of the received MQTT message
   * from the Things Stack in the internal RocksDB of the Milesight Beat.
   */
  override def publish(mqttMessage: MqttMessage): Unit = {

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
      val decodedPayload = uplinkMessage.get(TTN_DECODED_PAYLOAD).getAsJsonObject
      /*
       * STEP #3: Extract Milesight product specific
       * parameters
       */
      val product = options.getProduct
      product match {
        case EM_300 =>
          /*
           * Milesight environment sensor EM300-TH-868M
           *
           * Source: https://github.com/Milesight-IoT/SensorDecoders/tree/master/EM300_Series/EM300-TH
           *
           * --------------------- Payload Definition ---------------------
           *
           *                  [channel_id] [channel_type]   [channel_value]
           *
           * 01: battery      -> 0x01         0x75          [1byte ] Unit: %
           * 03: temperature  -> 0x03         0x67          [2bytes] Unit: °C (℉)
           * 04: humidity     -> 0x04         0x68          [1byte ] Unit: %RH
           *
           * ------------------------------------------------------ EM300-TH
           *
           * Sample: 01 75 5C 03 67 34 01 04 68 65
           *
           * {
           * "battery": 92,
           * "humidity": 50.5,
           * "temperature": 30.8
           * }
           *
           * The battery is an optional parameter; the parameter names
           * are directly used as field names for further processing.
           */
          publishPayload(deviceId, product, decodedPayload)

        case EM_500_UDL =>
          /*
           * Milesight ultrasonic level sensor
           *
           * Source: https://github.com/Milesight-IoT/SensorDecoders/tree/master/EM500_Series/EM500-UDL
           *
           * --------------------- Payload Definition ---------------------
           *
           *                  [channel_id] [channel_type]   [channel_value]
           *
           * 01: battery      -> 0x01       0x75            [1 byte]  Unit: %
           * 03: distance     -> 0x03       0x82            [2 bytes] Unit: m
           *
           * ------------------------------------------------------ EM500-UDL
           *
           * Sample: 01 75 5A 03 82 1E 00
           *
           * {
           *   "battery": 90,
           *   "distance": 30
           * }
           *
           * The battery is an optional parameter; the parameter names
           * are directly used as field names for further processing.
           *
           */
          publishPayload(deviceId, product, decodedPayload)

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

  private def publishPayload(deviceId:String, product:MsProducts.Value, payload:JsonObject):Unit = {

    val sensorAttrs = payload.entrySet().map(entry => {
      /*
       * The current implementation transforms decoded
       * values into `Double` values
       */
      val attrName = entry.getKey
      val attrType = "Double"
      val attrValue = entry.getValue.getAsNumber

      BeatAttr(attrName, attrType, attrValue)

    }).toSeq
    /*
     * Build sensor specification for output channel processing.
     * For use cases, where various `SensorBeat`s are used, the
     * product and also the brand name are published to distinguish
     * different beats.
     */
    val sensor = BeatSensor(
      sensorId = deviceId,
      sensorType = product.toString,
      sensorBrand = BRAND_NAME,
      sensorInfo  = BeatInfos.MONITOR,
      sensorTime = System.currentTimeMillis,
      sensorAttrs = sensorAttrs)

    val request = BeatRequest(action = BeatActions.WRITE, sensor = sensor)
    /*
     * Build sensor beat and send to configured output channels
     * for further processing; note, the MsRocks channel is always
     * defined.
     */
    options.getChannels.foreach(channelName => {
      /*
       * Note, a `BeatChannel` is implemented as an Akka actor
       */
      val beatChannel = BeatChannels.getChannel(channelName)
      if (beatChannel.nonEmpty) beatChannel.get ! request

    })

  }
}