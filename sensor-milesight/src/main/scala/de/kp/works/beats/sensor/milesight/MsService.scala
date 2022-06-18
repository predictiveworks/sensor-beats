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
import de.kp.works.beats.sensor.BeatInputs.{HELIUM, LORIOT, THINGS_STACK}
import de.kp.works.beats.sensor.BeatOutputs.{FIWARE, ROCKS_DB, SSE, THINGSBOARD}
import de.kp.works.beats.sensor._
import de.kp.works.beats.sensor.dl.anomaly.{AnomMonitor, AnomWorker}
import de.kp.works.beats.sensor.dl.forecast.{ForeMonitor, ForeWorker}
import org.apache.spark.sql.BeatSession
/**
 * [MsService] is built to manage the various micro
 * services used to provide the REST API and also
 * the multiple output channels
 */
class MsService(config:MsConf) extends BeatService[MsConf](config) with MsLogging {

  override protected var serviceName: String = "MsService"
  private val options = new MsOptions(config)

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
      /*
       * Train anomaly detection model &
       * retrieve anomalies
       */
      BEAT_ANOMALY_ACTOR ->
        system.actorOf(Props(new MsAnomActor(config)), BEAT_ANOMALY_ACTOR),
      /*
       * Train time series prediction model
       * & retrieve forecasts
       */
      BEAT_FORECAST_ACTOR ->
        system.actorOf(Props(new MsForeActor(config)), BEAT_FORECAST_ACTOR),
      /*
       * Retrieve sensor inferred readings via SQL query
       */
      BEAT_INSIGHT_ACTOR ->
        system.actorOf(Props(new MsInsightActor(config)), BEAT_INSIGHT_ACTOR),
      /*
       * Retrieve sensor readings via SQL query
       */
      BEAT_MONITOR_ACTOR ->
        system.actorOf(Props(new MsMonitorActor(config)), BEAT_MONITOR_ACTOR),
      /*
       * Retrieve sensor trend via SQL query
       */
      BEAT_TREND_ACTOR ->
      system.actorOf(Props(new MsTrendActor(config)), BEAT_TREND_ACTOR)

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
   * :
   * : - Send NGSIv2 compliant sensor event
   * :   to ThingsBoard Mqtt Broker
   *
   * This method also build the micro services that
   * control the deep learning tasks associated with
   * the Milesight sensor readings.
   */
  override def onStart(queue: SourceQueueWithComplete[String]): Unit = {
    /*
     * Build the input channel for the Milesight
     * server as a service
     */
    buildInput()
    /*
     * Create the micro services that control the
     * publishing of the incoming events;
     */
    buildOutputs(queue)
    /*
     * Start the scheduled anomaly detection and
     * timeseries forecasting monitors
     */
    buildMonitors(queue)
  }

  private def buildInput():Unit = {

    options.getSource match {
      case HELIUM =>
        val helium = new MsHelium(options)
        helium.subscribeAndPublish()

      case LORIOT =>
        val loriot = new MsLoriot(options)
        loriot.subscribeAndPublish()

      case THINGS_STACK =>
        val stack = new MsStack(options)
        stack.subscribeAndPublish()

    }

  }
  /**
   * Create the micro services that control the
   * publishing of the incoming events;
   *
   * the current implementation supports the
   * Fiware Context Broker (Fiware), the RocksDB
   * local storage, ThingsBoard and Server Sent
   * Events.
   */
  private def buildOutputs(queue: SourceQueueWithComplete[String]):Unit = {

    options.getSinks.foreach {
      case channel@FIWARE =>
        /*
         * Build Fiware specific output channel
         */
        val props = Props(new MsFiware(options))
        BeatSinks.registerChannel(channel, props)

      case channel@ROCKS_DB =>
        /*
         * Build RocksDB specific output channel;
         * note, the storage should be initialized
         * at this stage already
         */
        val props = Props(new MsRocks(options))
        BeatSinks.registerChannel(channel, props)

      case channel@SSE =>
        /*
         * Build SSE specific output channel
         */
        val props = Props(new BeatSse(queue))
        BeatSinks.registerChannel(channel, props)

      case channel@THINGSBOARD =>
        /*
         * Build ThingsBoard specific output channel
         */
        val props = Props(new MsBoard(options))
        BeatSinks.registerChannel(channel, props)

      case _ => /* Do nothing */
    }

  }
  /**
   * Start the scheduled anomaly detection and
   * timeseries forecasting monitors
   */
  private def buildMonitors(queue: SourceQueueWithComplete[String]):Unit = {

    val session = BeatSession.getSession
    /*
     * Build & initialize the `AnomWorker` and
     * the respective monitor
     */
    val anomWorker = new AnomWorker(queue, session, logger)

    val anomThreads = config.getNumThreads(BeatTasks.ANOMALY)
    val anomMonitor = new AnomMonitor[MsConf](config, anomThreads)
    /*
     * Build & initialize the `ForeWorker` and
     * the respective monitor
     */
    val foreWorker = new ForeWorker(queue, session, logger)

    val foreThreads = config.getNumThreads(BeatTasks.FORECAST)
    val foreMonitor = new ForeMonitor[MsConf](config,foreThreads)

    anomMonitor.start[AnomWorker](anomWorker)
    foreMonitor.start[ForeWorker](foreWorker)

  }
}
