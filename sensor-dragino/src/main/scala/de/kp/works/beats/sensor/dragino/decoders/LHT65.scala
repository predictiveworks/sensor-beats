package de.kp.works.beats.sensor.dragino.decoders

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

object LHT65 extends BaseDecoder {
  /*
   * The Dragino LHT65 includes a built-in SHT20 Temperature & Humidity
   * sensor and has an external sensor connector to connect to external
   * sensors such as Temperature Sensor, Soil Moisture Sensor, Tilting Sensor
   * etc.
   */
  override def decode(bytes: Array[Int], fport: Int): JsonObject = {

    val decoded = new JsonObject

    fport match {
      case 2 =>
        /*
         * Compute common control parameter; `ext` indicates
         * the external sensor
         */
        val connect = (bytes(6) & 0x80) >> 7
        val ext     = bytes(6) & 0x0F
        val status  = (bytes(6) & 0x40) >> 6

        if ((status == 0) && (bytes.length == 11)) {

          if (ext == 0x09) {
            // TempC_DS
            val temp_ds = ((bytes(0) << 24 >> 16) | bytes(1)).toDouble / 100
            decoded.addProperty("TempC_DS", temp_ds)

            // Bat_status
            val bat_status = bytes(4) >> 6
            decoded.addProperty("Bat_status", bat_status)

          }
          else {
            // BatV
            val bat = (((bytes(0) << 8) | bytes(1)) & 0x3FFF).toDouble / 1000
            decoded.addProperty("BatV", bat)

            // Bat_status
            val bat_status = bytes(0) >> 6
            decoded.addProperty("Bat_status", bat_status)
          }
          /*
           * Assign the values of the built-in sensors
           */
          if (ext != 0x0f) {
            // TempC_SHT
            val temperature = ((bytes(2) << 24 >> 16) | bytes(3)).toDouble / 100
            decoded.addProperty("TempC_SHT", temperature)

            // Hum_SHT
            val humidity = (((bytes(4) << 8) | bytes(5)) & 0xFFF).toDouble / 10
            decoded.addProperty("Hum_SHT", humidity)
          }

          if (connect == 1) {
            decoded.addProperty("No_connect", "Sensor no connection")
          }

          if (ext == 0) {
            decoded.addProperty("Ext_sensor", "No external sensor")
          }
          else if (ext == 1) {
            decoded.addProperty("Ext_sensor", "Temperature Sensor")

            // TempC_DS
            val temp_ds = ((bytes(7) << 24 >> 16) | bytes(8)).toDouble / 100
            decoded.addProperty("TempC_DS", temp_ds)
          }
          else if (ext == 4) {
            decoded.addProperty("Work_mode", "Interrupt Sensor send")

            // Exti_pin_level: "High":"Low"
            val level = if (bytes(7) == 1) 1D else 0D
            decoded.addProperty("Exti_pin_level", level)

            // Exti_status: "True":"False"
            val status = if (bytes(8) == 1) 1D else 0D
            decoded.addProperty("Exti_status", status)

          }
          else if (ext == 5) {
            decoded.addProperty("Work_mode", "Illumination Sensor")

            // ILL_lx
            val value = (bytes(7) << 8) | bytes(8)
            decoded.addProperty("ILL_lx", value)
          }
          else if (ext == 6) {
            decoded.addProperty("Work_mode", "ADC Sensor")

            // ADC_V
            val value = ((bytes(7) << 8) | bytes(8)).toDouble / 1000
            decoded.addProperty("ADC_V", value)
          }
          else if (ext == 7) {
            decoded.addProperty("Work_mode", "Interrupt Sensor count")

            // Exit_count
            val count = (bytes(7) << 8) | bytes(8)
            decoded.addProperty("Exit_count", count)
          }
          else if (ext == 8) {
            decoded.addProperty("Work_mode", "Interrupt Sensor count")

            // Exit_count
            val count = (bytes(7) << 24) | (bytes(8) << 16) | (bytes(9) << 8) | bytes(10)
            decoded.addProperty("Exit_count", count)
          }
          else if (ext == 9) {
            decoded.addProperty("Work_mode", "DS18B20 & timestamp")

            // Systimestamp
            val timestamp = (bytes(7) << 24) | (bytes(8) << 16) | (bytes(9) << 8) | bytes(10)
            decoded.addProperty("Systimestamp", timestamp)
          }
          else if (ext == 15) {

            decoded.addProperty("Work_mode", "DS18B20ID")
            val id = Seq(
              readString(bytes(2)),
              readString(bytes(3)),
              readString(bytes(4)),
              readString(bytes(5)),
              readString(bytes(7)),
              readString(bytes(8)),
              readString(bytes(9)),
              readString(bytes(10))).mkString.trim

            decoded.addProperty("ID", id)

          }

        }

      case _ =>
        throw new Exception(s"Unknown `fport` = $fport detected.")

    }
    /*
     * Remove fields fields from decoded
     * object
     */
    val removables = Seq("No_connect", "Ext_sensor", "Work_mode", "ID")
    removables.foreach(key =>
      if (decoded.has(key))decoded.remove(key))

    val result = new JsonObject
    result.add("data", decoded)

    result

  }

  override def fields: Seq[String] = Seq.empty[String]

}
