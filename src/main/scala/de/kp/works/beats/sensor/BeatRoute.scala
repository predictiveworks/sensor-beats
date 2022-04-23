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
import akka.http.scaladsl.model.{HttpProtocols, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.pattern.ask
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import de.kp.works.beats.sensor.api.ApiActor.Response

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.{Failure, Success}

object BeatRoute {

  val BEAT_ANOMALY_ACTOR  = "beat_anomaly_actor"
  val BEAT_FORECAST_ACTOR = "beat_forecast_actor"
  val BEAT_MONITOR_ACTOR  = "beat_monitor_actor"
  val BEAT_TREND_ACTOR    = "beat_trend_actor"

}

class BeatRoute(
  actors:Map[String, ActorRef], source:Source[ServerSentEvent, NotUsed])
  (implicit system: ActorSystem) extends CORS {

  implicit lazy val context: ExecutionContextExecutor = system.dispatcher
  /**
   * Common timeout for all Akka connections
   */
  val duration: FiniteDuration = 15.seconds
  implicit val timeout: Timeout = Timeout(duration)

  import BeatRoute._

  def getRoutes: Route = {

    getAnomaly ~
    getEvent ~
    getForecast ~
    getMonitor ~
    getTrend

  }

  /** EVENT **/

  /*
   * This is the Server Sent Event route
   */
  def getEvent:Route = {

    path("beat" / "v1" / "event") {
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
   * `SensorBeat` API route to invoke monitoring
   * time series
   */
  private def getMonitor:Route = routePost("beat/v1/monitor", actors(BEAT_MONITOR_ACTOR))
  /**
   * `SensorBeat` API route to invoke technical
   * trend analysis of the time series
   */
  private def getTrend:Route = routePost("beat/v1/trend", actors(BEAT_TREND_ACTOR))

  /*******************************
   *
   * HELPER METHODS
   *
   */
  private def routePost(url:String, actor:ActorRef):Route = {
    val matcher = separateOnSlashes(url)
    path(matcher) {
      post {
        /*
         * The client sends sporadic [HttpEntity.Default]
         * requests; the [BaseActor] is not able to extract
         * the respective JSON body from.
         *
         * As a workaround, the (small) request is made
         * explicitly strict
         */
        toStrictEntity(duration) {
          extract(actor)
        }
      }
    }
  }

  private def extract(actor:ActorRef) = {
    extractRequest { request =>
      complete {
        /*
         * The Http(s) request is sent to the respective
         * actor and the actor' response is sent to the
         * requester as response.
         */
        val future = actor ? request
        Await.result(future, timeout.duration) match {
          case Response(Failure(e)) =>
            val message = e.getMessage
            jsonResponse(message)
          case Response(Success(answer)) =>
            val message = answer.asInstanceOf[String]
            jsonResponse(message)
        }
      }
    }
  }

  private def extractOptions: RequestContext => Future[RouteResult] = {
    extractRequest { _ =>
      complete {
        baseResponse
      }
    }
  }

  private def baseResponse: HttpResponse = {

    val response = HttpResponse(
      status=StatusCodes.OK,
      protocol = HttpProtocols.`HTTP/1.1`)

    addCorsHeaders(response)

  }

  private def jsonResponse(message:String) = {

    HttpResponse(
      status=StatusCodes.OK,
      entity = ByteString(message),
      protocol = HttpProtocols.`HTTP/1.1`)

  }


}
