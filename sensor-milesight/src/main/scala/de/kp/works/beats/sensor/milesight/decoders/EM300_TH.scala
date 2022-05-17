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
import scala.util.control.Breaks._

/**
 * Payload Decoder for product EM300-TH
 */
object EM300_TH extends BaseDecoder {
  /*
   * Milesight environment sensor EM300-TH-868M
   *
   * Source: https://github.com/Milesight-IoT/SensorDecoders/tree/master/EM300_Series/EM300-TH
   *
   * --------------------- Payload Definition ---------------------
   *
   *                  [channel_id] [channel_type]   [channel_value]
   *
   * 01: battery      -> 0x01         0x75          [1byte ] Unit: %
   * 03: temperature  -> 0x03         0x67          [2bytes] Unit: °C (℉)
   * 04: humidity     -> 0x04         0x68          [1byte ] Unit: %RH
   *
   * ------------------------------------------------------ EM300-TH
   *
   * Sample: 01 75 5C 03 67 34 01 04 68 65
   *
   * {
   * "battery": 92,
   * "humidity": 50.5,
   * "temperature": 30.8
   * }
   *
   * The battery is an optional parameter; the parameter names
   * are directly used as field names for further processing.
   */
  def decode(bytes:Array[Int]):JsonObject = {

    val decoded = new JsonObject

    var i = -1;
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

        } else {
          break
        }

      }
    }

   decoded

  }

}
