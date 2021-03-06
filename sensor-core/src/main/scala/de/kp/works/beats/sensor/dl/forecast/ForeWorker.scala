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

import akka.stream.scaladsl.SourceQueueWithComplete
import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.{BeatJob, BeatJobs, BeatStatuses}
import de.kp.works.beats.sensor.dl.BeatWorker
import org.apache.spark.sql.SparkSession

/**
 * [ForeWorker] is responsible for executing the
 * deep learning timeseries forecasting task. This task
 * can be executed either on an adhoc basis (API) or
 * scheduled
 */
class ForeWorker(
  queue:SourceQueueWithComplete[String], session:SparkSession, logger:Logger)
  extends BeatWorker(queue, session, logger) {

  private val provider = getClass.getName
  private val FORECAST_10_EVENT = "Dataset loaded."

  override def execute(jid:String, table:String, start:Long, end:Long): Unit = {

    startts = System.currentTimeMillis
    currts  = startts
    /*
     * STEP #1: Inform the [BeatJobs] that the respective
     * deep learning task was started.
     */
    val beatJob = BeatJob(
      id = jid, createdAt = startts, updatedAt = 0L, status = BeatStatuses.STARTED)

    BeatJobs.register(beatJob)
    /*
     * STEP #2: Load specified dataframe
     */
    val dataframe = loadDataset(table, start, end)

    nextts = System.currentTimeMillis
    if (verbose) println(s"Dataset loaded in ${nextts - currts} ms.")

    if (queue != null) publisher.pushEvent(FORECAST_10_EVENT, provider, "train", 0.1)

    ???
  }

}
