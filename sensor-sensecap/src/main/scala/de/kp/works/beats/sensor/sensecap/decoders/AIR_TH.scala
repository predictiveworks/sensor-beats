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

import de.kp.works.beats.sensor.sensecap.enums.ScFields.{AirHumidity, AirTemperature}

object AIR_TH extends CommonDecoder {
  /*
   * SenseCAP Wireless Air Temperature & Humidity Sensor measures temperature and humidity
   * in the atmosphere at the range of -40° to 85° Celsius and 0 to 100 %RH (non-condensing)
   * respectively.
   *
   * With a high-precision measurement chip, this sensor features stability and reliability,
   * making it widely applicable in industrial environmental sensing scenarios.
   */
  override def fields: Seq[String] = {
    Seq(
      AirTemperature,
      AirHumidity)
  }
}
