package de.kp.works.beats.sensor.dl.forecast

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

import de.kp.works.beats.sensor.{BeatConf, BeatTasks}
import de.kp.works.beats.sensor.dl.{BeatMonitor, BeatQueue, QueueEntry}
/**
 * The [ForeMonitor] is responsible for executing
 * queued timeseries forecasting tasks (or jobs)
 */
class ForeMonitor[C <: BeatConf](config:C, numThreads:Int = 1)
  extends BeatMonitor[C](config, BeatTasks.FORECAST, numThreads) {
  /**
   * Public method to retrieve the next deep learning
   * queue entry; if the timeseries forecasting queue
   * is empty, this method creates the next deep learning
   * tasks and adds them to the queue.
   */
  override def getQueueEntry: Option[QueueEntry] = {
    val qe = BeatQueue.getAnomaly
    if (qe.isEmpty) {
      /*
       * Create the next timeseries forecasting
       * task for each specific column family of
       * the supported sensor
       */
      val createdAt = System.currentTimeMillis
      config.getRocksTables
        .filter(table => table.endsWith("_fore"))
        .foreach(table => {
        /*
         * Each deep learning request receives a unique
         * job identifier
         */
        val jid = s"job-${java.util.UUID.randomUUID.toString}"
        BeatQueue.addForecast(
          QueueEntry(
            id        = jid,
            createdAt = createdAt,
            table     = table,
            startTime = 0L,
            endTime   = 0L)
        )
      })
    }
    /*
     * In both cases the current queue findings
     * are exposed to the forecast worker controlled
     * by the [ForeMonitor]
     */
    qe
  }

}
