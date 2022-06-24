package de.kp.works.beats.sensor.ellenex.mappings

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

object ExMapper extends BeatMapper {
  /**
   * The main concept is define alias
   * names that can be harmonized.
   */
  override var MAPPINGS: String =
    """
      |[
      | {
      |   "alias": "battery_voltage",
      |   "name":  "batteryVoltage",
      |   "unit":  "voltage"
      | },
      | {
      |   "alias": "liquid_level_metre",
      |   "name":  "level",
      |   "unit":  "metre"
      | },
      | {
      |   "alias": "pressure_bar",
      |   "name":  "pressure",
      |   "unit":  "bar"
      | },
      | {
      |   "alias": "temperature_celsius",
      |   "name":  "temperature",
      |   "unit":  "celsius"
      | }
      |]
      |""".stripMargin

}
