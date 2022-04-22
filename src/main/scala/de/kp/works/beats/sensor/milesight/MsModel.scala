package de.kp.works.beats.sensor.milesight

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

object MsAttrs extends Enumeration {
  type MsAttr = Value

  val TIME:MsAttrs.Value  = Value(1, "time")
  val VALUE:MsAttrs.Value = Value(2, "value")

}

object MsTables extends Enumeration {
  type MsTable = Value
  /*
   * Monitoring tables
   */
  val BATTERY:MsTables.Value     = Value(1, "battery")
  val HUMIDITY:MsTables.Value    = Value(2, "humidity")
  val TEMPERATURE:MsTables.Value = Value(3, "temperature")
  /*
   * Anomaly tables
   */
  val BATTERY_ANON:MsTables.Value     = Value(4, "battery_anon")
  val HUMIDITY_ANON:MsTables.Value    = Value(5, "humidity_anon")
  val TEMPERATURE_ANON:MsTables.Value = Value(6, "temperature_anon")
  /*
   * Forecast tables
   */
  val BATTERY_FORE:MsTables.Value     = Value(7, "battery_fore")
  val HUMIDITY_FORE:MsTables.Value    = Value(8, "humidity_fore")
  val TEMPERATURE_FORE:MsTables.Value = Value(9, "temperature_fore")

}
