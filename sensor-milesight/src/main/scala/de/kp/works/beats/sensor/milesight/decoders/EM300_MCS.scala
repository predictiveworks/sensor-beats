package de.kp.works.beats.sensor.milesight.decoders

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
import de.kp.works.beats.sensor.milesight.enums.MsFields._

import scala.util.control.Breaks.{break, breakable}
/**
 * Payload Decoder for Milesight EM300-MCS
 */
object EM300_MCS extends BaseDecoder {
  /*
   * Milesight magnetic contact switch sensor
   *
   * Source: https://github.com/Milesight-IoT/SensorDecoders/tree/master/EM300_Series/EM300-MCS
   *
   * --------------------- Payload Definition ---------------------
   *
   *                  [channel_id] [channel_type] [channel_value]
   * 01: battery      -> 0x01         0x75          [1byte ] Unit: %
   * 03: temperature  -> 0x03         0x67          [2bytes] Unit: °C (℉)
   * 04: humidity     -> 0x04         0x68          [1byte ] Unit: %RH
   * 06: door         -> 0x06         0x00          [1byte ] Unit: N/A
   * ---------------------------------------------------- EM300-MCS
   */
  override def decode(bytes: Array[Int]): JsonObject = {

    val decoded = new JsonObject

    var i = -1
    breakable {
      while (i < bytes.length - 1) {

        // CHANNEL
        i += 1
        val channel_id = bytes(i)

        i += 1
        val channel_type = bytes(i)

        // BATTERY
        if (channel_id == 0x01 && channel_type == 0x75) {
          i += 1
          val battery = bytes(i)
          decoded.addProperty("battery",  battery)
        }
        // TEMPERATURE (°C)
        else if (channel_id == 0x03 && channel_type == 0x67) {
          i += 1
          val temperature = readInt16LE(bytes.slice(i, i + 2)).toDouble / 10
          decoded.addProperty("temperature", temperature)
          i += 1
        }
        // HUMIDITY
        else if (channel_id == 0x04 && channel_type == 0x68) {
          i += 1
          val humidity = bytes(i).toDouble / 2
          decoded.addProperty("humidity", humidity)
        }
        // DOOR
        else if (channel_id == 0x06 && channel_type == 0x00) {
          i += 1
          val door = bytes(i)
          decoded.addProperty("door", door)
        } else {
          break
        }

      }
    }

    decoded

  }

  override def fields: Seq[String] = {
    Seq(
      BATTERY,
      TEMPERATURE,
      HUMIDITY,
      DOOR
    )
  }
}
