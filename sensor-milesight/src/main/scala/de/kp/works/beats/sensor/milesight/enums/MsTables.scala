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

object MsTables extends Enumeration {
  type MsTable = Value

  val ACTIVITY: MsTable             = Value(1, "activity")

  val ADC1_AVG: MsTable             = Value(2, "adc1_avg")
  val ADC1_CUR: MsTable             = Value(3, "adc1_cur")
  val ADC1_MAX: MsTable             = Value(4, "adc1_max")
  val ADC1_MIN: MsTable             = Value(5, "adc1_min")
  val ADC2_AVG: MsTable             = Value(6, "adc2_avg")
  val ADC2_CUR: MsTable             = Value(7, "adc2_cur")
  val ADC2_MAX: MsTable             = Value(8, "adc2_max")
  val ADC2_MIN: MsTable             = Value(9, "adc2_min")

  val BATTERY: MsTable              = Value(10, "battery")
  val BEEP: MsTable                 = Value(11, "beep")
  val CO2: MsTable                  = Value(12, "co2")
  val COUNTER1: MsTable             = Value(13, "counter1")
  val COUNTER2: MsTable             = Value(14, "counter2")
  val DISTANCE: MsTable             = Value(15, "distance")
  val DOOR: MsTable                 = Value(16, "door")
  val EC: MsTable                   = Value(17, "ec")
  val GPIO1: MsTable                = Value(18, "gpio1")
  val GPIO2: MsTable                = Value(19, "gpio2")
  val HCHO: MsTable                 = Value(20, "hcho")
  val HUMIDITY: MsTable             = Value(21, "humidity")
  val ILLUMINATION: MsTable         = Value(22, "illumination")
  val IN: MsTable                   = Value(23, "in")
  val INFRARED: MsTable             = Value(24, "infrared")
  val INFRARED_AND_VISIBLE: MsTable = Value(25, "infrared_and_visible")
  val LIGHT_LEVEL: MsTable          = Value(26, "light_level")
  val O3: MsTable                   = Value(27, "o3")
  val OUT: MsTable                  = Value(28, "out")
  val PEOPLE_COUNTER_ALL: MsTable   = Value(29, "people_counter_all")
  val PEOPLE_MAX: MsTable           = Value(30, "people_max")
  val PIR: MsTable                  = Value(31, "pir")
  val PM10: MsTable                 = Value(32, "pm10")
  val PM2_5: MsTable                = Value(33, "pm2_5")
  val PRESSURE: MsTable             = Value(34, "pressure")
  val REGION_COUNT: MsTable         = Value(35, "region_count")
  val TEMPERATURE: MsTable          = Value(36, "temperature")
  val TVOC: MsTable                 = Value(37, "tvoc")
  val WATER_LEAK:MsTable            = Value(38, "water_leak")
  val WATER_LEVEL:MsTable           = Value(39, "water_level")

}

