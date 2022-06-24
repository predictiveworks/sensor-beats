package de.kp.works.beats.sensor.uradmonitor.mappings

import de.kp.works.beats.sensor.BeatMapper

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

object UmMapper extends BeatMapper {

  override var MAPPINGS:String =
    """
      |[
      | {
      |   "alias": "ch20_ppm",
      |   "name":  "ch20",
      |   "unit":  "ppm"
      | },
      | {
      |   "alias": "cl2_ppm",
      |   "name":  "cl2",
      |   "unit":  "ppm"
      | },
      | {
      |   "alias": "co_ppm",
      |   "name":  "co",
      |   "unit":  "ppm"
      | },
      | {
      |   "alias": "co2_ppm",
      |   "name":  "co2",
      |   "unit":  "ppm"
      | },
      | {
      |   "alias": "h2s_ppm",
      |   "name":  "h2s",
      |   "unit":  "ppm"
      | },
      | {
      |   "alias": "humidity_percentage",
      |   "name":  "humidity",
      |   "unit":  "percentage"
      | },
      | {
      |   "alias": "iaq",
      |   "name":  "iaq",
      |   "unit":  ""
      | },
      | {
      |   "alias": "o3_ppm",
      |   "name":  "o3",
      |   "unit":  "ppm"
      | },
      | {
      |   "alias": "nh3_ppm",
      |   "name":  "nh3",
      |   "unit":  "ppm"
      | },
      | {
      |   "alias": "no2_ppm",
      |   "name":  "no2",
      |   "unit":  "ppm"
      | },
      | {
      |   "alias": "pm1_ug_m3",
      |   "name":  "pm1",
      |   "unit":  "ug_m3"
      | },
      | {
      |   "alias": "pm2_5_ug_m3",
      |   "name":  "pm2_5",
      |   "unit":  "ug_m3"
      | },
      | {
      |   "alias": "pm10_ug_m3",
      |   "name":  "pm10",
      |   "unit":  "ug_m3"
      | },
      | {
      |   "alias": "pressure_pa",
      |   "name":  "pressure",
      |   "unit":  "pa"
      | },
      | {
      |   "alias": "so2_ppm",
      |   "name":  "so2",
      |   "unit":  "ppm"
      | },
      | {
      |   "alias": "sound_dba",
      |   "name":  "sound",
      |   "unit":  "dba"
      | },
      | {
      |   "alias": "temperature_celsius",
      |   "name":  "temperature",
      |   "unit":  "celsius"
      | },
      | {
      |   "alias": "voc_ohm",
      |   "name":  "gas_resistance",
      |   "unit":  "ohm"
      | }
      |]
      |""".stripMargin

}
