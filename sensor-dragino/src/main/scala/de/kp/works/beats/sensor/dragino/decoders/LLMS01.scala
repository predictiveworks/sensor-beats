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

object LLMS01 extends BaseDecoder {
  /*
   * The Dragino LLMS01 is a LoRaWAN Leaf Moisture Sensor
   * for IoT of Agriculture. It is designed to measure the
   * leaf moisture and temperature, to analyze the leaf status
   * such as : watering, moisturizing, dew, frozen.
   */
  override def decode(bytes: Array[Int], fport: Int): JsonObject = {

    val decoded = new JsonObject

    fport match {
      case 2 =>

        // BAT
        val battery = ((bytes(0) << 8 | bytes(1)) & 0x3FFF).toDouble / 1000
        decoded.addProperty("Bat", battery)

        // TempC_DS18B20
        val temperature = {
          var value = bytes(2) << 8 | bytes(3)
          if ((bytes(2) & 0x80) == 1) value |= 0xFFFF0000
          value.toDouble / 10
        }
        decoded.addProperty("TempC_DS18B20", temperature)

        // Leaf_Moisture
        val leaf_moisture = (bytes(4) << 8 | bytes(5)).toDouble / 10
        decoded.addProperty("Leaf_Moisture", leaf_moisture)

        // Leaf_Temperature
        val leaf_temperature = {
          val value = bytes(6) << 8 | bytes(7)
          if ((value & 0x8000) >> 15 == 0)
            value.toDouble / 10

          else if ((value & 0x8000) >> 15 == 1)
            (value - 0xFFFF).toDouble / 10

          else 0D
        }

        decoded.addProperty("Leaf_Temperature", leaf_temperature)

        // Interrupt_flag
        val flag = bytes(8)
        decoded.addProperty("Interrupt_flag", flag)

        // Message_type
        val message_type = bytes(10)
        decoded.addProperty("Message_type", message_type)

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
      Leaf_Moisture,
      Leaf_Temperature
    )
  }
}
