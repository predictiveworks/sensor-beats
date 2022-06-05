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

object LDDS04 extends BaseDecoder {
  /*
   * The Dragino LDDS04 Distance Sensor is capable to add up
   * to four Ultrasonic Sensors to measure four distances at
   * the same time.
   *
   * The LDDS04 can be applied to scenarios such as horizontal
   * distance measurement, liquid level measurement, parking
   * management system, object proximity and presence detection,
   * intelligent trash can management system, robot obstacle avoidance,
   * automatic control, sewer, bottom water level monitoring, etc.
   */
  override def decode(bytes: Array[Int], fport: Int): JsonObject = {

    val decoded = new JsonObject

    fport match {
      case 2 =>

        if(!((bytes(0) == 0x03) && (bytes(10)==0x02))) {

          // BatV
          val battery = ((bytes(0) << 8 | bytes(1)) & 0x3FFF).toDouble / 1000
          decoded.addProperty("BatV", battery)

          // EXTI_Trigger; the decoding transforms to "TRUE" or "FALSE"
          val trigger = bytes(0) & 0x80
          decoded.addProperty("EXTI_Trigger", trigger)

          // distance1_cm
          val distance1_cm = (bytes(2) << 8 | bytes(3)).toDouble / 10
          decoded.addProperty("distance1_cm", distance1_cm)

          // distance2_cm
          val distance2_cm = (bytes(4) << 8 | bytes(5)).toDouble / 10
          decoded.addProperty("distance2_cm", distance2_cm)

          // distance3_cm
          val distance3_cm = (bytes(6) << 8 | bytes(7)).toDouble / 10
          decoded.addProperty("distance3_cm", distance3_cm)

          // distance4_cm
          val distance4_cm = (bytes(8) << 8 | bytes(9)).toDouble / 10
          decoded.addProperty("distance4_cm", distance4_cm)

          // mes_type
          val mes_type= bytes(10)
          decoded.addProperty("mes_type", mes_type)

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
      BatV,
      distance1_cm,
      distance2_cm,
      distance3_cm,
      distance4_cm)
  }
}
