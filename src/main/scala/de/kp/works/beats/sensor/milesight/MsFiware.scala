package de.kp.works.beats.sensor.milesight

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

import de.kp.works.beats.sensor.BeatActions.{CREATE, UPDATE}
import de.kp.works.beats.sensor.fiware.{Options, Producer}
import de.kp.works.beats.sensor.{BeatChannel, BeatRequest}
/**
 * Implementation of the FIWARE output channel
 */
class MsFiware extends BeatChannel {

  private val config = MsConf.getInstance
  /**
   * The internal configuration is used, if the current
   * configuration is not set here
   */
  if (!config.isInit) config.init()

  private val fiwareOptions = new Options(config)
  private val fiwareProducer = new Producer(fiwareOptions)

  override def execute(request: BeatRequest): Unit = {

    request.action match {
      case CREATE =>
        fiwareProducer.createSensor(request.sensor)

      case UPDATE =>
        fiwareProducer.updateSensor(request.sensor)

      case _ => /* Do nothing */
    }

  }

}
