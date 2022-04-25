package de.kp.works.beats.sensor.dl

import scala.collection.mutable

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

case class QueueEntry(
  createdAt:Long,
  /*
   * Database table specification
   */
  table:String,
  startTime:Long,
  endTime:Long
)

/**
 * [BeatQueue] is responsible for queuing deep learning
 * tasks to synchronize adhoc and scheduled tasks.
 *
 * It leverages the [BeatJobs] registry to determine
 * whether a certain queued job can be executed or
 * not.
 *
 * In case of an empty queue, [BeatQueue] adds background
 * tasks for anomaly detection and timeseries forecasting
 */
object BeatQueue {

  private val anomalies = new mutable.Queue[QueueEntry]()
  private val forecasts = new mutable.Queue[QueueEntry]()
  /**
   * Public method to add another anomaly detection
   * task to the deep learning task queue
   */
  def addAnomaly(qe:QueueEntry):Unit = {
    anomalies.enqueue(qe)
  }
  /**
   * Public method to retrieve the `oldest` anomaly
   * detection task from the queue
   */
  def getAnomaly:Option[QueueEntry] = {
    /*
     * [Queue] is a FIFO and `dequeue` removes
     * an element from the head of the queue
     */
    if (anomalies.isEmpty) None
    else {
      val qe = anomalies.dequeue()
      Some(qe)
    }
  }
  /**
   * Public method to add another timeseries
   * forecasting task to the deep learning
   * task queue
   */
  def addForecast(qe:QueueEntry):Unit = {
    forecasts.enqueue(qe)
  }
  /**
   * Public method to retrieve the `oldest` time
   * series forecasting task from the queue
   */
  def getForecast:Option[QueueEntry] = {
    /*
     * [Queue] is a FIFO and `dequeue` removes
     * an element from the head of the queue
     */
    if (forecasts.isEmpty) None
    else {
      val qe = forecasts.dequeue()
      Some(qe)
    }
  }

}
