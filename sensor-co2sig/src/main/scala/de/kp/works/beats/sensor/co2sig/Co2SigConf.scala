package de.kp.works.beats.sensor.co2sig

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

import com.typesafe.config.Config
import de.kp.works.beats.sensor.BeatConf

object Co2SigConf {

  private var instance:Option[Co2SigConf] = None

  def getInstance:Co2SigConf = {
    if (instance.isEmpty) instance = Some(new Co2SigConf())
    instance.get
  }

}

class Co2SigConf extends BeatConf {
  /**
   * The (internal) resource folder file name
   */
  override var path: String = "co2signal.conf"

  override def getRocksTables: Seq[String] = {
    Seq.empty[String]
  }

  def getLocationCfg: Config = getCfg("location")

  def getSignalCfg: Config = getCfg("signal")

}
