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
 * Payload Decoder for Milesight AM100 Series,
 * which covers AM104 & AM107
 */
object AM100 extends BaseDecoder {
  /*
   * Milesight ambience monitoring sensor (Indoor air quality sensor)
   *
   * Source: https://github.com/Milesight-IoT/SensorDecoders/tree/master/AM100_Series
   *
   * --------------------- Payload Definition ---------------------
   *
   *                    [channel_id] [channel_type] [channel_value]
   * 01: battery        -> 0x01         0x75          [1byte ] Unit: %
   * 03: temperature    -> 0x03         0x67          [2bytes] Unit: °C (℉)
   * 04: humidity       -> 0x04         0x68          [1byte ] Unit: %RH
   * 05: activity       -> 0x05         0x6A          [2bytes] Unit:
   * 06: illumination   -> 0x06         0x65          [6bytes] Unit: lux
   * 07: CO2            -> 0x07         0x7D          [2bytes] Unit: ppm
   * 08: tVOC           -> 0x08         0x7D          [2bytes] Unit: ppb
   * 09: pressure       -> 0x09         0x73          [2bytes] Unit: hPa
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
          decoded.addProperty("battery", battery)
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
        // ACTIVITY (PIR)
        else if (channel_id == 0x05 && channel_type == 0x6A) {
          i += 1
          val activity = readInt16LE(bytes.slice(i, i + 2)).toDouble / 10
          decoded.addProperty("activity", activity)
          i += 1
        }
        // LIGHT
        else if (channel_id == 0x06 && channel_type == 0x65) {
          i += 1
          val illumination = readUInt16LE(bytes.slice(i, i + 2))
          decoded.addProperty("illumination", illumination)

          val infrared_and_visible = readUInt16LE(bytes.slice(i + 2, i + 4))
          decoded.addProperty("infrared_and_visible", infrared_and_visible)

          val infrared = readUInt16LE(bytes.slice(i + 4, i + 6))
          decoded.addProperty("infrared", infrared)
          i += 5
        }
        // CO2
        else if (channel_id == 0x07 && channel_type == 0x7D) {
          i += 1
          val co2 = readUInt16LE(bytes.slice(i, i + 2))
          decoded.addProperty("co2", co2)
          i += 1
        }
        // TVOC
        else if (channel_id == 0x08 && channel_type == 0x7D) {
          i += 1
          val tvoc = readUInt16LE(bytes.slice(i, i + 2))
          decoded.addProperty("tvoc", tvoc)
          i += 1
        }
        // PRESSURE
        else if (channel_id == 0x09 && channel_type == 0x73) {
          i += 1
          val pressure = readUInt16LE(bytes.slice(i, i + 2)).toDouble / 10
          decoded.addProperty("pressure", pressure)
          i += 1
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
      ACTIVITY,
      ILLUMINATION,
      INFRARED_AND_VISIBLE,
      INFRARED,
      CO2,
      TVOC,
      PRESSURE
    )
  }
}
