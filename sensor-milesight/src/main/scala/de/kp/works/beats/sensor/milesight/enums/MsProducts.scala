package de.kp.works.beats.sensor.milesight.enums

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

object MsProducts extends Enumeration {
  type MsProduct = Value

  val AM100: MsProduct       = Value(1, "AM100")
  val AM300: MsProduct       = Value(2, "AM300")
  val EM300_MCS: MsProduct   = Value(3, "EM300_MCS")
  val EM300_SLD: MsProduct   = Value(4, "EM300_SLD")
  val EM300_TH: MsProduct    = Value(5, "EM300_TH")
  val EM300_ZLD: MsProduct   = Value(6, "EM300_ZLD")
  val EM500_CO2: MsProduct   = Value(7, "EM500_CO2")
  val EM500_LGT: MsProduct   = Value(8, "EM500_LGT")
  val EM500_PP: MsProduct    = Value(9, "EM500_PP")
  val EM500_PT100: MsProduct = Value(10, "EM500_PT100")
  val EM500_SMT: MsProduct   = Value(11, "EM500_SMT")
  val EM500_SWL: MsProduct   = Value(12, "EM500_SWL")
  val EM500_UDL: MsProduct   = Value(13, "EM500_UDL")
  val UC500: MsProduct       = Value(14, "UC500")
}

