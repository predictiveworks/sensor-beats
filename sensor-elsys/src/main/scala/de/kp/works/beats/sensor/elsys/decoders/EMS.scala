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
 * Payload decoder for Elsys EMS: INDOOR
 */
object EMS extends CommonDecoder {
  /*
   * Indoor LoRaWAN® room sensor for measuring the indoor environment,
   * the number of openings on a door, or maybe if something has moved
   * or not.
   *
   * It also has a water leak detector which makes it ideal to mount under
   * e.g. a dishwasher. It is perfect for mounting on door frames, window panes,
   * or any other limited surface area.
   */
  override def fields: Seq[String] = {
    Seq(
      TEMPERATURE,
      HUMIDITY,
      X,
      Y,
      Z,
      /*
       * Refers to door switch with respect to
       * Elsys website documentation
       */
      PULSE1,
      WATER_LEAK
    )
  }
}
