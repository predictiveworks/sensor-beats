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
import de.kp.works.beats.sensor.dragino.enums.DoFields.{Bat, Distance}

object LDDS20 extends BaseDecoder {
  /*
   * The Dragino LDDS20 is a LoRaWAN Ultrasonic liquid level sensor.
   * It uses non contact method to measure the height of liquid in a
   * container without opening the container.
   *
   * The LDDS20 sensor is installed directly below the container to
   * detect the height of the liquid level. The non contact measurement
   *  makes the measurement safety, easier and possible for some strict
   * situation.
   *
   * LDDS20 uses ultrasonic sensing technology for distance measurement.
   * It is of high accuracy to measure various liquid such as: toxic substances,
   * strong acids, strong alkalis and various pure liquids in high-temperature and
   * high-pressure airtight containers.
   */
  override def decode(bytes: Array[Int], fport: Int): JsonObject = {

    val decoded = new JsonObject

    fport match {
      case 2 =>

        // Bat in V
        val battery = ((bytes(0) << 8 | bytes(1)) & 0x3FFF).toDouble / 1000
        decoded.addProperty("Bat", battery)

        // Distance in [mm]
        val distance = bytes(2) << 8 | bytes(3)
        if (distance == 0) {
          decoded.addProperty("Distance", -1) // No Sensor

        }
        else if (distance == 20) {
          decoded.addProperty("Distance", -2) // Invalid Reading
        }
        else
          decoded.addProperty("Distance", distance)

        // Interrupt_flag
        val flag = bytes(4)
        decoded.addProperty("Interrupt_flag", flag)

        // TempC_DS18B20
        val temperature = {
          var value = bytes(5) << 8 | bytes(6)
          if ((bytes(5) & 0x80) == 1) value |= 0xFFFF0000
          value.toDouble / 10
        }
        decoded.addProperty("TempC_DS18B20", temperature)

        // Sensor_flag
        val sensor_flag = bytes(7)
        decoded.addProperty("Sensor_flag", sensor_flag)

      case _ =>
        throw new Exception(s"Unknown `fport` = $fport detected.")

    }

    val result = new JsonObject
    result.add("data", decoded)

    result

  }

  override def fields: Seq[String] = Seq(
    Bat,
    Distance
  )
}
