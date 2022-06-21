package de.kp.works.beats.sensor.netvox.enums

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

object NvProducts extends Enumeration {

  type NvProduct = Value

  val R711:NvProduct    = Value(1, "R711")
  val R718A:NvProduct   = Value(2, "R718A")
  val R718AB:NvProduct  = Value(3, "R718AB")
  val R720A:NvProduct   = Value(4, "R720A")
  val RB11E:NvProduct   = Value(5, "RB11E")

}
