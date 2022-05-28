package de.kp.works.beats.sensor.elsys.enums

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

object EsTables extends Enumeration {

  type EsTable = Value

  val ACC_MOTION: EsTable              = Value(1, "accMotion")
  val ANALOG1: EsTable                 = Value(2, "analog1")
  val ANALOG2: EsTable                 = Value(3, "analog2")
  val ANALOG_UV: EsTable               = Value(4, "analogUv")
  val CO2: EsTable                     = Value(5, "co2")
  val DIGITAL: EsTable                 = Value(6, "digital")
  val DIGITAL2: EsTable                = Value(7, "digital2")
  val DISTANCE: EsTable                = Value(8, "distance")
  val EXTERNAL_TEMPERATURE: EsTable    = Value(9, "externalTemperature")
  val EXTERNAL_TEMPERATURE2: EsTable   = Value(10, "externalTemperature2")
  val GRIDEYE: EsTable                 = Value(11, "grideye")
  val HUMIDITY: EsTable                = Value(12, "humidity")
  val IR_EXTERNAL_TEMPERATURE: EsTable = Value(13, "irExternalTemperature")
  val IR_INTERNAL_TEMPERATURE: EsTable = Value(14, "irInternalTemperature")
  val LAT: EsTable                     = Value(15, "lat")
  val LIGHT: EsTable                   = Value(16, "light")
  val LONG: EsTable                    = Value(17, "long")
  val MOTION: EsTable                  = Value(18, "motion")
  val OCCUPANCY: EsTable               = Value(19, "occupancy")
  val PRESSURE: EsTable                = Value(20, "pressure")
  val PULSE1: EsTable                  = Value(21, "pulse1")
  val PULSE2: EsTable                  = Value(22, "pulse2")
  val PULSE1_ABS: EsTable              = Value(23, "pulseAbs")
  val PULSE2_ABS: EsTable              = Value(24, "pulseAbs2")
  val SOUND_AVG: EsTable               = Value(25, "soundAvg")
  val SOUND_PEAK: EsTable              = Value(26, "soundPeak")
  val TEMPERATURE: EsTable             = Value(27, "temperature")
  val TVOC: EsTable                    = Value(28, "tvoc")
  val VDD: EsTable                     = Value(29, "vdd")
  val WATER_LEAK: EsTable              = Value(30, "waterleak")
  val X: EsTable                       = Value(31, "x")
  val Y: EsTable                       = Value(32, "y")
  val Z: EsTable                       = Value(33, "z")

}
