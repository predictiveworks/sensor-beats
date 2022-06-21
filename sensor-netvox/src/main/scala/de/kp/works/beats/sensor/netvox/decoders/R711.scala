package de.kp.works.beats.sensor.netvox.decoders

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

object R711 extends BaseDecoder {
  /*
   * R711 is used to detect the indoor temperature
   * and humidity.
   */
  override def decode(bytes: Array[Int], fport: Int): JsonObject = {

    val decoded = new JsonObject

    fport match {
      case 6 =>

        if (bytes(2) == 0x00) {

          // Device
          val device = getDeviceName(bytes(1))
          decoded.addProperty("Device", device)

          // SWver
          val sw_version = bytes(3) / 10
          decoded.addProperty("SWver", sw_version)

          // HWver
          val hw_version = bytes(4)
          decoded.addProperty("HWver", hw_version)

          // Datecode
          val datacode =
            padLeft(Integer.toString(bytes(5),16),2) +
            padLeft(Integer.toString(bytes(6),16),2) +
            padLeft(Integer.toString(bytes(7),16),2) +
            padLeft(Integer.toString(bytes(8),16),2)

          decoded.addProperty("Datecode", datacode)

        }
        else {

          // Device
          val device = getDeviceName(bytes(1))
          decoded.addProperty("Device", device)

          // Volt
          if ((bytes(3) & 0x80) == 1) {
            val volt = (bytes(3) & 0x7F).toDouble / 10
            decoded.addProperty("Volt", volt)
          }
          else {
            val volt = bytes(3).toDouble / 10
            decoded.addProperty("Volt", volt)
          }

          // Temp
          val temperature = if ((bytes(4) & 0x80) == 1) {
            val num = (bytes(4) << 8) | bytes(5)
            (-1) * (0x10000 - num).toDouble / 100
          }
          else {
            ((bytes(4) << 8) | bytes(5)).toDouble / 100
          }

          decoded.addProperty("Temp", temperature)

          // Humi
          val humidity = ((bytes(6) << 8) | bytes(7)).toDouble / 100
          decoded.addProperty("Humi", humidity)
        }

      case 7 =>

        // Device
        val device = getDeviceName(bytes(1))
        decoded.addProperty("Device", device)

        if (bytes(0) == 0x81)  {

          // Cmd
          val cmd = getCmd(bytes(0))
          decoded.addProperty("Cmd", cmd)

          // Status :: Success = 1 | Failure = 0
          val status = if (bytes(2) == 0x00) 1 else 0
          decoded.addProperty("Status", status)

        }
        else if (bytes(0) == 0x82) {

          // Cmd
          val cmd = getCmd(bytes(0))
          decoded.addProperty("Cmd", cmd)

          // MinTime
          val minTime = (bytes(2) << 8) | bytes(3)
          decoded.addProperty("MinTime", minTime)

          // MaxTime
          val maxTime = (bytes(4) << 8) | bytes(5)
          decoded.addProperty("MaxTime", maxTime)

          // BatteryChange
          val batteryCharge = bytes(6).toDouble / 10
          decoded.addProperty("BatteryChange", batteryCharge)

          // TempChange
          val tempChange = ((bytes(7) << 8) | bytes(8)).toDouble / 100
          decoded.addProperty("TempChange", tempChange)

          // HumiChange
          val humiChange = ((bytes(9) << 8) | bytes(10)).toDouble / 100
          decoded.addProperty("HumiChange", humiChange)

        }

      case _ =>
        throw new Exception(s"Unknown `fport` = $fport detected.")

    }
    val result = new JsonObject
    result.add("data", decoded)

    result

  }
  /**
   * Dependent on the `fport` and the combination
   * of fields, there are different decoding
   * results.
   */
  override def fields: Seq[String] = Seq.empty[String]

}
