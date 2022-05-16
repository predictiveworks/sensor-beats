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
import de.kp.works.beats.sensor.BeatConf
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class Options[T <: BeatConf](config:T) {

  private val heliumCfg = config.getHeliumCfg

  def getBrokerUrl:String =
    heliumCfg.getString("brokerUrl")

  def getClientId:String =
    heliumCfg.getString("clientId")

  def getMqttOptions:MqttConnectOptions = {
    /*
     * The MQTT connection is configured to
     * enable automatic re-connection
     */
    val options = new MqttConnectOptions()
    options.setAutomaticReconnect(true)

    options.setCleanSession(true)

    val timeout = heliumCfg.getInt("timeout")
    options.setConnectionTimeout(timeout)

    val keepAlive = heliumCfg.getInt("keepAlive")
    options.setKeepAliveInterval(keepAlive)
    /*
     * Authentication
     */
    val mqttUser = heliumCfg.getString("mqttUser")
    options.setUserName(mqttUser)

    val mqttPass = heliumCfg.getString("mqttPass")
    options.setPassword(mqttPass.toCharArray)
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
    val mqttVersion = heliumCfg.getInt("mqttVersion")
    options.setMqttVersion(mqttVersion)

    options

  }
  /** 						MESSAGE PERSISTENCE
   *
   * Since we don’t want to persist the state of pending
   * QoS messages and the persistent session, we are just
   * using an in-memory persistence. A file-based persistence
   * is used by default.
   */
  def getMqttPersistence:MemoryPersistence = new MemoryPersistence()

  def getMqttQos:Int =
    heliumCfg.getInt("mqttQoS")

  def getMqttTopic:String =
    heliumCfg.getString("mqttTopic")

}
