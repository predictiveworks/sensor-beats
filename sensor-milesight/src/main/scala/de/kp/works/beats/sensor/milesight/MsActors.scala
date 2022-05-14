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

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.model.HttpRequest
import akka.routing.RoundRobinPool
import ch.qos.logback.classic.Logger
import com.google.gson.{JsonArray, JsonObject}
import de.kp.works.beats.sensor.BeatMessages
import de.kp.works.beats.sensor.api._

class MsDeepActor extends DeepActor with MsLogging {

  override def getLogger: Logger = logger

  override def validateTable(table: String): Unit =
    MsTables.withName(table)

}
/**
 * The [AnomalyActor] supports the re-training
 * of the SensorBeat's anomaly detection model.
 *
 * Note, computing anomalies is regularly performed
 * on a scheduled basis, but can also be executed on
 * demand as well.
 */
class AnomalyActor(config:MsConf) extends ApiActor(config) with MsLogging {

  private val worker:ActorRef =
    system
      .actorOf(RoundRobinPool(instances)
        .withResizer(resizer)
        .props(Props(new MsDeepActor())), "AnomWorker")

  /**
   * The response of this request is a JsonArray;
   * in case of an invalid request, an empty response
   * is returned
   */
  private val emptyResponse = new JsonObject

  override def execute(request: HttpRequest): String = {

    val json = getBodyAsJson(request)
    if (json == null) {
      logger.warn(BeatMessages.invalidJson())
      return emptyResponse.toString
    }

    val req = mapper.readValue(json.toString, classOf[DeepReq])
    worker ! req

    val response = new JsonObject
    response.addProperty("message", BeatMessages.anomalyStarted())

    response.toString

  }

  override def getLogger: Logger = logger

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
class ForecastActor(config:MsConf) extends ApiActor(config) with MsLogging {

  private val worker:ActorRef =
    system
      .actorOf(RoundRobinPool(instances)
        .withResizer(resizer)
        .props(Props(new MsDeepActor())), "ForeWorker")

  /**
   * The response of this request is a JsonArray;
   * in case of an invalid request, an empty response
   * is returned
   */
  private val emptyResponse = new JsonObject

  override def execute(request: HttpRequest): String = {

    val json = getBodyAsJson(request)
    if (json == null) {
      logger.warn(BeatMessages.invalidJson())
      return emptyResponse.toString
    }

    val req = mapper.readValue(json.toString, classOf[DeepReq])
    /*
     * Each deep learning request receives a unique
     * job identifier
     */
    val jid = s"job-${java.util.UUID.randomUUID.toString}"
    val reqS = req.copy(id = jid)

    worker ! reqS

    val response = new JsonObject
    response.addProperty("message", BeatMessages.forecastStarted())

    response.toString

  }

  override def getLogger: Logger = logger

}
/**
 * The [InsightActor] supports the provisioning of
 * sensor event insights based on a SQL statement.
 * This actor is part of the `Sensor as a Table`
 * approach.
 */
class InsightActor(config:MsConf) extends ApiActor(config) with MsLogging {
  /**
   * [MsSql] is used to do the SQL query interpretation,
   * transformation to RocksDB commands and returning
   * the respective entries
   */
  private val msSql = new MsSql()
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

    val req = mapper.readValue(json.toString, classOf[InsightReq])
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
        logger.error(BeatMessages.insightFailed(t))
        emptyResponse.toString
    }

  }

  override def getLogger: Logger = logger

}
/**
 * The [MsJobActor] supports the provisioning of
 * status information about deep learning jobs
 */
class MsJobActor(config:MsConf) extends JobActor(config) with MsLogging {

  override def getLogger: Logger = logger

}

/**
 * The [MonitorActor] supports the provisioning of
 * sensor events based on a SQL statement. This actor
 * is part of the `Sensor as a Table` approach.
 */
class MonitorActor(config:MsConf) extends ApiActor(config) with MsLogging {
  /**
   * [MsSql] is used to do the SQL query interpretation,
   * transformation to RocksDB commands and returning
   * the respective entries
   */
  private val msSql = new MsSql()
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
        logger.error(BeatMessages.monitorFailed(t))
        emptyResponse.toString
    }

  }

  override def getLogger: Logger = logger

}
/**
 * The [TrendActor] supports the provisioning of
 * sensor event trends based on a SQL statement.
 * This actor is part of the `Sensor as a Table`
 * approach.
 */
class TrendActor(config:MsConf) extends ApiActor(config) with MsLogging {
  /**
   * [MsSql] is used to do the SQL query interpretation,
   * transformation to RocksDB commands and returning
   * the respective entries
   */
  private val msSql = new MsSql()
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

  override def getLogger: Logger = logger

}
