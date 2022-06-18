package de.kp.works.beats.sensor.weather.api

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
import akka.stream.scaladsl.SourceQueueWithComplete
import com.google.gson.{JsonElement, JsonObject}
import de.kp.works.beats.sensor.weather.WeMessages
import de.kp.works.beats.sensor.weather.pvlib.{PVlibJob, PVlibSolar}
import org.apache.spark.sql.SparkSession

class IrradianceWorker(
  queue:SourceQueueWithComplete[String],
  session:SparkSession) extends SolarWorker(queue) {

  override def receive: Receive = {
    case req:IrradianceReq =>
      /*
       * STEP #1: Initiate `pvlib` solar wrapper
       */
      val solar = new PVlibSolar(
        session    = session,
        latitude   = req.latitude,
        longitude  = req.longitude,
        altitude   = req.altitude,
        resolution = req.resolution)
      /*
       * STEP #2: Initiate the result handler
       */
      val handler = buildHandler()
      handler.setActor(self)
      /*
       * STEP #3: Set worker to receive status events
       * and execute irradiance request
       */
      req.algorithm match {
        case "dirint" =>
          solar.dniByDirint(handler)

        case "disc" =>
          solar.dniByDisc(handler)

        case "erbs" =>
          solar.dniByErbs(handler)
        /*
         * This request returns the MOSMIX weather
         * forecast enriched with DNI & DHI
         */
        case "disc_erbs" =>
          solar.weatherByDisc(handler)

        case other =>

          val data = new JsonObject
          data.addProperty("message", s"Unknown algorithm `$other` detected.")

          val event = new JsonObject

          event.addProperty("id", "")
          event.addProperty("type",  s"urn:works:pvlib:error")

          event.add("data", data)
          queue.offer(event.toString)
      }

    /*
     * This request is received from the [PVlibMonitor]
     * that supervises the associated Python task
     */
    case job:PVlibJob => publishJob(job)

  }

}

/**
 * The [Irradiance] actor computes the solar irradiance
 * for a certain geospatial point for the next 240h
 * or 10d, based on the latest MOSMIX station forecast.
 *
 * The computation is based on the Python `pvlib` library
 * that is invoked asynchronously. The result is returned
 * via SSE event queue
 */
class Irradiance(queue:SourceQueueWithComplete[String], session:SparkSession) extends JsonActor {

  override def getEmpty = new JsonObject
  /**
   * The solar irradiance computing is executed
   * asynchronously and delegated to the worker
   * actor
   */
  private val worker:ActorRef = getWorker

  private def getWorker:ActorRef =
    system
      .actorOf(RoundRobinPool(instances)
        .withResizer(resizer)
        .props(Props(new IrradianceWorker(queue, session))), "IrradianceWorker")

  override def executeJson(json: JsonElement): String = {

    val req = mapper.readValue(json.toString, classOf[IrradianceReq])
    worker ! req

    val response = new JsonObject
    response.addProperty("status", "success")
    response.addProperty("message", WeMessages.irradianceReceived())

    response.toString

  }

}
