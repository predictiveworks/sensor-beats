package de.kp.works.beats.sensor.fiware

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

import akka.http.scaladsl.HttpsConnectionContext
import com.typesafe.config.Config
import de.kp.works.beats.sensor.BeatConf
import de.kp.works.beats.sensor.ssl.SslOptions
/**
 * [Options] is a FIWARE specific wrapper of
 * the sensor specific configuration.
 */
class Options[T <: BeatConf](config:T) {
  /**
   * Configuration of the access parameters
   * of a FIWARE Context Broker
   */
  private val fiwareConfig = config.getFiwareCfg
  /**
   * HTTP(s) address of the FIWARE Context Broker
   */
  def getBrokerUrl:String =
    fiwareConfig.getString("brokerUrl")
  /**
   * Retrieve the HTTPS connection context for the
   * FIWARE Context Broker
   */
  def getHttpsContext:Option[HttpsConnectionContext] = {

    val security = getSecurityCfg
    if (security.getString("ssl") == "false") None

    else
      Some(SslOptions.buildConnectionContext(security))

  }

  private def getSecurityCfg: Config =
    fiwareConfig.getConfig("security")

}
