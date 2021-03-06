package de.kp.works.beats.sensor.sensecap.decoders

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

import de.kp.works.beats.sensor.sensecap.enums.ScFields

object CO2 extends CommonDecoder {
  /*
   * SenseCAP Wireless CO2 Sensor measures the level of carbon dioxide (CO2)
   * gas at the range of 0 ~ 40000 ppm in the atmosphere, applicable for
   * indoor environments.
   *
   * It is perfect for monitoring CO2 ppm in greenhouses, industrial campuses,
   * factories, schools, office buildings, hotels, hospitals, transportation
   * stations, and anywhere that data of CO2 emission is needed.
   */
  override def fields: Seq[String] =
    Seq(ScFields.CO2)
}
