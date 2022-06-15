package de.kp.works.beats.sensor.weather.owea

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

import de.kp.works.beats.sensor.weather.{WeLogging, WeOptions}

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

class WeMonitor(options:WeOptions, numThreads:Int = 1) extends WeLogging {
  /**
   * The configured time interval the consumer task
   * is executed
   */
  private val interval = options.getTimeInterval

  private var executorService:ScheduledExecutorService = _

  private val consumer = new WeConsumer(options)

  def start():Unit = {

    val worker = new Runnable {

      override def run(): Unit = {
        info(s"Weather Consumer started.")
        consumer.subscribeAndPublish()
      }
    }

    try {

      executorService = Executors.newScheduledThreadPool(numThreads)
      executorService.scheduleAtFixedRate(worker, 0, interval, TimeUnit.MILLISECONDS)


    } catch {
      case t:Exception =>
        error(s"Weather Monitor failed with: ${t.getLocalizedMessage}")
        stop()
    }

  }

  def stop():Unit = {

    executorService.shutdown()
    executorService.shutdownNow()

  }

}

