package de.kp.works.beats.sensor.api

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

import akka.actor.Actor
import akka.http.scaladsl.model.HttpRequest
import ch.qos.logback.classic.Logger
import com.google.gson.JsonObject
import de.kp.works.beats.sensor.BeatTasks.{ANOMALY, FORECAST}
import de.kp.works.beats.sensor.dl.{BeatQueue, QueueEntry}
import de.kp.works.beats.sensor.{BeatConf, BeatJobs, BeatMessages, BeatRocks, BeatTasks}

/**
 * The [DeepActor] invokes deep learning tasks,
 * either anomaly detection or timeseries fore-
 * casting. To this end, these tasks are defined
 * as queue entries and added to the task queue.
 *
 * The task queue distinguishes anomaly & forecast
 * jobs, and executes them on a configured and
 * scheduled basis.
 */
abstract class DeepActor extends Actor {

  override def receive: Receive = {

    case request:DeepReq =>
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
        validateTable(request.table)
        /*
         * Check whether the RocksDB is initialized
         */
        if (!BeatRocks.isInit)
          throw new Exception(BeatMessages.rocksNotInitialized())
        /*
         * Build queue entry
         */
        val qe = QueueEntry(
          id        = request.id,
          createdAt = System.currentTimeMillis,
          table     = request.table,
          startTime = request.startTime,
          endTime   = request.endTime)
        /*
         * Check and distinguish between the supported
         * deep learning tasks. Specific task is added
         * to the respective deep learning queue, that
         * executed tasks on a scheduled basis
         */
        BeatTasks.withName(request.task) match {
          case ANOMALY =>
            BeatQueue.addAnomaly(qe)

          case FORECAST =>
            BeatQueue.addForecast(qe)
        }

      } catch {
        case t:Throwable =>
          getLogger.error(BeatMessages.deepFailed(t))
      }
  }

  def getLogger:Logger
  /**
   * Public method to validate whether the externally
   * provided table is supported.
   */
  def validateTable(table:String):Unit

}

/**
 * The [JobActor] supports the provisioning of
 * status information about deep learning jobs
 */
abstract class JobActor[C <: BeatConf](config:C) extends ApiActor(config) {
  /**
   * The response of this request is a JsonObject;
   * in case of an invalid request, an empty response
   * is returned
   */
  private val emptyResponse = new JsonObject

  override def execute(request: HttpRequest): String = {

    val json = getBodyAsJson(request)
    if (json == null) {
      getLogger.warn(BeatMessages.invalidJson())
      return emptyResponse.toString
    }

    val req = mapper.readValue(json.toString, classOf[JobReq])
    val jid = req.id
    /*
     * Validate job identifier
     */
    if (jid.isEmpty) {
      getLogger.warn(BeatMessages.emptyJob())
      return emptyResponse.toString
    }

    try {
      val job = BeatJobs.get(jid)
      mapper.writeValueAsString(job)

    } catch {
      case t:Throwable =>
        getLogger.error(BeatMessages.insightFailed(t))
        emptyResponse.toString
    }

  }

}
