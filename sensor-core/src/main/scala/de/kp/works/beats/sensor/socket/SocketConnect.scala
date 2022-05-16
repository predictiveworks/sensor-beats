package de.kp.works.beats.sensor.socket

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

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.{ConnectionContext, Http}
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
/**
 * The [SocketConnect] trait is e.g. used by the Loriot consumer,
 * as the Sensor Beat project leverages the recommended application
 * output of the Loriot server
 */
trait SocketConnect {

  private val uuid = java.util.UUID.randomUUID.toString
  implicit val socketSystem: ActorSystem = ActorSystem(s"socket-connect-$uuid")

  implicit lazy val socketContext: ExecutionContextExecutor = socketSystem.dispatcher
  implicit val socketMaterializer: ActorMaterializer = ActorMaterializer()
  /**
   * The connection context
   */
  private var connectionContext:Option[ConnectionContext] = None

  def setContext(context:Option[ConnectionContext]):Unit = {
    connectionContext = context
  }

  def connect(serverUrl:String, handler:SocketHandler):Unit = {

    val publisher: Sink[Message, Future[Done]] =
      Sink.foreach {
        case message: TextMessage.Strict =>
          handler.handleMessage(message.toString)

        case _ =>
        /* Ignore other message types */
      }

    val flow: Flow[Message, Message, Future[Done]] =
      Flow.fromSinkAndSourceMat(publisher, Source.maybe)(Keep.left)
    /*
     * Build web socket request and distinguish
     * between a secure and non-secure connection
     */
    val req = WebSocketRequest(uri = serverUrl)
    val (upgrade, closed) =
      if (connectionContext.isEmpty)
        Http().singleWebSocketRequest(req, flow)

      else
        Http().singleWebSocketRequest(req, flow,
          connectionContext = connectionContext.get)
    /*
     * `upgrade` is a Future[WebSocketUpgradeResponse]
     * that completes or fails when the connection succeeds
     * or fails
     */
    upgrade.onComplete {
      case Success(s) =>
        /*
         * Just like a regular http request we can access response
         * status which is available via value.response.status
         */
        if (s.response.status == StatusCodes.SwitchingProtocols) {
          handler.handleConnect(true)

        } else {
          handler.handleConnect(false)

        }
      case Failure(f) =>
        handler.handleConnect(false)
    }
    /*
     * `closed` is a Future[Done] representing the stream completion
     */
    closed.onComplete {
      case Success(_) => handler.handleClose(true)
      case Failure(_) => handler.handleClose(false)
    }

  }
}
