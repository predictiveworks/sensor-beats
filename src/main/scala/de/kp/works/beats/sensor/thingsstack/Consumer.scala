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
import com.google.gson.JsonObject
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttCallback, MqttClient, MqttMessage}

abstract class Consumer(options:Options) {
  /**
   * FIELD NAMES of the TTN v3 uplink message format
   */
  protected val TTN_DECODED_PAYLOAD = "decoded_payload"
  protected val TTN_DEVICE_ID       = "device_id"
  protected val TTN_END_DEVICE_IDS  = "end_device_ids"
  protected val TTN_RX_METADATA     = "rx_metadata"
  protected val TTN_UPLINK_MESSAGE  = "uplink_message"

  private val mqttClient: Option[MqttClient] = buildMqttClient
  protected var logger:Logger

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
      val message = s"MQTT Consumer could not connect to: ${options.getBrokerUrl}."
      logger.error(message)
    }
    else {
      val message = s"MQTT Consumer is connected with: ${options.getBrokerUrl}."
      logger.info(message)

      mqttClient.get.subscribe(options.getMqttTopic, options.getMqttQos)

    }

  }
  /**
   * Method to re-connect and subscribe to the
   * specified MQTT broker
   */
  def restart(t:Throwable): Unit = {
    logger.warn(s"MQTT Consumer restart due to: ${t.getLocalizedMessage}")
    subscribeAndPublish()
  }

  def unsubscribe():Unit = {

    if (mqttClient.isEmpty) return
    mqttClient.get.disconnect()

    val message = s"MQTT Consumer is disconnected from: ${options.getBrokerUrl}."
    logger.info(message)

  }
  /**
   * Public method to persist the content of the
   * received MQTT message in the internal RocksDB
   * of the `SensorBeat`.
   */
  def publish(mqttMessage:MqttMessage):Unit
  /**
   * Public method to extract the common TTN uplink
   * payload.
   */
  def extract(messageObj:JsonObject):Option[JsonObject] = {

    ???
  }
  /*


   */
}
