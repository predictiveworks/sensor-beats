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

object LMDS200 extends BaseDecoder {
  /*
   * The Dragino LMDS200 is a LoRaWAN Microwave Radar distance sensor.
   * It uses 24Ghz Microwave to detect the distance between the sensor
   * and different objects.
   *
   * Compare vs the ultrasonic or Lidar measurement method, Microwave Radar
   * is more reliable for condensation / dusty environments. It can sense
   * the correct distance even there is water or thick dust on top of the
   * sensor.
   *
   * The LMDS200 can be applied to scenarios such as horizontal distance measurement,
   * parking management system, object proximity and presence detection, intelligent
   * trash can management system, robot obstacle avoidance, automatic control, sewer, etc.
   *
   * LMDS200 can measure two distances: the closest object and the next object behind
   * the closest one.
   */
  override def decode(bytes: Array[Int], fport: Int): JsonObject = {

    val decoded = new JsonObject

    fport match {
      case 2 =>

        // BAT
        val battery = (bytes(0) << 8 | bytes(1)).toDouble / 1000
        decoded.addProperty("Bat", battery)

        // dis1
        val dis1 = bytes(2) << 8 | bytes(3)
        decoded.addProperty("dis1", dis1)

        // dis2
        val dis2 = bytes(4) << 8 | bytes(5)
        decoded.addProperty("dis2", dis2)

        // DALARM_count
        val dalarm_count = (bytes(6)>>2) & 0x3F
        decoded.addProperty("DALARM_count", dalarm_count)

        // Interrupt_alarm
        val interrupt_alarm = bytes(6) & 0x01
        decoded.addProperty("Interrupt_alarm", interrupt_alarm)

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
      dis1,
      dis2
    )
  }
}
