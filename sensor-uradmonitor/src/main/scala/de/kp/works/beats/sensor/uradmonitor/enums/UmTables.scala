package de.kp.works.beats.sensor.uradmonitor.enums

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

object UmTables extends Enumeration {

  type UmTable = Value

  val CH20: UmTable           = Value(1, "ch20")
  val CL2: UmTable            = Value(2, "cl2")
  val CO: UmTable             = Value(3, "co")
  val CO2: UmTable            = Value(4, "co2")
  val GAS_RESISTANCE: UmTable = Value(5, "gas_resistance")
  val H2S: UmTable            = Value(6, "h2s")
  val HUMIDITY: UmTable       = Value(7, "humidity")
  val IAQ: UmTable            = Value(8, "iaq")
  val NH3: UmTable            = Value(9, "nh3")
  val NO2: UmTable            = Value(10, "no2")
  val O3: UmTable             = Value(11, "o3")
  val PM1: UmTable            = Value(12, "pm1")
  val PM10: UmTable           = Value(13, "pm10")
  val PM2_5: UmTable          = Value(14, "pm2_5")
  val PRESSURE: UmTable       = Value(15, "pressure")
  val SO2: UmTable            = Value(16, "so2")
  val SOUND: UmTable          = Value(17, "sound")
  val TEMPERATURE: UmTable    = Value(18, "temperature")


}
