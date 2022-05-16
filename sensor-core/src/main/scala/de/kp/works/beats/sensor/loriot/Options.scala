package de.kp.works.beats.sensor.loriot

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

import akka.http.scaladsl.ConnectionContext
import com.typesafe.config.Config
import de.kp.works.beats.sensor.BeatConf
import de.kp.works.beats.sensor.ssl.SslOptions

class Options[T <: BeatConf](config:T) {

  private val loriotCfg = config.getLoriotCfg
  /**
   * Retrieve the connection context for the
   * LORIOT Socket Server
   */
  def getContext:Option[ConnectionContext] = {

    val security = getSecurityCfg
    if (security.getString("ssl") == "false") None

    else
      Some(SslOptions.buildConnectionContext(security))

  }

  private def getSecurityCfg: Config =
    loriotCfg.getConfig("security")

  def getServerUrl:String = {

    val server = loriotCfg.getString("server")

    val app_id = loriotCfg.getString("app_id")
    val app_token = loriotCfg.getString("app_token")

    s"wss://$server.loriot.io/app?id=$app_id&token=$app_token"

  }
}
