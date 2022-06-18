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
import com.google.gson.{JsonArray, JsonElement, JsonObject}
import de.kp.works.beats.sensor.weather.WeMessages
import de.kp.works.beats.sensor.weather.pvlib.{PVlibJob, PVlibSolar}
import de.kp.works.beats.sensor.weather.sandia.SAMRegistry
import org.apache.spark.sql.SparkSession

class PowerWorker(
  queue:SourceQueueWithComplete[String],
  session:SparkSession) extends SolarWorker(queue) {

  override def receive: Receive = {
    case req:PowerReq =>
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
       * STEP #3: Execute power request
       */
      val modules = buildModules(req)
      solar.systemByDisc(handler, Some(modules))

    /*
     * This request is received from the [PVlibMonitor]
     * that supervises the associated Python task
     */
    case job:PVlibJob => publishJob(job)

  }

  private def buildInverterName(module:PVModule):String = {

    val inverter = module.inverter
      .replace(".", "")
      .replace(" ", "_")
      .replace("-", "_")

    inverter

  }

  private def buildInverterParams(module:PVModule):JsonElement = {
    SAMRegistry.getInverterParams(
      session, name = module.inverter)
  }

  private def buildModuleName(module:PVModule):String = {

    val manufacturer = module.manufacturer
      .replace(".", "")
      .replace(" ", "_")
      .replace("-", "_")

    val name = module.name
      .replace(".", "")
      .replace(" ", "_")
      .replace("-", "_")

    s"${manufacturer}__$name"

  }

  private def buildModuleParams(module:PVModule):JsonElement = {
    SAMRegistry.getModuleParams(
      session, manufacturer = module.manufacturer, name = module.name)
  }

  private def buildModules(req:PowerReq):JsonArray = {

    val modules = new JsonArray
    req.modules.foreach(pvModule => {

      val module = new JsonObject
      module.addProperty("id", pvModule.id)
      /*
       * Specify PV module
       */
      module.addProperty("surface_tilt", pvModule.elevation)
      module.addProperty("surface_azimuth", pvModule.azimuth)

      module.addProperty("module", buildModuleName(pvModule))
      module.add("module_parameters", buildModuleParams(pvModule))

      module.addProperty("modules_per_string", pvModule.modulesPerString)
      /*
       * Specify PV Inverter
       */
      module.addProperty("inverter", buildInverterName(pvModule))
      module.add("inverter_parameters", buildInverterParams(pvModule))

      module.addProperty("strings_per_inverter", pvModule.stringsPerInverter)

      module.addProperty("albedo", req.albedo)
      modules.add(module)

    })

    modules

  }
}

/**
 * The [Power] actor computes the PV power generation
 * for a certain geospatial point for the next 240h
 * or 10d, based on the latest MOSMIX station forecast.
 *
 * The computation is based on the Python `pvlib` library
 * that is invoked asynchronously. The result is returned
 * via SSE event queue
 */
class Power(queue:SourceQueueWithComplete[String], session:SparkSession) extends JsonActor {

  override def getEmpty = new JsonObject
  /**
   * The power generation computing is executed
   * asynchronously and delegated to the worker
   * actor
   */
  private val worker:ActorRef = getWorker

  private def getWorker:ActorRef =
    system
      .actorOf(RoundRobinPool(instances)
        .withResizer(resizer)
        .props(Props(new PowerWorker(queue, session))), "PowerWorker")

  override def executeJson(json: JsonElement): String = {

    val req = mapper.readValue(json.toString, classOf[PowerReq])
    worker ! req

    val response = new JsonObject
    response.addProperty("status", "success")
    response.addProperty("message", WeMessages.powerReceived())

    response.toString

  }

}
