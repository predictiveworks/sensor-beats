package de.kp.works.beats.sensor
/**
 * Copyright (c) 2020 - 2022 Dr. Krusche & Partner PartG. All rights reserved.
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

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.scaladsl.Source

import scala.concurrent.ExecutionContextExecutor

object BeatRoute {

  val BEAT_ANOMALY_ACTOR  = "beat_anomaly_actor"
  val BEAT_FORECAST_ACTOR = "beat_forecast_actor"
  val BEAT_INSIGHT_ACTOR  = "beat_insight_actor"
  val BEAT_JOB_ACTOR      = "beat_job_actor"
  val BEAT_MONITOR_ACTOR  = "beat_monitor_actor"
  val BEAT_TREND_ACTOR    = "beat_trend_actor"

}

class BeatRoute(
  actors:Map[String, ActorRef], source:Source[ServerSentEvent, NotUsed])
  (implicit system: ActorSystem) extends BaseRoute {

  implicit lazy val context: ExecutionContextExecutor = system.dispatcher

  import BeatRoute._

  def getRoutes: Route = {

    getAnomaly ~
    getEvent ~
    getForecast ~
    getInsight ~
    getJob ~
    getMonitor ~
    getTrend

  }

  /**
   * SSE EVENTS
   *
   * This is the Server Sent Event route; note, the respective
   * endpoint is harmonized with Works Beat services to enable
   * a unified shipping e.g. to Apache Ignite caches.
   */
  def getEvent:Route = {

    path("sensor" / "stream") {
      options {
        extractOptions
      } ~
      Directives.get {
        addCors(
          complete {
            source
          }
        )
      }
    }
  }
  /**
   * `SensorBeat` API route to invoke anomaly
   * detection and inference
   */
  private def getAnomaly:Route = routePost("beat/v1/anomaly", actors(BEAT_ANOMALY_ACTOR))
  /**
   * `SensorBeat` API route to invoke time series
   * forecasts and inference
   */
  private def getForecast:Route = routePost("beat/v1/forecast", actors(BEAT_FORECAST_ACTOR))
  /**
   * `SensorBeat` API route to read inferred
   * time series
   */
  private def getInsight:Route = routePost("beat/v1/insight", actors(BEAT_INSIGHT_ACTOR))
  /**
   * `SensorBeat` API route to read the status
   * of a deep learning job (or task)
   */
  private def getJob:Route = routePost("beat/v1/job", actors(BEAT_JOB_ACTOR))
  /**
   * `SensorBeat` API route to invoke monitoring
   * time series
   */
  private def getMonitor:Route = routePost("beat/v1/monitor", actors(BEAT_MONITOR_ACTOR))
  /**
   * `SensorBeat` API route to invoke technical
   * trend analysis of the time series
   */
  private def getTrend:Route = routePost("beat/v1/trend", actors(BEAT_TREND_ACTOR))

}
