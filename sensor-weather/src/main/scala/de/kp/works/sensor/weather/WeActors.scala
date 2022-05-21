package de.kp.works.sensor.weather

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
import de.kp.works.sensor.weather.enums.WeTables

/**
 * The [WeAnomActor] supports the re-training
 * of the SensorBeat's anomaly detection model.
 *
 * Note, computing anomalies is regularly performed
 * on a scheduled basis, but can also be executed on
 * demand as well.
 */
class WeAnomActor(config:WeConf) extends DeepActor(config) with WeLogging {

  override def getWorker:ActorRef =
    system
      .actorOf(RoundRobinPool(instances)
        .withResizer(resizer)
        .props(Props(new WeLearnActor())), "AnomWorker")

  override def getLogger: Logger = logger

}

/**
 * The [WeForeActor] supports the re-training
 * of the SensorBeat's timeseries forecast model,
 * and also the provisioning of forecasted values.
 *
 * Note, computing time series forecasts is regularly
 * performed on a scheduled basis, but can also be
 * executed on demand as well.
 */
class WeForeActor(config:WeConf) extends DeepActor(config) with WeLogging {

  override def getWorker:ActorRef =
    system
      .actorOf(RoundRobinPool(instances)
        .withResizer(resizer)
        .props(Props(new WeLearnActor())), "ForeWorker")

  override def getLogger: Logger = logger

}

/**
 * The [WeInsightActor] supports the provisioning of
 * sensor event insights based on a SQL statement.
 * This actor is part of the `Sensor as a Table`
 * approach.
 */
class WeInsightActor(config:WeConf) extends InsightActor(config) with WeLogging {
  /**
   * [WeSql] is used to do the SQL query interpretation,
   * transformation to RocksDB commands and returning
   * the respective entries
   */
  private val msSql = new WeSql()

  override def read(sql:String):String =
    msSql.read(sql)

  override def getLogger: Logger = logger

}

/**
 * The [WeJobActor] supports the provisioning of
 * status information about deep learning jobs
 */
class WeJobActor(config:WeConf) extends JobActor(config) with WeLogging {

  override def getLogger: Logger = logger

}

/**
 * The [WeLearnActor] executes deep learning tasks,
 * either anomaly detection or timeseries fore-
 * casting. To this end, these tasks are defined
 * as queue entries and added to the task queue.
 *
 * The task queue distinguishes anomaly & forecast
 * jobs, and executes them on a configured and
 * scheduled basis.
 */
class WeLearnActor extends LearnActor with WeLogging {

  override def getLogger: Logger = logger

  override def validateTable(table: String): Unit =
    WeTables.withName(table)

}

/**
 * The [WeMonitorActor] supports the provisioning of
 * sensor events based on a SQL statement. This actor
 * is part of the `Sensor as a Table` approach.
 */
class WeMonitorActor(config:WeConf) extends MonitorActor(config) with WeLogging {
  /**
   * [WeSql] is used to do the SQL query interpretation,
   * transformation to RocksDB commands and returning
   * the respective entries
   */
  private val msSql = new WeSql()

  override def read(sql:String):String =
    msSql.read(sql)

  override def getLogger: Logger = logger

}

/**
 * The [WeTrendActor] supports the provisioning of
 * sensor event trends based on a SQL statement.
 * This actor is part of the `Sensor as a Table`
 * approach.
 */
class WeTrendActor(config:WeConf) extends TrendActor(config) with WeLogging {
  /**
   * [WeSql] is used to do the SQL query interpretation,
   * transformation to RocksDB commands and returning
   * the respective entries
   */
  private val msSql = new WeSql()

  override def trend(sql:String, indicator:String, timeframe:Int=5):String =
    msSql.trend(sql, indicator, timeframe)

  override def getLogger: Logger = logger

}
