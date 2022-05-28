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

/**
 * Payload decoder for Elsys ELT-LITE: Measurements comprise
 * a single external sensor like an electricity meter, flow
 * meter, analog sensor, moisture.
 */
object ELT_LITE extends CommonDecoder {
  /*
   * The supported field are set empty as
   * they are dynamic and not pre-assigned
   * to the sensor
   */
  override def fields: Seq[String] = {
    Seq()
  }

}
