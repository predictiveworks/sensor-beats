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

import akka.stream.scaladsl.SourceQueueWithComplete
import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.BeatTasks.{ANOMALY, FORECAST}
import de.kp.works.beats.sensor.{BeatMessages, BeatRocks, BeatTasks}
import de.kp.works.beats.sensor.api.DeepReq
import de.kp.works.beats.sensor.dl.anomaly.AnomalyWorker
import de.kp.works.beats.sensor.dl.forecast.ForecastWorker

class MsInsight(queue: SourceQueueWithComplete[String], logger:Logger) {
  /**
   * The deep learning workers for the anomaly detection
   * and time series forecasting tasks. These worker are
   * invoked on an adhoc basis here.
   */
  private def anomalyWorker = new AnomalyWorker(queue, logger)
  private def forecastWorker = new ForecastWorker(queue, logger)

  def execute(request:DeepReq):Unit = {
    /*
     * Note, adhoc deep learning tasks are queued, to be in
     * sync with the scheduled tasks; the current implementation
     * supports environments where only a small number of DL
     * tasks can be executed simultaneously.
     */
    try {
      /*
       * Check whether the provided table name
       * is defined
       */
      MsTables.withName(request.table)
      /*
       * Check whether the RocksDB is initialized
       */
      if (!BeatRocks.isInit)
        throw new Exception(BeatMessages.rocksNotInitialized())
      /*
       * Check and distinguish between the supported
       * deep learning tasks and execute task.
       */
      BeatTasks.withName(request.task) match {
        case ANOMALY =>
          anomalyWorker.execute(request.table, request.startTime, request.endTime)

        case FORECAST =>
          forecastWorker.execute(request.table, request.startTime, request.endTime)
      }

    } catch {
      case t:Throwable =>
        logger.error(BeatMessages.deepFailed(t))
    }
  }

}
