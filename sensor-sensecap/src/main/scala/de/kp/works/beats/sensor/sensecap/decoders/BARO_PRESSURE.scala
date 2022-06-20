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

import de.kp.works.beats.sensor.sensecap.enums.ScFields.BarometricPressure

object BARO_PRESSURE extends CommonDecoder {
  /*
   * SenseCAP Wireless Barometric Pressure Sensor measures atmospheric
   * pressure in the range of 300~1100 hPa. Featuring high-precision,
   * stability, and high EMC robustness, this sensor is suitable for
   * industrial applications such as weather stations, outdoor farms,
   * tea plantations, greenhouses, and more.
   */
  override def fields: Seq[String] =
    Seq(BarometricPressure)
}
