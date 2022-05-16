package de.kp.works.beats.sensor.ellenex

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
import de.kp.works.beats.sensor.ellenex.enums.ExTables

/**
 * The [ExAnomActor] supports the re-training
 * of the SensorBeat's anomaly detection model.
 *
 * Note, computing anomalies is regularly performed
 * on a scheduled basis, but can also be executed on
 * demand as well.
 */
class ExAnomActor(config:ExConf) extends DeepActor(config) with ExLogging {

  override def getWorker:ActorRef =
    system
      .actorOf(RoundRobinPool(instances)
        .withResizer(resizer)
        .props(Props(new ExLearnActor())), "AnomWorker")

  override def getLogger: Logger = logger

}

/**
 * The [ExForeActor] supports the re-training
 * of the SensorBeat's timeseries forecast model,
 * and also the provisioning of forecasted values.
 *
 * Note, computing time series forecasts is regularly
 * performed on a scheduled basis, but can also be
 * executed on demand as well.
 */
class ExForeActor(config:ExConf) extends DeepActor(config) with ExLogging {

  override def getWorker:ActorRef =
    system
      .actorOf(RoundRobinPool(instances)
        .withResizer(resizer)
        .props(Props(new ExLearnActor())), "ForeWorker")

  override def getLogger: Logger = logger

}

/**
 * The [ExInsightActor] supports the provisioning of
 * sensor event insights based on a SQL statement.
 * This actor is part of the `Sensor as a Table`
 * approach.
 */
class ExInsightActor(config:ExConf) extends InsightActor(config) with ExLogging {
  /**
   * [ExSql] is used to do the SQL query interpretation,
   * transformation to RocksDB commands and returning
   * the respective entries
   */
  private val exSql = new ExSql()

  override def read(sql:String):String =
    exSql.read(sql)

  override def getLogger: Logger = logger

}

/**
 * The [ExJobActor] supports the provisioning of
 * status information about deep learning jobs
 */
class ExJobActor(config:ExConf) extends JobActor(config) with ExLogging {

  override def getLogger: Logger = logger

}

/**
 * The [MsLearnActor] executes deep learning tasks,
 * either anomaly detection or timeseries fore-
 * casting. To this end, these tasks are defined
 * as queue entries and added to the task queue.
 *
 * The task queue distinguishes anomaly & forecast
 * jobs, and executes them on a configured and
 * scheduled basis.
 */
class ExLearnActor extends LearnActor with ExLogging {

  override def getLogger: Logger = logger

  override def validateTable(table: String): Unit =
    ExTables.withName(table)

}

/**
 * The [ExMonitorActor] supports the provisioning of
 * sensor events based on a SQL statement. This actor
 * is part of the `Sensor as a Table` approach.
 */
class ExMonitorActor(config:ExConf) extends MonitorActor(config) with ExLogging {
  /**
   * [ExSql] is used to do the SQL query interpretation,
   * transformation to RocksDB commands and returning
   * the respective entries
   */
  private val exSql = new ExSql()

  override def read(sql:String):String =
    exSql.read(sql)

  override def getLogger: Logger = logger

}

/**
 * The [ExTrendActor] supports the provisioning of
 * sensor event trends based on a SQL statement.
 * This actor is part of the `Sensor as a Table`
 * approach.
 */
class ExTrendActor(config:ExConf) extends TrendActor(config) with ExLogging {
  /**
   * [ExSql] is used to do the SQL query interpretation,
   * transformation to RocksDB commands and returning
   * the respective entries
   */
  private val exSql = new ExSql()

  override def trend(sql:String, indicator:String, timeframe:Int=5):String =
    exSql.trend(sql, indicator, timeframe)

  override def getLogger: Logger = logger

}
