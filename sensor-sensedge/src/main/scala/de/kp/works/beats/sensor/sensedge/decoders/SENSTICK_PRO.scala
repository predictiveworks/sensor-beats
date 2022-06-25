package de.kp.works.beats.sensor.sensedge.decoders

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
import de.kp.works.beats.sensor.sensedge.enums.SeFields._

object SENSTICK_PRO extends BaseDecoder {
  /*
   * Senstick is a LoRaWAN sensor for reliable and quality environmental
   * data in harsh indoor and outdoor environments.
   */
  override def decode(bytes: Array[Int], fport: Int): JsonObject = {

    val decoded = new JsonObject

    fport match {
      case 2 =>

        // Status
        val status = bytes(0)
        decoded.addProperty("Status", status)

        // Temperature (in ° Celsius)
        val temperature = int2Double((bytes(1) << 8) + bytes(2))
        decoded.addProperty("Temperature", temperature)

        // Humidity (relative humidity in percentage)
        val humidity = ((bytes(3) << 8) + bytes(4)).toDouble / 100
        decoded.addProperty("Humidity", humidity)

        // AirPressure (hPa)
        val pressure = bytes(5)
        decoded.addProperty("AirPressure", pressure)

        // Movement (mg, g = gravitation)
        val movement = bytes(6).toDouble / 100
        decoded.addProperty("Movement", movement)

        // BatteryLevel
        val battery = (bytes(7) + 100).toDouble / 100
        decoded.addProperty("BatteryLevel", battery)

      case _ =>
        throw new Exception(s"Unknown `fport` = $fport detected.")

    }

    val result = new JsonObject
    result.add("data", decoded)

    result
  }

}
