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

object EM500_UDL extends BaseDecoder {
  /*
   * Milesight ultrasonic level sensor
   *
   * Source: https://github.com/Milesight-IoT/SensorDecoders/tree/master/EM500_Series/EM500-UDL
   *
   * --------------------- Payload Definition ---------------------
   *
   *                  [channel_id] [channel_type]   [channel_value]
   *
   * 01: battery      -> 0x01       0x75            [1 byte]  Unit: %
   * 03: distance     -> 0x03       0x82            [2 bytes] Unit: m
   *
   * ------------------------------------------------------ EM500-UDL
   *
   * Sample: 01 75 5A 03 82 1E 00
   *
   * {
   *   "battery": 90,
   *   "distance": 30
   * }
   *
   * The battery is an optional parameter; the parameter names
   * are directly used as field names for further processing.
   *
   */
  def decode(bytes:Array[Int]):JsonObject = {

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
        // DISTANCE
        else if (channel_id == 0x03 && channel_type == 0x82) {
          i += 1
          val distance = readInt16LE(bytes.slice(i, i + 2)).toDouble
          decoded.addProperty("distance", distance)
          i += 1
        }
        else {
          break
        }
      }
    }

    decoded
  }

}
