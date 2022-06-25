package de.kp.works.beats.sensor.sensedge.mappings
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

import de.kp.works.beats.sensor.BeatMapper

object SeMapper extends BeatMapper {

  override var MAPPINGS: String =
    """
      |[
      | {
      |   "alias": "battery_percentage",
      |   "name":  "BatteryLevel",
      |   "unit":  "percentage"
      | },
      | {
      |   "alias": "co2_ppm",
      |   "name":  "eCO2",
      |   "unit":  "ppm"
      | },
      | {
      |   "alias": "humidity_percentage",
      |   "name":  "Humidity",
      |   "unit":  "percentage"
      | },
      | {
      |   "alias": "iaq",
      |   "name":  "IAQ",
      |   "unit":  ""
      | },
      | {
      |   "alias": "pressure_hpa",
      |   "name":  "AirPressure",
      |   "unit":  "hpa"
      | },
      | {
      |   "alias": "temperature_celsius",
      |   "name":  "Temperature",
      |   "unit":  "celsius"
      | },
      | {
      |   "alias": "tvoc_mg_m3",
      |   "name":  "BreathVOC",
      |   "unit":  "mg_m3"
      | }
      |]
      |""".stripMargin

}
