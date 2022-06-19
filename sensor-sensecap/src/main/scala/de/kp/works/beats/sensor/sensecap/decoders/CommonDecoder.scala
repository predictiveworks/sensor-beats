package de.kp.works.beats.sensor.sensecap.decoders

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

import com.google.gson.{JsonArray, JsonObject}
import scala.collection.JavaConversions.iterableAsScalaIterable

abstract class CommonDecoder extends BaseDecoder {

  override def decode(bytes: Array[Int]): JsonObject = {
    /*
     * SenseCAP Wireless CO2 sensor (sample)
     *
     * {
     *    "err": 0,
     *    "messages": [
     *      {
     *        "measurementId": 4100,
     *        "measurementValue": 364,
     *        "type": "report_telemetry"
     *      },
     *      ...
     *    ],
     *    "payload": "010410E08D05009802",
     *    "valid": true
     * }
     */

    val decoded = new JsonObject
    /*
     * Re-transform bytes into hexstring
     * and prepare the decoded object
     */
    val hexstring = bytes2HexString(bytes).toUpperCase

    decoded.addProperty("err", 0)
    decoded.addProperty("valid", true)

    decoded.addProperty("payload", hexstring)
    decoded.add("messages", new JsonArray)

    /* STEP #1: CRC check */
    if (!crc16check(hexstring)) {

      decoded.remove("valid")
      decoded.addProperty("valid", false)

      decoded.remove("err")
      decoded.addProperty("err", -1)

      return decoded

    }

    /* STEP #2: Length Check */
    if ((((hexstring.length / 2) - 2) % 7) != 0) {

      decoded.remove("valid")
      decoded.addProperty("valid", false)

      decoded.remove("err")
      decoded.addProperty("err", -1)

      return decoded

    }

    /* Cache sensor id */
    var sensorEuiLowBytes:String = null
    var sensorEuiHighBytes:String = null

    val frames = divideBy7Bytes(hexstring)
    frames.foreach(frame => {

      /* Extract key parameters */
      val id = strTo10SysNub(frame.substring(2, 6))
      println(id)
      val value = frame.substring(6, 14)

      val realval =
        if (isSpecialDataId(id)) {
          ttnDataSpecialFormat(id, value)
        }
        else {
          /* Method returns a Double */
          ttnDataFormat(value)
        }

      if (checkIdIsMeasureUpload(id)) {
        /* Telemetry  */
        val message = new JsonObject
        message.addProperty("type", "report_telemetry")

        message.addProperty("measurementId", id)
        message.addProperty("measurementValue", realval.toDouble)

        addMessage(decoded, message)
      }
      else if (isSpecialDataId(id) || (id == 5) || (id == 6)) {

        id match {
          case 0x00 =>
            /* Node version */
            val (hardware, software) = sensorAttrForVersion(realval)

            val message = new JsonObject
            message.addProperty("type", "upload_version")

            message.addProperty("hardwareVersion", hardware)
            message.addProperty("softwareVersion", software)

            addMessage(decoded, message)

          case 1 =>
            /* Sensor version */

          case 2 =>
            /* Sensor eui, low bytes */
            sensorEuiLowBytes = realval

          case 3 =>
            /* Sensor eui, high bytes */
            sensorEuiHighBytes = realval

          case 7 =>
            /* Battery power && interval */
            val tokens = realval.split(",")

            val upload_battery = new JsonObject
            upload_battery.addProperty("type", "upload_battery")
            upload_battery.addProperty("battery", tokens(0))

            addMessage(decoded, upload_battery)

            val upload_interval = new JsonObject
            upload_interval.addProperty("type", "upload_interval")
            upload_interval.addProperty("interval", tokens(1).toInt * 60)

            addMessage(decoded, upload_interval)

          case 0x120 =>
            /* Remove sensor */
            val message = new JsonObject
            message.addProperty("type", "report_remove_sensor")
            message.addProperty("channel", 1)

            addMessage(decoded, message)

          case _ => /* Do nothing */

        }
      }
      else {

        val message = new JsonObject
        message.addProperty("type", "unknown_message")

        message.addProperty("dataID", id)
        message.addProperty("dataValue", value)

        addMessage(decoded, message)

      }

    })

    if (sensorEuiHighBytes != null && sensorEuiLowBytes != null) {

      val message = new JsonObject
      message.addProperty("type", "upload_sensor_id")
      message.addProperty("channel", 1)

      val sensorId = (sensorEuiHighBytes + sensorEuiLowBytes).toUpperCase
      message.addProperty("sensorId", sensorId)
      /*
       * In this case the sensorId message is added
       * as the first message
       */
      val messages =
        List(message) ++ decoded.remove("messages")
        .getAsJsonArray
        .toList

      val newMessages = new JsonArray
      messages.foreach(newMessages.add)

      decoded.add("messages", newMessages)

    }

    val result = new JsonObject
    result.add("data", decoded)

    result

  }

  private def addMessage(decoded:JsonObject, message:JsonObject):Unit = {

    val messages = decoded.remove("messages")
      .getAsJsonArray

    messages.add(message)
    decoded.add("messages", messages)

  }
}
