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
 * Payload Decoder for Milesight AM300 Series
 */
object AM300 extends BaseDecoder {

  override def decode(bytes: Array[Int]): JsonObject = {
    /*
     * Milesight ambience monitoring sensor (Indoor air quality sensor)
     *
     * Source: https://github.com/Milesight-IoT/SensorDecoders/tree/master/AM100_Series
     *
     * --------------------- Payload Definition ---------------------
     *
     *                   [channel_id] [channel_type] [channel_value]
     *
     * 01: battery        -> 0x01         0x75          [1byte ] Unit: %
     * 03: temperature    -> 0x03         0x67          [2bytes] Unit: °C (℉)
     * 04: humidity       -> 0x04         0x68          [1byte ] Unit: %RH
     * 05: PIR            -> 0x05         0x00          [1byte ] Unit:
     * 06: light_level    -> 0x06         0xCB          [1byte ] Unit:
     * 07: CO2            -> 0x07         0x7D          [2bytes] Unit: ppm
     * 08: tVOC           -> 0x08         0x7D          [2bytes] Unit:
     * 09: pressure       -> 0x09         0x73          [2bytes] Unit: hPa
     * --------------------------------------------------------- AM307
     *
     * 0A: HCHO           -> 0x0A         0x7D          [2bytes] Unit: mg/m3
     * 0B: PM2.5          -> 0x0B         0x7D          [2bytes] Unit: ug/m3
     * 0C: PM10           -> 0x0C         0x7D          [2bytes] Unit: ug/m3
     * 0D: O3             -> 0x0D         0x7D          [2bytes] Unit: ppm
     * 0E: beep           -> 0x0E         0x01          [1byte ] Unit:
     * --------------------------------------------------------- AM319
     */
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
        // PIR
        else if (channel_id == 0x05 && channel_type == 0x00) {
          i += 1
          /*
           * The value = 1 specifies "trigger" or "idle"
           */
          val pir = bytes(i)
          decoded.addProperty("pir", pir)
        }
        // LIGHT
        else if (channel_id == 0x06 && channel_type == 0xCB) {
          i += 1
          val light_level = bytes(i)
          decoded.addProperty("light_level", light_level)
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
        }
        // HCHO
        else if (channel_id == 0x0A && channel_type == 0x7D) {
          i += 1
          val hcho = readUInt16LE(bytes.slice(i, i + 2)).toDouble / 100
          decoded.addProperty("hcho", hcho)
          i += 1
        }
        // PM2.5
        else if (channel_id == 0x0B && channel_type == 0x7D) {
          i += 1
          val pm2_5 = readUInt16LE(bytes.slice(i, i + 2))
          decoded.addProperty("pm2_5", pm2_5)
          i += 1
        }
        // PM10
        else if (channel_id == 0x0C && channel_type == 0x7D) {
          i += 1
          val pm10 = readUInt16LE(bytes.slice(i, i + 2))
          decoded.addProperty("pm10", pm10)
          i += 1
        }
        // O3
        else if (channel_id == 0x0D && channel_type == 0x7D) {
          i += 1
          val o3 = readUInt16LE(bytes.slice(i, i + 2)).toDouble / 100
          decoded.addProperty("o3", o3)
          i += 1
        }
        // BEEP
        else if (channel_id == 0x0E && channel_type == 0x01) {
          i += 1
          val beep = bytes(i)
          decoded.addProperty("beep", beep)
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
      PIR,
      LIGHT_LEVEL,
      CO2,
      TVOC,
      PRESSURE,
      HCHO,
      PM2_5,
      PM10,
      O3,
      BEEP
    )
  }
}
