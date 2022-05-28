package de.kp.works.beats.sensor.elsys.enums

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

object EsProducts extends Enumeration {

  type EsProduct = Value

  val ELT_2: EsProduct          = Value(1, "ELT_2")
  val ELT_LITE: EsProduct       = Value(2, "ELT_LITE")
  val ELT_ULTRASONIC: EsProduct = Value(3, "ELT_ULTRASONIC")
  val EMS: EsProduct            = Value(4, "EMS")
  val EMS_DESK: EsProduct       = Value(5, "EMS_DESK")
  val EMS_DOOR: EsProduct       = Value(6, "EMS_DOOR")
  val EMS_LITE: EsProduct       = Value(7, "EMS_LITE")
  val ERS: EsProduct            = Value(8, "ERS")
  val ERS_CO2: EsProduct        = Value(9, "ERS_CO2")
  val ERS_CO2_LITE: EsProduct   = Value(10, "ERS_CO2_LITE")
  val ERS_EYE: EsProduct        = Value(11, "ERS_EYE")
  val ERS_LITE: EsProduct       = Value(12, "ERS_LITE")
  val ERS_SOUND: EsProduct      = Value(13, "ERS_SOUND")

}
