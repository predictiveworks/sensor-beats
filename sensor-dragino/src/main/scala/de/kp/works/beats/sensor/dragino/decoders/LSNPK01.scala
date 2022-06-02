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

object LSNPK01 extends BaseDecoder {
  /*
   * The Dragino LSNPK01 is a LoRaWAN Soil NPK Sensor for
   * IoT of Agriculture. It is designed to measure the Soil
   * Fertility Nutrient and provide a plant growing reference.
   *
   * LSNPK01 detects soil's Nitrogen, Phosphorus, and Potassium
   * using TDR method.
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

        // N_SOIL, Unit: mg/kg
        val n_soil = bytes(4) << 8 | bytes(5)
        decoded.addProperty("N_SOIL", n_soil)

        // P_SOIL, Unit: mg/kg
        val p_soil = bytes(6) << 8 | bytes(7)
        decoded.addProperty("P_SOIL", p_soil)

        // K_SOIL, Unit: mg/kg
        val k_soil = bytes(8) << 8 | bytes(9)
        decoded.addProperty("K_SOIL", k_soil)

        // Interrupt_flag
        val flag =  bytes(10) & 0x0F
        decoded.addProperty("Interrupt_flag", flag)

        // Message_type
        val message_type = bytes(10) >> 4
        decoded.addProperty("Message_type", message_type)

      case _ =>
        throw new Exception(s"Unknown `fport` = $fport detected.")

    }
    val result = new JsonObject
    result.add("data", decoded)

    result

  }

  override def fields: Seq[String] = {
    Seq(
      Bat,
      N_SOIL,
      P_SOIL,
      K_SOIL
    )
  }
}
