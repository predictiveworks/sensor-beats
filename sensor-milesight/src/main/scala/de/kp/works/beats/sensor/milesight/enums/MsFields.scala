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

object MsFields {
  /**
   * Milesight devices decoded payload fields
   */
  val ACTIVITY: String             = "activity"

  val ADC1_AVG: String             = "adc1.avg"
  val ADC1_CUR: String             = "adc1.cur"
  val ADC1_MAX: String             = "adc1.max"
  val ADC1_MIN: String             = "adc1.min"

  val ADC2_AVG: String             = "adc2.avg"
  val ADC2_CUR: String             = "adc2.cur"
  val ADC2_MAX: String             = "adc2.max"
  val ADC2_MIN: String             = "adc2.min"

  val BATTERY: String              = "battery"
  val BEEP: String                 = "beep"
  val CO2: String                  = "co2"
  val COUNTER1: String             = "counter1"
  val COUNTER2: String             = "counter2"
  val DISTANCE: String             = "distance"
  val DOOR: String                 = "door"
  val EC: String                    = "ec"
  /* No table equivalent */
  val FIRMWARE_VERSION: String     = "firmware_version"
  val GPIO1: String                = "gpio1"
  val GPIO2: String                = "gpio2"
  /* No table equivalent */
  val HARDWARE_VERSION: String     = "hardware_version"
  val HCHO: String                 = "hcho"
  val HUMIDITY: String             = "humidity"
  val ILLUMINATION: String         = "illumination"
  val IN: String                   = "in"
  val INFRARED: String             = "infrared"
  val INFRARED_AND_VISIBLE: String = "infrared_and_visible"
  val LIGHT_LEVEL: String          = "light_level"
  val O3: String                   = "o3"
  val OUT: String                  = "out"
  val PEOPLE_COUNTER_ALL: String   = "people_counter_all"
  val PEOPLE_MAX: String           = "people_max"
  val PIR: String                  = "pir"
  val PM10: String                 = "pm10"
  val PM2_5:String                 = "pm2_5"
  val PRESSURE: String             = "pressure"
  /* No table equivalent */
  val PROTOCOL_VERSION: String     = "protocol_version"
  val REGION_COUNT: String         = "region_count"
  /* No table equivalent */
  val SN: String                   = "sn"
  val TEMPERATURE: String          = "temperature"
  val TVOC: String                 = "tvoc"
  val WATER_LEAK: String           = "water_leak"
  val WATER_LEVEL: String          = "water_level"

}
