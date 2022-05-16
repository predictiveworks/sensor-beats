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

import com.google.gson.JsonObject
import de.kp.works.beats.sensor.BeatActions.{CREATE, UPDATE}
import de.kp.works.beats.sensor.{BeatSink, BeatConf, BeatRequest, BeatSensor}
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttException, MqttMessage}

/**
 * Base output channel to a ThingsBoard Mqtt Broker
 */
abstract class Producer[T <: BeatConf](options:Options[T]) extends BeatSink {

  private val mqttClient = mqttConnect

  override def execute(request: BeatRequest): Unit = {

    request.action match {
      /*
       * This producer does not distinguish between `create` and `update`
       * requests, as this implementation uses the [Manager] to create and
       * register devices.
       */
      case CREATE | UPDATE =>
        publish(request.sensor)

      case _ => /* Do nothing */
    }

  }

  def error(message:String):Unit

  def info(message:String):Unit

  private def publish(sensor:BeatSensor): Unit = {
    /*
      * The sensor is transformed into a
      * ThingsBoard compliant JSON payload:
      *
      * Payload {
      *    ts: ...,
      *    values: {
      *      name: value,
      *    }
      * }
      */
    val payload = new JsonObject
    payload.addProperty("ts", sensor.sensorTime)

    val values = new JsonObject
    sensor.sensorAttrs.foreach(sensorAttr => {

      val attrName = sensorAttr.attrName
      val attrType = sensorAttr.attrType

      val attrValu = sensorAttr.attrValue
      attrType match {
        case "Double" =>
          values.addProperty(attrName, attrValu.doubleValue())

        case "Float" =>
          values.addProperty(attrName, attrValu.floatValue())

        case "Integer" =>
          values.addProperty(attrName, attrValu.intValue())

        case "Long" =>
          values.addProperty(attrName, attrValu.longValue())

        case "Short" =>
          values.addProperty(attrName, attrValu.shortValue())
        case _ =>
          throw new Exception(s"Data type `$attrType` is not supported.")
      }
    })

    payload.add("values", values)
    mqttPublish(payload.toString)

  }

  private def mqttConnect:MqttClient = {

    val brokerUrl = options.getBrokerUrl
    val clientId  = options.getClientId

    val persistence = options.getPersistence
    val mqttClient = new MqttClient(brokerUrl, clientId, persistence)

    val mqttOptions = options.getMqttOptions
    mqttClient.connect(mqttOptions)

    if (mqttClient.isConnected) {
      info(s"Producer is connected to ThingsBoard.")
      mqttClient

    }
    else {
      null

    }

  }
  /**
   * This is the basic method to send a certain
   * MQTT message in a ThingsBoard compliant
   * format to the server
   */
  private def mqttPublish(mqttPayload:String):Unit = {

    try {

      val mqttMessage = new MqttMessage(mqttPayload.getBytes("UTF-8"))
      mqttMessage.setQos(options.getQos)

      if (mqttClient == null)
        throw new Exception("No Mqtt client initialized.")

      mqttClient.publish(options.getMqttTopic, mqttMessage)

    } catch {
      case t:MqttException =>

        val reasonCode = t.getReasonCode
        val cause = t.getCause

        val message = t.getMessage
        error(s"Mqtt publishing failed: Reason=$reasonCode, Cause=$cause, Message=$message")

    }

  }

}
