package de.kp.works.beats.sensor.weather.pvlib

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

import akka.actor.ActorRef
import akka.stream.scaladsl.SourceQueueWithComplete
import com.google.gson.JsonObject
import PVlibMethods.{NOT_DEFINED, PVlibMethod}
import PVlibStatuses.PVlibStatus

import java.time.Instant
import scala.collection.mutable
/**
 * This enumeration defines the methods, the
 * Python `pvlib` wrapper supports
 */
object PVlibMethods extends Enumeration {
  type PVlibMethod = Value

  val POSITIONS:PVlibMethod    = Value(1, "positions")
  val DNI_DIRINT:PVlibMethod   = Value(2, "dni_dirint")
  val DNI_DISC:PVlibMethod     = Value(3, "dni_disc")
  val DNI_ERBS:PVlibMethod     = Value(4, "dni_erbs")
  val SYSTEM_DISC:PVlibMethod  = Value(5, "system_disc")
  val WEATHER_DISC:PVlibMethod = Value(6, "weather_disc")
  val NOT_DEFINED:PVlibMethod  = Value(7, "not_defined")

}
/**
 * This enumeration specifies the statuses
 * of the Python `pvlib` tasks.
 */
object PVlibStatuses extends Enumeration {
  type PVlibStatus = Value

  val NOT_STARTED:PVlibStatus = Value(1, "not_started")
  val STARTED:PVlibStatus     = Value(2, "started")
  val FINISHED:PVlibStatus    = Value(3, "finished")
  val PERSISTED:PVlibStatus   = Value(4, "persisted")

}

case class PVlibJob(
  /*
   * The unique identifier of the Python
   * `pvlib` job or task
   */
  jid:String,
  /*
   * The name of the Python `pvlib` method
   */
  method:PVlibMethod,
  /*
   * The timestamp, the Python `pvlib` job
   * was started
   */
  createdAt:Long,
  /*
   * The timestamp, the Python `pvlib` job
   * was updated.
   */
  updatedAt:Long,
  /*
   * The current status of the Python `pvlib`
   * job
   */
  status:PVlibStatus) {

  def toJson:JsonObject = {

    val json = new JsonObject
    json.addProperty("jid", jid)
    json.addProperty("method", method.toString)

    json.addProperty("createdAt", createdAt)
    json.addProperty("updatedAt", updatedAt)

    json.addProperty("status", status.toString)
    json

  }
}


/**
 * This object is made to supervise requests to
 * the Python pvlib for photovoltaic computations
 */
object PVlibMonitor {
  /**
   * The job registry that supports the management
   * of the Python `pvlib` tasks; it is an in-memory
   * data store we do not support long running tasks
   * beyond the horizon of the lifetime of this Beat.
   */
  private val registry = mutable.HashMap.empty[String, PVlibJob]
  /**
   * The actor based output channel to inform
   * other services about the Python tasks
   */
  private var actor:Option[ActorRef] = None
  /**
   * The SSE based output queue to inform external
   * services about the current Python tasks
   */
  private var queue: Option[SourceQueueWithComplete[String]] = None

  def setActor(actor:ActorRef):Unit = {
    this.actor = Some(actor)
  }

  def setQueue(queue: SourceQueueWithComplete[String]):Unit = {
    this.queue = Some(queue)
  }
  /**
   * Determine whether a certain job defined
   * by its unique id is registered
   */
  def isRegistered(jid:String):Boolean =
    registry.contains(jid)

  /**
   * Public method to register a certain Python
   * `pvlib` job
   */
  def started(job:PVlibJob):Unit = {
    registry += job.jid -> job
  }

  def finished(jid:String):Unit = {

    if (registry.contains(jid)) {
      /* Leverage UTC time */
      val updatedAt = Instant.now().toEpochMilli

      val job = registry(jid)
      val jobS = job.copy(updatedAt = updatedAt, status = PVlibStatuses.FINISHED)

      registry += job.jid -> jobS
    }

  }
  def get(jid:String):PVlibJob = {
    if (registry.contains(jid))
      registry(jid)

    else {
      PVlibJob(jid, NOT_DEFINED, 0L, 0L, PVlibStatuses.NOT_STARTED)
    }
  }

  def beforeTask(jid:String, method:PVlibMethod):Unit = {

    val job = buildJob(jid, method)

    started(job)
    publish(job)

  }

  def afterTask(jid:String):Unit = {

    if (registry.contains(jid)) {
      finished(jid)

      val job = registry(jid)
      publish(job)

    }

  }

  def publish(job:PVlibJob):Unit = {

    /* STEP #1: Actor */
    if (actor.nonEmpty) actor.get ! job

    /* STEP #2: SSE queue */
    if (queue.nonEmpty) queue.get.offer(job.toJson.toString)

  }

  private def buildJob(jid:String, method:PVlibMethod):PVlibJob = {

    /* Leverage UTC time */
    val createdAt = Instant.now().toEpochMilli
    /* Specify job */
    PVlibJob(
      jid       = jid,
      method    = method,
      createdAt = createdAt,
      updatedAt = 0L,
      status    = PVlibStatuses.STARTED)

  }

}
