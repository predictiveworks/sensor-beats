package de.kp.works.beats.sensor.thingsboard

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

import de.kp.works.beats.sensor.BeatConf
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class Options[T <: BeatConf](config:T) {
  /**
   * This is the device telemetry topic defined to send messages
   * to the ThingsBoard server:
   *
   * The simplest format
   *
   * {"key1":"value1", "key2":"value2"}
   *
   *  In this case, the server-side timestamp will be assigned
   *  to uploaded data. Providing own timestamps requires the
   *  following format
   *
   *  {"ts":1451649600512, "values":{"key1":"value1", "key2":"value2"}}
   *
   * The timestamp is a unix timestamp with milliseconds precision
   */
  private val DEVICE_TELEMETRY_TOPIC = "v1/devices/me/telemetry"
  /**
   * Configuration of the access parameters
   * of a ThingsBoard HTTP & MQTT endpoint
   */
  private val thingsBoardConfig = config.getThingsBoardCfg
  /**
   * The ThingsBoard device token for the supported sensor
   */
  private def getDeviceToken:String =
    thingsBoardConfig.getString("deviceToken")
  /**
   * TCP address of the ThingsBoard MQTT Broker
   */
  def getBrokerUrl:String =
    thingsBoardConfig.getString("brokerUrl")
  /**
   * The MQTT client identifier configured for all
   * MQTT connections
   */
  def getClientId:String = thingsBoardConfig.getString("clientId")
  /**
   * __KUP__
   *
   * Publishing air quality messages sometimes runs into
   * the following exception:
   *
   * Message: Too many publishes in progress, Reason: 32202,
   * Cause: null
   *
   * This exception was thrown by the Paho MqttClient, even
   * with QOS = 1.
   *
   * This exception is independent of whether the synchronous
   * or asynchronous MQTT client is used.
   *
   * A working solution to avoid this exception is to increase
   * the `maxInflight` value, which vy default is 10
   */
  def getMqttOptions:MqttConnectOptions = {
    /*
     * The MQTT connection is configured to
     * enable automatic re-connection
     */
    val options = new MqttConnectOptions()
    options.setAutomaticReconnect(true)

    options.setCleanSession(true)

    val timeout = thingsBoardConfig.getInt("timeout")
    options.setConnectionTimeout(timeout)

    val keepAlive = thingsBoardConfig.getInt("keepAlive")
    options.setKeepAliveInterval(keepAlive)

    val maxInflight = thingsBoardConfig.getInt("maxInflight")
    options.setMaxInflight(maxInflight)

    /* Authentication
     *
     * Access to a certain ThingsBoard device requires
     * the device token, that is used as user name
     */
    options.setUserName(getDeviceToken)
    /*
     * Connect with MQTT 3.1 or MQTT 3.1.1
     *
     * Depending which MQTT broker you are using, you may want to explicitly
     * connect with a specific MQTT version.
     *
     * By default, Paho tries to connect with MQTT 3.1.1 and falls back to
     * MQTT 3.1 if it’s not possible to connect with 3.1.1.
     *
     * We therefore do not specify a certain MQTT version.
     */
    val mqttVersion = thingsBoardConfig.getInt("mqttVersion")
    options.setMqttVersion(mqttVersion)

    options

  }

  def getMqttTopic:String = DEVICE_TELEMETRY_TOPIC

  /** 						MESSAGE PERSISTENCE
   *
   * Since we don’t want to persist the state of pending
   * QoS messages and the persistent session, we are just
   * using a in-memory persistence. A file-based persistence
   * is used by default.
   */
  def getPersistence:MemoryPersistence = new MemoryPersistence()

  def getQos:Int = thingsBoardConfig.getInt("mqttQoS")

}
