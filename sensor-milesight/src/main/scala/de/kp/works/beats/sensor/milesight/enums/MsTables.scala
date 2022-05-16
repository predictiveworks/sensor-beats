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
  val DISTANCE:MsTables.Value    = Value(2, "distance")
  val HUMIDITY:MsTables.Value    = Value(3, "humidity")
  val TEMPERATURE:MsTables.Value = Value(4, "temperature")
  /**
   * Anomaly tables
   */
  val BATTERY_ANON:MsTables.Value     = Value(5, "battery_anon")
  val DISTANCE_ANON:MsTables.Value    = Value(6, "distance_anon")
  val HUMIDITY_ANON:MsTables.Value    = Value(7, "humidity_anon")
  val TEMPERATURE_ANON:MsTables.Value = Value(8, "temperature_anon")
  /**
   * Forecast tables
   */
  val BATTERY_FORE:MsTables.Value     = Value(9,  "battery_fore")
  val DISTANCE_FORE:MsTables.Value    = Value(10, "distance_fore")
  val HUMIDITY_FORE:MsTables.Value    = Value(11, "humidity_fore")
  val TEMPERATURE_FORE:MsTables.Value = Value(12, "temperature_fore")

  def getTables(product:MsProducts.Value):Seq[String] = {
    product match {
      /*
       * Milesight humidity & temperature
       * indoor sensor
       */
      case EM_300 =>
        Seq(
          MsTables.BATTERY.toString,
          MsTables.BATTERY_ANON.toString,
          MsTables.BATTERY_FORE.toString,
          MsTables.HUMIDITY.toString,
          MsTables.HUMIDITY_ANON.toString,
          MsTables.HUMIDITY_FORE.toString,
          MsTables.TEMPERATURE.toString,
          MsTables.TEMPERATURE_ANON.toString,
          MsTables.TEMPERATURE_FORE.toString)
      /*
       * Milesight ultrasonic outdoor level
       * sensor
       */
      case EM_500_UDL =>
        Seq(
          MsTables.BATTERY.toString,
          MsTables.BATTERY_ANON.toString,
          MsTables.DISTANCE.toString,
          MsTables.DISTANCE_ANON.toString)
    }
  }
}

