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

import akka.NotUsed
import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Source, SourceQueueWithComplete}
import de.kp.works.beats.sensor.Channels.{FIWARE, ROCKS_DB, SSE}
import de.kp.works.beats.sensor.{BeatChannels, BeatRoute, BeatService, BeatSse, Channels}
/**
 * [MsService] is built to manage the various micro
 * services used to provide the REST API and also
 * the multiple output channels
 */
class MsService(config:MsConf) extends BeatService[MsConf](config) {

  override protected var serviceName: String = "MsService"

  import BeatRoute._
  override def buildRoute(queue: SourceQueueWithComplete[String],
                          source: Source[ServerSentEvent, NotUsed]): Route = {

    val actors = buildApiActors(queue)
    val beatRoute = new BeatRoute(actors, source)

    beatRoute.getRoutes

  }

  /**
   * Public method to build the micro services (actors)
   * that refer to the REST API of the `SensorBeat`
   */
  override def buildApiActors(queue: SourceQueueWithComplete[String]): Map[String, ActorRef] = {

    Map(
      BEAT_ANOMALY_ACTOR ->
        system.actorOf(Props(new AnomalyActor(queue)), BEAT_ANOMALY_ACTOR),

      BEAT_FORECAST_ACTOR ->
        system.actorOf(Props(new ForecastActor(queue)), BEAT_FORECAST_ACTOR),

      BEAT_MONITOR_ACTOR ->
        system.actorOf(Props(new MonitorActor(queue)), BEAT_MONITOR_ACTOR)

    )

  }
  /**
   * Public method to register the micro services that
   * provided the configured output channels
   */
  override def onStart(queue: SourceQueueWithComplete[String]): Unit = {
    /*
     * Create the micro services that control the
     * publishing of the incoming events;
     *
     * the current implementation supports the
     * Fiware Context Broker (Fiware), the RocksDB
     * local storage, and Server Sent Events.
     */
    val options = new MsOptions(config)
    options.getChannels.foreach(channelName => {
      Channels.withName(channelName) match {
        case FIWARE =>
          /*
           * Build Fiware specific output channel
           */
          val props = Props(new MsFiware(options))
          BeatChannels.registerChannel(channelName, props)

        case ROCKS_DB =>
          /*
           * Build RocksDB specific output channel
           */
          val props = Props(new MsRocks(options))
          BeatChannels.registerChannel(channelName, props)

        case SSE =>
          /*
           * Build SSE specific output channel
           */
          val props = Props(new BeatSse(queue))
          BeatChannels.registerChannel(channelName, props)

        case _ => /* Do nothing */
      }

    })

  }
}
