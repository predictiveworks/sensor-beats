package de.kp.works.beats.sensor.uradmonitor

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
import akka.routing.RoundRobinPool
import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.api._

/**
 * The [UmAnomActor] supports the re-training
 * of the SensorBeat's anomaly detection model.
 *
 * Note, computing anomalies is regularly performed
 * on a scheduled basis, but can also be executed on
 * demand as well.
 */
class UmAnomActor(config:UmConf) extends DeepActor(config) with UmLogging {

  override def getWorker:ActorRef =
    system
      .actorOf(RoundRobinPool(instances)
        .withResizer(resizer)
        .props(Props(new UmLearnActor())), "AnomWorker")

  override def getLogger: Logger = logger

}

/**
 * The [UmForeActor] supports the re-training
 * of the SensorBeat's timeseries forecast model,
 * and also the provisioning of forecasted values.
 *
 * Note, computing time series forecasts is regularly
 * performed on a scheduled basis, but can also be
 * executed on demand as well.
 */
class UmForeActor(config:UmConf) extends DeepActor(config) with UmLogging {

  override def getWorker:ActorRef =
    system
      .actorOf(RoundRobinPool(instances)
        .withResizer(resizer)
        .props(Props(new UmLearnActor())), "ForeWorker")

  override def getLogger: Logger = logger

}

/**
 * The [UmInsightActor] supports the provisioning of
 * sensor event insights based on a SQL statement.
 * This actor is part of the `Sensor as a Table`
 * approach.
 */
class UmInsightActor(config:UmConf) extends InsightActor(config) with UmLogging {
  /**
   * [UmSql] is used to do the SQL query interpretation,
   * transformation to RocksDB commands and returning
   * the respective entries
   */
  private val umSql = new UmSql()

  override def read(sql:String):String =
    umSql.read(sql)

  override def getLogger: Logger = logger

}

/**
 * The [UmJobActor] supports the provisioning of
 * status information about deep learning jobs
 */
class UmJobActor(config:UmConf) extends JobActor(config) with UmLogging {

  override def getLogger: Logger = logger

}

/**
 * The [UmLearnActor] executes deep learning tasks,
 * either anomaly detection or timeseries fore-
 * casting. To this end, these tasks are defined
 * as queue entries and added to the task queue.
 *
 * The task queue distinguishes anomaly & forecast
 * jobs, and executes them on a configured and
 * scheduled basis.
 */
class UmLearnActor extends LearnActor with UmLogging {

  override def getLogger: Logger = logger

}

/**
 * The [UmMonitorActor] supports the provisioning of
 * sensor events based on a SQL statement. This actor
 * is part of the `Sensor as a Table` approach.
 */
class UmMonitorActor(config:UmConf) extends MonitorActor(config) with UmLogging {
  /**
   * [UmSql] is used to do the SQL query interpretation,
   * transformation to RocksDB commands and returning
   * the respective entries
   */
  private val umSql = new UmSql()

  override def read(sql:String):String =
    umSql.read(sql)

  override def getLogger: Logger = logger

}

/**
 * The [UmTrendActor] supports the provisioning of
 * sensor event trends based on a SQL statement.
 * This actor is part of the `Sensor as a Table`
 * approach.
 */
class UmTrendActor(config:UmConf) extends TrendActor(config) with UmLogging {
  /**
   * [UmSql] is used to do the SQL query interpretation,
   * transformation to RocksDB commands and returning
   * the respective entries
   */
  private val umSql = new UmSql()

  override def trend(sql:String, indicator:String, timeframe:Int=5):String =
    umSql.trend(sql, indicator, timeframe)

  override def getLogger: Logger = logger

}
