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

import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.SourceQueueWithComplete
import ch.qos.logback.classic.Logger
import com.google.gson.JsonArray
import de.kp.works.beats.sensor.BeatActions.{COMPUTE, READ}
import de.kp.works.beats.sensor.api.{AnomalyReq, ApiActor, ForecastReq, MonitorReq, TrendReq}
import de.kp.works.beats.sensor.{BeatActions, BeatConf, BeatMessages}
/**
 * The [AnomalyActor] supports the re-training
 * of the SensorBeat's anomaly detection model,
 * and also the provisioning of detected anomalies.
 *
 * Note, computing anomalies is regularly performed
 * on a scheduled basis, but can also be executed on
 * demand as well.
 */
class AnomalyActor(queue: SourceQueueWithComplete[String]) extends ApiActor {

  override protected var logger: Logger = MsLogger.getLogger
  override protected var config: BeatConf = MsConf.getInstance
  /**
   * [MsInsight] is used to retrieve anomalies as
   * well as performing anomaly detection on demand
   */
  private val msInsight = new MsInsight(queue, logger)
  /**
   * The response of this request is a JsonArray;
   * in case of an invalid request, an empty response
   * is returned
   */
  private val emptyResponse = new JsonArray

  override def execute(request: HttpRequest): String = {

    val json = getBodyAsJson(request)
    if (json == null) {
      logger.warn(BeatMessages.invalidJson())
      return emptyResponse.toString
    }

    val req = mapper.readValue(json.toString, classOf[AnomalyReq])
    try {
      BeatActions.withName(req.action) match {
        case COMPUTE =>
          msInsight.computeAnomalies(req.startTime, req.endTime)

        case READ =>
          msInsight.readAnomalies(req.startTime, req.endTime)

        case _ => throw new Exception(s"Unknown action detected.")
      }

    } catch {
      case t:Throwable =>
        val message = s"Anomaly request failed: ${t.getLocalizedMessage}"
        logger.error(message)

        emptyResponse.toString
    }
  }

}
/**
 * The [ForecastActor] supports the re-training
 * of the SensorBeat's timeseries forecast model,
 * and also the provisioning of forecasted values.
 *
 * Note, computing time series forecasts is regularly
 * performed on a scheduled basis, but can also be
 * executed on demand as well.
 */
class ForecastActor(queue: SourceQueueWithComplete[String]) extends ApiActor {

  override protected var logger: Logger = MsLogger.getLogger
  override protected var config: BeatConf = MsConf.getInstance
  /**
   * [MsInsight] is used to retrieve time series forecasts
   * as well as performing time series predictions on demand
   */
  private val msInsight = new MsInsight(queue, logger)
  /**
   * The response of this request is a JsonArray;
   * in case of an invalid request, an empty response
   * is returned
   */
  private val emptyResponse = new JsonArray

  override def execute(request: HttpRequest): String = {

    val json = getBodyAsJson(request)
    if (json == null) {
      logger.warn(BeatMessages.invalidJson())
      return emptyResponse.toString
    }

    val req = mapper.readValue(json.toString, classOf[ForecastReq])
    try {
      BeatActions.withName(req.action) match {
        case COMPUTE =>
          msInsight.computeForecasts(req.startTime, req.endTime)

        case READ =>
          msInsight.readForecasts(req.startTime, req.endTime)

        case _ => throw new Exception(s"Unknown action detected.")
      }

    } catch {
      case t:Throwable =>
        val message = s"Forecast request failed: ${t.getLocalizedMessage}"
        logger.error(message)

        emptyResponse.toString
    }

  }

}
/**
 * The [MonitorActor] supports the provisioning of
 * sensor events based on a SQL statement. This actor
 * is part of the `Sensor as a Table` approach.
 */
class MonitorActor(queue: SourceQueueWithComplete[String]) extends ApiActor {

  override protected var logger: Logger = MsLogger.getLogger
  override protected var config: BeatConf = MsConf.getInstance
  /**
   * [MsSql] is used to do the SQL query interpretation,
   * transformation to RocksDB commands and returning
   * the respective entries
   */
  private val msSql = new MsSql(queue, logger)
  /**
   * The response of this request is a JsonArray;
   * in case of an invalid request, an empty response
   * is returned
   */
  private val emptyResponse = new JsonArray

  override def execute(request: HttpRequest): String = {

    val json = getBodyAsJson(request)
    if (json == null) {
      logger.warn(BeatMessages.invalidJson())
      return emptyResponse.toString
    }

    val req = mapper.readValue(json.toString, classOf[MonitorReq])
    val sql = req.sql
    /*
     * Validate SQL query
     */
    if (sql.isEmpty) {
      logger.warn(BeatMessages.emptySql())
      return emptyResponse.toString
    }

    try {
      msSql.read(sql)

    } catch {
      case t:Throwable =>
        val message = s"Monitor request failed: ${t.getLocalizedMessage}"
        logger.error(message)

        emptyResponse.toString
    }

  }

}
/**
 * The [TrendActor] supports the provisioning of
 * sensor event trends based on a SQL statement.
 * This actor is part of the `Sensor as a Table`
 * approach.
 */
class TrendActor(queue: SourceQueueWithComplete[String]) extends ApiActor {

  override protected var logger: Logger = MsLogger.getLogger
  override protected var config: BeatConf = MsConf.getInstance
  /**
   * [MsSql] is used to do the SQL query interpretation,
   * transformation to RocksDB commands and returning
   * the respective entries
   */
  private val msSql = new MsSql(queue, logger)
  /**
   * The response of this request is a JsonArray;
   * in case of an invalid request, an empty response
   * is returned
   */
  private val emptyResponse = new JsonArray

  override def execute(request: HttpRequest): String = {

    val json = getBodyAsJson(request)
    if (json == null) {
      logger.warn(BeatMessages.invalidJson())
      return emptyResponse.toString
    }

    val req = mapper.readValue(json.toString, classOf[TrendReq])
    val sql = req.sql
    /*
     * Validate SQL query
     */
    if (sql.isEmpty) {
      logger.warn(BeatMessages.emptySql())
      return emptyResponse.toString
    }
    val indicator = req.indicator
    /*
     * Validate technical indicator
     */
    if (indicator.isEmpty) {
      logger.warn(BeatMessages.emptyIndicator())
      return emptyResponse.toString
    }

    try {
      msSql.trend(sql, indicator, req.timeframe)

    } catch {
      case t:Throwable =>
        val message = s"Trend request failed: ${t.getLocalizedMessage}"
        logger.error(message)

        emptyResponse.toString
    }

  }

}
