package de.kp.works.beats.sensor.dragino.enums

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

object DoProducts extends Enumeration {

  type DoProduct = Value

  val LDDS04:DoProduct      = Value(7, "LDDS04")
  val LDDS20:DoProduct      = Value(8, "LDDS20")
  val LDDS45:DoProduct      = Value(10, "LDDS45")
  val LHT52:DoProduct       = Value(15, "LHT52")
  val LHT65:DoProduct       = Value(16, "LHT65")
  val LLDS12:DoProduct      = Value(18, "LLDS12")
  val LLMS01:DoProduct      = Value(19, "LLMS01")
  val LMDS200:DoProduct     = Value(20, "LMDS200")
  val LSE01:DoProduct       = Value(21, "LSE01")
  val LSNPK01:DoProduct     = Value(26, "LSNPK01")
  val LSPH01:DoProduct      = Value(27, "LSPH01")
  val WSC1_L:DoProduct      = Value(34, "WSC1_L")

}
