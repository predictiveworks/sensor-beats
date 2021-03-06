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
 * Payload decoder for Elsys ELT Ultrasonic: OUTDOOR
 */
object ELT_ULTRASONIC extends CommonDecoder {
  /*
   * The ELT Ultrasonic is designed for measuring the distance. It can be placed
   * in various environments and can work as a bin level sensor, tank level sensor,
   * or water level under bridges, and more.
   *
   * It also has four internal sensors:
   *
   * - temperature sensor,
   * - humidity sensor,
   * - accelerometer,
   * - atmospheric pressure sensor.
   */
  override def fields: Seq[String] = {
    Seq(
      TEMPERATURE,
      HUMIDITY,
      X,
      Y,
      Z,
      PRESSURE,
      DISTANCE)
  }
}
