package de.kp.works.beats.sensor

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
 * The number of input channels that can be configured
 * to consume sensor events.
 */
object BeatInputs extends Enumeration {
  type BeatInput = Value

  val HELIUM: BeatInputs.Value       = Value(1, "HELIUM")
  val LORIOT: BeatInputs.Value       = Value(2, "LORIOT")
  val THINGS_STACK: BeatInputs.Value = Value(3, "THINGS_STACK")

}

trait BeatSource {

  def subscribeAndPublish():Unit

}
