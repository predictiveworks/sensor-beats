package de.kp.works.beats.sensor.helium

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
import de.kp.works.beats.sensor.{BeatConf, BeatSource}
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttCallback, MqttClient, MqttMessage}
/**
 * The Helium consumer is built to consume Helium uplink
 * messages from an MQTT broker that is exposed to Helium
 * as the respective endpoint
 */
abstract class Consumer[T <: BeatConf](options:Options[T]) extends BeatSource {

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

      override def messageArrived(topic: String, mqttMessage: MqttMessage) {

        try {
          /*
           * Extract message payload and check
           * whether valid JSON is provided
           */
          val payload = new String(mqttMessage.getPayload)
          val json = JsonParser.parseString(payload)

          val uplink = mapper.readValue(json.toString, classOf[HeliumUplink])
          publish(uplink)

        } catch {
          case t:Throwable =>
            getLogger.error(s"MQTT Message parsing failed: ${t.getLocalizedMessage}")
        }
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
      val message = s"Helium Consumer could not connect to: ${options.getBrokerUrl}."
      getLogger.error(message)
    }
    else {
      val message = s"Helium Consumer is connected with: ${options.getBrokerUrl}."
      getLogger.info(message)

      mqttClient.get.subscribe(options.getMqttTopic, options.getMqttQos)

    }

  }
  /**
   * Method to re-connect and subscribe to the
   * specified MQTT broker
   */
  def restart(t:Throwable): Unit = {
    getLogger.warn(s"MQTT Consumer restart due to: ${t.getLocalizedMessage}")
    subscribeAndPublish()
  }

  def unsubscribe():Unit = {

    if (mqttClient.isEmpty) return
    mqttClient.get.disconnect()

    val message = s"Helium Consumer is disconnected from: ${options.getBrokerUrl}."
    getLogger.info(message)

  }

  protected def publish(uplinkMessage:HeliumUplink):Unit

}
