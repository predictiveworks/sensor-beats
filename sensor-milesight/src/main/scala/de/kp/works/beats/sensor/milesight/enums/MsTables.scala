package de.kp.works.beats.sensor.milesight.enums

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

import de.kp.works.beats.sensor.milesight.enums.MsProducts._

object MsTables extends Enumeration {
  type MsTable = Value
  /**
   * Monitoring tables
   */
  val BATTERY:MsTables.Value     = Value(1, "battery")
  val CO2:MsTables.Value         = Value(2, "co2")
  val DISTANCE:MsTables.Value    = Value(3, "distance")
  val HUMIDITY:MsTables.Value    = Value(4, "humidity")
  val PRESSURE:MsTables.Value    = Value(5, "humidity")
  val TEMPERATURE:MsTables.Value = Value(6, "pressure")

  def getTables(product:MsProducts.Value):Seq[String] = {
    product match {
      /*
       * Milesight humidity & temperature
       * indoor sensor
       */
      case EM300_TH =>
        Seq(
          MsTables.BATTERY.toString,
          MsTables.HUMIDITY.toString,
          MsTables.TEMPERATURE.toString)
      /*
       * Milesight CO2 sensor
       */
      case EM500_CO2 =>
        Seq(
          MsTables.BATTERY.toString,
          MsTables.CO2.toString,
          MsTables.HUMIDITY.toString,
          MsTables.PRESSURE.toString,
          MsTables.TEMPERATURE.toString)
      /*
       * Milesight ultrasonic outdoor level
       * sensor
       */
      case EM500_UDL =>
        Seq(
          MsTables.BATTERY.toString,
          MsTables.DISTANCE.toString)

      case UC500 =>
        // TODO
        Seq.empty[String]
    }
  }
}

