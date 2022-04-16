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
 * The [ThingsServer] is designed to connect to 3 different
 * data sources, EU Air Quality service, OpenWeather and The
 * Things Stack. The data are read either on a scheduled basis
 * or on demand as MQTT listening.
 *
 * This approach can be extended to other (real-time) data
 * sources as well like stock exchange rates.
 *
 * The current data destination or sink is the ThingsBoard
 * MQTT broker (community edition) that persists received
 * telemetry data in a Postgres database.
 */

import ch.qos.logback.classic.{Level, Logger, LoggerContext}
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.encoder.Encoder
import ch.qos.logback.core.rolling.{RollingFileAppender, SizeAndTimeBasedRollingPolicy}
import ch.qos.logback.core.util.FileSize
import org.slf4j.LoggerFactory

trait BeatLogger {

  protected def getFolder:String
  protected var loggerName:String

  /**
   * This method build the Logback logger including
   * a rolling file appender programmatically
   */
  protected def buildLogger:Logger = {

    val logCtx = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

    /*
     * Build encoder and use default pattern
     */
    val logEncoder = new PatternLayoutEncoder()
    logEncoder.setContext(logCtx)

    logEncoder.setPattern(" %d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n")
    logEncoder.start()
    /*
     * Build rolling file appender
     */
    val logFileAppender = new RollingFileAppender[ILoggingEvent]()
    logFileAppender.setContext(logCtx)

    logFileAppender.setName("logFileAppender")
    logFileAppender.setEncoder(logEncoder.asInstanceOf[Encoder[ILoggingEvent]])

    logFileAppender.setAppend(true)
    logFileAppender.setFile(s"${getFolder}beat.log")
    /*
     * Set time- and size-based rolling policy
     */
    val logFilePolicy = new SizeAndTimeBasedRollingPolicy[ILoggingEvent]()
    logFilePolicy.setContext(logCtx)

    logFilePolicy.setParent(logFileAppender)
    logFilePolicy.setFileNamePattern(s"${getFolder}beat.%d{yyyy-MM-dd}.%i.log")

    logFilePolicy.setMaxFileSize(FileSize.valueOf("100mb"))
    logFilePolicy.setTotalSizeCap(FileSize.valueOf("3GB"))

    logFilePolicy.setMaxHistory(30)
    logFilePolicy.start()

    logFileAppender.setRollingPolicy(logFilePolicy)
    logFileAppender.start()
    /*
     * Finally builder logger
     */
    val logger = logCtx.getLogger(loggerName)
    logger.setAdditive(false)

    logger.setLevel(Level.INFO)
    logger.addAppender(logFileAppender)

    logFileAppender.start()
    logger

  }
}