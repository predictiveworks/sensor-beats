package de.kp.works.beats.sensor.sensedge.enums

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

object SeTables extends Enumeration {

  type SeTable = Value

  val AirPressure:SeTable  = Value(1, "AirPressure")
  val BatteryLevel:SeTable = Value(2, "BatteryLevel")
  val BreathVOC:SeTable    = Value(3, "BreathVOC")
  val eCO2:SeTable         = Value(4, "eCO2")
  val Humidity:SeTable     = Value(5, "Humidity")
  val IAQ:SeTable          = Value(6, "IAQ")
  val IAQAccuracy:SeTable  = Value(7, "IAQAccuracy")
  val Movement:SeTable     = Value(8, "Movement")
  val StaticIAQ:SeTable    = Value(9, "StaticIAQ")
  val Status:SeTable       = Value(10, "Status")
  val Temperature: SeTable = Value(11, "Temperature")

}
