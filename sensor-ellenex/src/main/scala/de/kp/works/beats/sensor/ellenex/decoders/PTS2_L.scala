package de.kp.works.beats.sensor.ellenex.decoders

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
import de.kp.works.beats.sensor.ellenex.enums.ExFields._

/**
 * Payload Decoder for Ellenex PTS2_L:
 *
 * LoRaWAN low power standard pressure transmitter
 * for liquid and gas media compatible with stainless
 * steel (water, air, diesel, oil)
 *
 * https://github.com/TheThingsNetwork/lorawan-devices/tree/master/vendor/ellenex
 */
object PTS2_L extends BaseDecoder {
  /*
   * APPLICATIONS:
   *
   * - Pump Performance
   * - Water Pipeline Pressure
   * - Pressurised System Monitoring
   * - Chiller and Cooling System
   * - Hydraulic and Pneumatic Systems
   * - Liquid Storage Level
   * - Spraying System
   * - Air Compressor Monitoring
   */
  override def decode(bytes: Array[Int], fport:Int): JsonObject = {

    if (fport == 1) {
      /*
       * Throw an error if length of Bytes is not 8
       */
      if (bytes.length != 8) {
        throw new Exception(s"Invalid uplink payload: length is not 8 bytes.")
      }
      val pressure = readHex2bytes(bytes(3), bytes(4)).toDouble
      val battery = bytes(7).toDouble / 10

      val decoded = new JsonObject
      // batteryVoltage = VOLTAGE
      decoded.addProperty("batteryVoltage", battery)

      // pressure = BAR
      decoded.addProperty("pressure", pressure)

      decoded

    }
    else
      throw new Exception(s"Please use `fport` = 1")

  }

  override def fields: Seq[String] = {
    Seq(
      batteryVoltage,
      pressure
    )

  }
}
