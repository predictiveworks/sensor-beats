package de.kp.works.beats.sensor.dl

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

import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.{BeatConf, BeatTasks}

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
/**
 * The [BeatMonitor] controls anomaly detection and
 * timeseries forecasting tasks, and, in combination
 * with the [BeatQueue] executes the respective tasks
 */
abstract class BeatMonitor[C <: BeatConf](config:C, task:BeatTasks.Value, numThreads:Int = 1) {

  private var logger:Logger = _
  /**
   * The configured time interval the deep learning
   * task is executed
   */
  private val interval =
    config.getSchedulerIntervals(task)
  /**
   * The executor service that is responsible
   */
  private var executorService:ScheduledExecutorService = _

  def setLogger(logger:Logger):Unit = {
    this.logger = logger
  }

  def start[T <: BeatWorker](worker:T):Unit = {
    /*
     * Define runnable to execute the provided
     * deep learning task
     */
    val runnable = new Runnable {
      override def run(): Unit = {
        /*
         * STEP #1: Determine whether there is a
         * deep learning task in the respective
         * queue (anomaly & forecast)
         */
        val qe = getQueueEntry
        /*
         * STEP #2: Execute the deep learning task
         * that refers to a non-empty queue entry
         */
        if (qe.nonEmpty) {
          if (logger != null)
            logger.info(s"Deep learning task `${task.toString}` started.")

          worker.execute(
            qe.get.id, qe.get.table, qe.get.startTime, qe.get.endTime)

        }
      }
    }

    try {

      executorService = Executors.newScheduledThreadPool(numThreads)
      executorService.scheduleAtFixedRate(runnable, 0, interval, TimeUnit.SECONDS)


    } catch {
      case t: Exception =>
        if (logger != null)
          logger.error(s"Beat Monitor failed: ${t.getLocalizedMessage}")

        stop()
    }

  }
  /**
   * Public method to retrieve the next
   * deep learning queue entry
   */
  def getQueueEntry:Option[QueueEntry]

  def stop(): Unit = {

    executorService.shutdown()
    executorService.shutdownNow()

  }

}