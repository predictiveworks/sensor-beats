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

import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.BeatLogger

object MsLogger extends BeatLogger {

  override protected var loggerName = "MsLogger"
  /**
   * The internal configuration is used, if the current
   * configuration is not set here
   */
  if (!MsConf.isInit) MsConf.init()

  private val logger = buildLogger
  def getLogger: Logger = logger

  override def getFolder: String = MsConf.getLogFolder

}

trait MsLogging {

  val logger: Logger = MsLogger.getLogger

  def info(message: String): Unit = {
    logger.info(s"$message")
  }

  def warn(message: String): Unit = {
    logger.warn(s"$message")

  }

  def error(message: String): Unit = {
    logger.error(s"$message")
  }

}
