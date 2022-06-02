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
import de.kp.works.beats.sensor.dragino.enums.DoFields._

object LSE01 extends BaseDecoder {
  /*
   * The Dragino LSE01 is a LoRaWAN Soil Moisture & EC Sensor
   * for IoT of Agriculture. It detects Soil Moisture, Soil
   * Temperature and Soil Conductivity.
   *
   * It is designed to measure the soil moisture of saline-alkali
   * soil and loamy soil. The soil sensor uses the FDR method to
   * calculate the soil moisture with the compensation from soil
   * temperature and conductivity.
   *
   * It also has been calibrated for Mineral soil type.
   *
   */
  override def decode(bytes: Array[Int], fport: Int): JsonObject = {

    val decoded = new JsonObject

    fport match {
      case 2 =>

        // BAT V
        val battery = ((bytes(0) << 8 | bytes(1)) & 0x3FFF).toDouble / 1000
        decoded.addProperty("Bat", battery)

        // TempC_DS18B20
        val temperature = {
          var value = bytes(2) << 8 | bytes(3)
          if ((bytes(2) & 0x80) == 1) value |= 0xFFFF0000
          value.toDouble / 10
        }
        decoded.addProperty("TempC_DS18B20", temperature)

        // water_SOIL, Humidity, units:%
        val water_soil = (bytes(4) << 8 | bytes(5)).toDouble / 100
        decoded.addProperty("water_SOIL", water_soil)

        // temp_SOIL, temperature, units:°C
        val temp_soil = {
          val value = bytes(6) << 8 | bytes(7)
          if ((value & 0x8000) >> 15 == 0)
            value.toDouble / 100

          else if ((value & 0x8000) >> 15 == 1)
            (value - 0xFFFF).toDouble / 100

          else 0D
        }

        decoded.addProperty("temp_SOIL", temp_soil)

        // conduct_SOIL, conductivity, units:uS/cm
        val conduct_soil = bytes(8) << 8 | bytes(9)
        decoded.addProperty("conduct_SOIL", conduct_soil)

      case _ =>
        throw new Exception(s"Unknown `fport` = $fport detected.")

    }
    val result = new JsonObject
    result.add("data", decoded)

    result

  }
  /**
   * This method provides the relevant
   * measurements of the sensor
   */
  override def fields: Seq[String] = {
    Seq(
      Bat,
      water_SOIL,
      temp_SOIL,
      conduct_SOIL)
  }
}
