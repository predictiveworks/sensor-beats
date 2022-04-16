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

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{BroadcastHub, Keep, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, DelayOverflowStrategy, OverflowStrategy}
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContextExecutor, Future}

trait BeatService {

  private var server:Option[Future[Http.ServerBinding]] = None
  private val uuid = java.util.UUID.randomUUID().toString
  /**
   * Akka 2.6 provides a default materializer out of the box, i.e., for Scala
   * an implicit materializer is provided if there is an implicit ActorSystem
   * available. This avoids leaking materializers and simplifies most stream
   * use cases somewhat.
   */
  implicit val system: ActorSystem = ActorSystem(s"sensor-beat-$uuid")
  implicit lazy val context: ExecutionContextExecutor = system.dispatcher

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  /**
   * Common timeout for all Akka connection
   */
  implicit val timeout: Timeout = Timeout(15.seconds)
  /**
   * Adjust subsequent variable to the sensor specific service
   */
  protected var config:BeatConf
  protected var serviceName:String
  /**
   * Public method to build sensor specific HTTP routes.
   * It leverages SSE `queue` & `source` and defines the
   * starting point of the SSE output channel.
   */
  def buildRoute(queue: SourceQueueWithComplete[String],
                 source: Source[ServerSentEvent, NotUsed]):Route
  /**
   * Method to build the HTTP(s) server that
   * defines the backbone of the SensorBeat
   */
  private def buildServer(route:Route):Unit = {

    val binding = config.getBindingCfg

    val host = binding.getString("host")
    val port = binding.getInt("port")

    val connectionContext = config.getConnectionContext
    server = connectionContext match {
      case Some(context) =>
        Http().setDefaultServerHttpContext(context)
        Some(Http().bindAndHandle(route, host, port, connectionContext = context))

      case None =>
        Some(Http().bindAndHandle(route , host, port))
    }

  }

  def start():Unit = {

    try {
      /*
       * STEP #1: Build configuration
       */
      if (!config.isInit) config.init()
      if (!config.isInit) {
        throw new Exception(s"Loading configuration failed and service is not started.")
      }
      /*
       * STEP #2: Build SSE queue & source
       *
       * The queue is used as an internal buffer that receives
       * events published during internal longer lasting processes.
       *
       * These events can be continuously used leveraging an EventSource
       *
       * The overflow strategy specified for the queue is backpressure,
       * and is used to avoid dropping events if the buffer is full.
       *
       * Instead, the returned Future does not complete until there is
       * space in the buffer and offer should not be called again until
       * it completes.
       *
       */
      lazy val (queue, source) =
        Source.queue[String](Int.MaxValue, OverflowStrategy.backpressure)
          .delay(1.seconds, DelayOverflowStrategy.backpressure)
        /*
         * The message type, used to distinguish SSE from multiple source,
         * is set equal to the provided `name`
         */
        .map(message => ServerSentEvent(message, Some(serviceName)))
        .keepAlive(1.second, () => ServerSentEvent.heartbeat)
        .toMat(BroadcastHub.sink[ServerSentEvent])(Keep.both)
        .run()

      /*
       * STEP #3: Build HTTP(s) route and
       * construct HTTP(s) server with this
       * route
       */
      buildServer(buildRoute(queue, source))
      /*
       * STEP #3: Invoke sensor specific after
       * start processing
       */
      onStart(queue)

    } catch {
      case t:Throwable =>
        system.terminate()

        println(t.getLocalizedMessage)
        System.exit(0)
    }

  }
  /**
   * Method to invoke the sensor specific after
   * start processing
   */
  def onStart(queue:SourceQueueWithComplete[String]):Unit

  def stop():Unit = {

    if (server.isEmpty) {
      system.terminate()
      throw new Exception(s"Service was not launched.")
    }

    server.get
      /*
       * Trigger unbinding from port
       */
      .flatMap(_.unbind())
      /*
       * Shut down application
       */
      .onComplete(_ => {
        system.terminate()
        System.exit(0)
      })

  }

}
