package de.kp.works.beats.sensor.sensecap.enums

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
object ScProducts extends Enumeration {
  type ScProduct = Value

  val AIR_TH:ScProduct          = Value(1, "AIR_TH")
  val BARO_PRESSURE:ScProduct   = Value(2, "BARO_PRESSURE")
  val CO2:ScProduct             = Value(3, "CO2")
  val LIGHT_INTENSITY:ScProduct = Value(4, "LIGHT_INTENSITY")
  val SOIL_MT:ScProduct         = Value(5, "SOIL_MT")

}
