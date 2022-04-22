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

import ch.qos.logback.classic.Logger
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

trait BeatTask {

  def execute():Unit

}

abstract class BeatMonitor[C <: BeatConf](config:C, task:String, numThreads:Int = 1) {

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

  def start[T <: BeatTask](beatTask:T):Unit = {
    /*
     * Define worker to execute the provided
     * deep learning task
     */
    val worker = new Runnable {
      override def run(): Unit = {
        if (logger != null)
          logger.info(s"Deep learning task `$task` is started.")

        beatTask.execute()
      }
    }

    try {

      executorService = Executors.newScheduledThreadPool(numThreads)
      executorService.scheduleAtFixedRate(worker, 0, interval, TimeUnit.SECONDS)


    } catch {
      case t: Exception =>
        if (logger != null)
          logger.error(s"Beat Monitor failed: ${t.getLocalizedMessage}")

        stop()
    }

  }

  def stop(): Unit = {

    executorService.shutdown()
    executorService.shutdownNow()

  }

}