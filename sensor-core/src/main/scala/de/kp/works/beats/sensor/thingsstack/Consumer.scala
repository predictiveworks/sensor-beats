package de.kp.works.beats.sensor.thingsstack

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
import de.kp.works.beats.sensor.{BeatConf, BeatSource}
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttCallback, MqttClient, MqttMessage}

abstract class Consumer[T <: BeatConf](options:Options[T]) extends BeatSource {
  /**
   * FIELD NAMES of the TTN v3 uplink message format
   */
  protected val TTN_DECODED_PAYLOAD = "decoded_payload"
  protected val TTN_DEVICE_ID       = "device_id"
  protected val TTN_END_DEVICE_IDS  = "end_device_ids"
  protected val TTN_RX_METADATA     = "rx_metadata"
  protected val TTN_UPLINK_MESSAGE  = "uplink_message"

  private val mqttClient: Option[MqttClient] = buildMqttClient
  protected def getLogger:Logger

  /**
   * Internal method to build an Eclipse Paho
   * MQTT client with memory persistence
   */
  private def buildMqttClient:Option[MqttClient] = {

    val brokerUrl = options.getBrokerUrl
    val clientId  = options.getClientId

    val persistence = options.getMqttPersistence
    Some(new MqttClient(brokerUrl, clientId, persistence))

  }
  /**
   * Public method to subscribe to the MQTT broker
   * specified (like The ThingsStack) and publish
   * incoming events in the RocksDB
   */
  def subscribeAndPublish():Unit = {
    /*
     * Callback automatically triggers as and when new message
     * arrives on specified topic; this callback defines the
     * bridge between the MQTT broker and the internal RocksDB
     */
    val callback: MqttCallback = new MqttCallback() {

      override def messageArrived(topic: String, message: MqttMessage) {
        publish(message)
      }

      override def deliveryComplete(token: IMqttDeliveryToken) {}

      override def connectionLost(cause: Throwable) {
        restart(cause)
      }

    }
    /*
     * Make sure that the Mqtt client is defined
     * to connect to the MQTT broker
     */
    if (mqttClient.isEmpty) buildMqttClient
    /*
     * Set up callback for MqttClient. This needs to happen before
     * connecting or subscribing, otherwise messages may be lost
     */
    mqttClient.get.setCallback(callback)

    val mqttOptions = options.getMqttOptions
    mqttClient.get.connect(mqttOptions)

    if (!mqttClient.get.isConnected) {
      val message = s"Things Stack Consumer could not connect to: ${options.getBrokerUrl}."
      getLogger.error(message)
    }
    else {
      val message = s"Things Stack Consumer is connected with: ${options.getBrokerUrl}."
      getLogger.info(message)

      mqttClient.get.subscribe(options.getMqttTopic, options.getMqttQos)

    }

  }
  /**
   * Method to re-connect and subscribe to the
   * specified MQTT broker
   */
  def restart(t:Throwable): Unit = {
    getLogger.warn(s"Things Stack Consumer restart due to: ${t.getLocalizedMessage}")
    subscribeAndPublish()
  }

  def unsubscribe():Unit = {

    if (mqttClient.isEmpty) return
    mqttClient.get.disconnect()

    val message = s"Things Stack Consumer is disconnected from: ${options.getBrokerUrl}."
    getLogger.info(message)

  }

  protected def publish(mqttMessage:MqttMessage):Unit
  /**
   * Helper method to extract the `deviceId`, and the
   * provided TTN payload, and, thereby flatten the
   * payload to a unique {key: value} format
   */
  protected def unpack(mqttMessage:MqttMessage):(String, JsonObject) = {

    val payload = new String(mqttMessage.getPayload)
    val json = JsonParser.parseString(payload)
    /*
     * Extract uplink message and associated
     * decoded payload
     */
    val messageObj = json.getAsJsonObject
    /*
     * Extract the unique TTN device identifier,
     * which is also used to uniquely identify
     * the sensor.
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
     * Extract the decoded payload from the
     * provided TTN v3 uplink message
     */
    val uplinkMessage = messageObj.get(TTN_UPLINK_MESSAGE).getAsJsonObject
    var sensorReadings = uplinkMessage.get(TTN_DECODED_PAYLOAD).getAsJsonObject
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
    if (sensorReadings.has("data")) {
      /*
       * Flatten the sensor readings
       */
      val data = sensorReadings.remove("data").getAsJsonObject
      sensorReadings = data
    }

    (deviceId, sensorReadings)
  }
}
