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

object LHT52 extends BaseDecoder {
  /*
   * The Dragino LHT52 includes a built-in Temperature & Humidity sensor
   * and has a USB Type-C sensor connector to connect to external sensors
   * such as an external temperature sensor.
   *
   * It targets professional wireless sensor network applications such as
   * food service, smart metering, smart cities, building automation, and
   * so on.
   */
  override def decode(bytes: Array[Int], fport: Int): JsonObject = {

    val decoded = new JsonObject

    fport match {
      case 2 =>

        if (bytes.length == 11) {

          // TempC_SHT
          val temperature = ((bytes(0) << 24 >>16) | bytes(1)).toDouble / 100
          decoded.addProperty("TempC_SHT", temperature)

          // Hum_SHT
          val humidity = ((bytes(2) << 24 >> 16) | bytes(3)).toDouble / 10
          decoded.addProperty("Hum_SHT", humidity)

          // TempC_DS
          val temp_ds = ((bytes(4) << 24 >> 16) | bytes(5)).toDouble /100
          decoded.addProperty("TempC_DS", temp_ds)

          // Ext
          val ext = bytes(6)
          decoded.addProperty("Ext", ext)

          // Systimestamp
          val timestamp = (bytes(7) << 24) | (bytes(8) << 16) | (bytes(9) << 8) | bytes(10)
          decoded.addProperty("Systimestamp", timestamp)

        }

      case _ =>
        throw new Exception(s"Unknown `fport` = $fport detected.")

    }

    val result = new JsonObject
    result.add("data", decoded)

    result

  }

  override def fields: Seq[String] = {
    Seq(
      TempC_SHT,
      Hum_SHT,
      TempC_DS)
  }
}
