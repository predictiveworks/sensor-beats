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

import akka.actor.ActorRef
import akka.pattern.ask
import akka.http.scaladsl.model.{HttpProtocols, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, extractRequest, path, post, separateOnSlashes, toStrictEntity}
import akka.http.scaladsl.server.{RequestContext, Route, RouteResult}
import akka.util.{ByteString, Timeout}
import de.kp.works.beats.sensor.api.BeatActor.Response

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.{Failure, Success}

trait BaseRoute extends CORS {
  /**
   * Common timeout for all Akka connections
   */
  val duration: FiniteDuration = 15.seconds
  implicit val timeout: Timeout = Timeout(duration)

  /*******************************
   *
   * HELPER METHODS
   *
   */
  def routePost(url:String, actor:ActorRef):Route = {
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

  def extract(actor:ActorRef): Route = {
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

  def extractOptions: RequestContext => Future[RouteResult] = {
    extractRequest { _ =>
      complete {
        baseResponse
      }
    }
  }

  def baseResponse: HttpResponse = {

    val response = HttpResponse(
      status=StatusCodes.OK,
      protocol = HttpProtocols.`HTTP/1.1`)

    addCorsHeaders(response)

  }

  def jsonResponse(message:String): HttpResponse = {

    HttpResponse(
      status=StatusCodes.OK,
      entity = ByteString(message),
      protocol = HttpProtocols.`HTTP/1.1`)

  }

}
