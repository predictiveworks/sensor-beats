package de.kp.works.beats.sensor.elsys.decoders

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

import de.kp.works.beats.sensor.elsys.enums.EsFields._

/**
 * Payload decoder for Elsys EMS Desk: INDOOR
 */
object EMS_DESK extends CommonDecoder {
  /*
   * EMS Desk is ideal for mounting under desks. It is designed
   * to detect occupancy regardless of type of table and position
   * of the person.
   *
   * EMS Desk is equipped with a body heat sensor and can detect
   * when somebody is standing or sitting by a desk, even if the
   * person is still.
   *
   * Environment measuring can be combined with occupancy detection
   * since the EMS Desk is equipped with internal sensors for measuring
   * the temperature and humidity.
   */
  override def fields: Seq[String] = {
    Seq(
      TEMPERATURE,
      HUMIDITY,
      X,
      Y,
      Z,
      OCCUPANCY
    )
  }
}
