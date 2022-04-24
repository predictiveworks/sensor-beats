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
import de.kp.works.beats.sensor._
import de.kp.works.beats.sensor.milesight.channel.{MsFiware, MsRocks}
/**
 * [MsService] is built to manage the various micro
 * services used to provide the REST API and also
 * the multiple output channels
 */
class MsService(config:MsConf) extends BeatService[MsConf](config) {

  override protected var serviceName: String = "MsService"
  /**
   * Initialize the local RocksDB storage
   */
  private val options = new MsOptions(config)
  MsRocksApi.getInstance(options)

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
   *
   * +---------- REST API ----------
   * :
   * : - train anomaly detection model &
   * :   retrieve anomalies
   * :
   * : - train time series prediction model &
   * :   retrieve forecasts
   * :
   * : - retrieve sensor readings via SQL query
   * :
   * : - retrieve sensor trends via SQL query
   * :
   * : - provide sensor events & inferred info
   * :   via Server Sent Event listing
   * :
   * +------------------------------
   */
  override def buildApiActors(queue: SourceQueueWithComplete[String]): Map[String, ActorRef] = {

    Map(
      /*
       * Train anomaly detection model &
       * retrieve anomalies
       */
      BEAT_ANOMALY_ACTOR ->
        system.actorOf(Props(new AnomalyActor(queue)), BEAT_ANOMALY_ACTOR),
      /*
       * Train time series prediction model
       * & retrieve forecasts
       */
      BEAT_FORECAST_ACTOR ->
        system.actorOf(Props(new ForecastActor(queue)), BEAT_FORECAST_ACTOR),
      /*
       * Retrieve sensor inferred readings via SQL query
       */
      BEAT_INSIGHT_ACTOR ->
        system.actorOf(Props(new InsightActor(queue)), BEAT_INSIGHT_ACTOR),
      /*
       * Retrieve sensor readings via SQL query
       */
      BEAT_MONITOR_ACTOR ->
        system.actorOf(Props(new MonitorActor(queue)), BEAT_MONITOR_ACTOR),
      /*
       * Retrieve sensor trend via SQL query
       */
      BEAT_TREND_ACTOR ->
      system.actorOf(Props(new TrendActor(queue)), BEAT_TREND_ACTOR)

    )

  }
  /**
   * Public method to register the micro services that
   * provided the configured output channels
   *
   * +---------- Output Channels ----------
   * :
   * : - Send NGSIv2 compliant sensor event
   * :   to Fiware Context Broker
   * :
   * : - Send NGSIv2 compliant sensor event
   * :   to the local storage engine
   * :
   * : - Send NGSIv2 compliant sensor event
   * :   to Server-Sent-Event queue
   *
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
           * Build RocksDB specific output channel;
           * note, the storage should be initialized
           * at this stage already
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
    /*
     * Start the scheduled anomaly detection and
     * timeseries forecasting tasks
     */

    // TODO

  }
}
