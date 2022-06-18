package de.kp.works.beats.sensor.weather

/**
 * Copyright (c) 2020 - 2022 Dr. Krusche & Partner PartG. All rights reserved.
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

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.kp.works.beats.sensor.BaseRoute

import scala.concurrent.ExecutionContextExecutor

object WeRoute {

  val WE_FORECAST_ACTOR    = "weather_anomaly_actor"
  val WE_INVERTER_ACTOR    = "weather_inverter_actor"
  val WE_INVERTERS_ACTOR   = "weather_inverters_actor"
  val WE_IRRADIANCE_ACTOR  = "weather_irradiance_actor"
  val WE_MODULE_ACTOR      = "weather_module_actor"
  val WE_MODULES_ACTOR     = "weather_modules_actor"
  val WE_POSITIONS_ACTOR   = "weather_positions_actor"
  val WE_POWER_ACTOR       = "weather_power_actor"

}

class WeRoute(actors:Map[String, ActorRef])(implicit system: ActorSystem) extends BaseRoute {

  implicit lazy val context: ExecutionContextExecutor = system.dispatcher
  import WeRoute._

  def getRoutes: Route = {
    getForecast ~
    getInverter ~
    getInverters ~
    getIrradiance ~
    getModule ~
    getModules ~
    getPositions ~
    getPower
  }

  /**
   * `SensorBeat` API route to retrieve MOSMIX
   * station forecasts for the next 10 days
   */
  private def getForecast:Route = routePost("weather/v1/forecast", actors(WE_FORECAST_ACTOR))
  /**
   * `SensorBeat` API route to retrieve a certain
   * CEC inverter by name
   */
  private def getInverter:Route = routePost("weather/v1/inverter", actors(WE_INVERTER_ACTOR))
  /**
   * `SensorBeat` API route to all registered
   * CEC inverters
   */
  private def getInverters:Route = routePost("weather/v1/inverters", actors(WE_INVERTERS_ACTOR))
  /**
   * `SensorBeat` API route to retrieve MOSMIX
   * station forecasts for the next 10 days and
   * computed solar irradiance
   */
  private def getIrradiance:Route = routePost("weather/v1/irradiance", actors(WE_IRRADIANCE_ACTOR))
  /**
   * `SensorBeat` API route to retrieve a certain
   * CEC module by name and manufacturer
   */
  private def getModule:Route = routePost("weather/v1/module", actors(WE_MODULE_ACTOR))
  /**
   * `SensorBeat` API route to all registered
   * CEC modules
   */
  private def getModules:Route = routePost("weather/v1/modules", actors(WE_MODULES_ACTOR))
  /**
   * `SensorBeat` API route to get predicted
   * solar positions from the MOSMIX station
   * forecasts
   */
  private def getPositions:Route = routePost("weather/v1/positions", actors(WE_POSITIONS_ACTOR))
  /**
   * `SensorBeat` API route to get predicted
   * PV power generation from the MOSMIX station
   * forecasts
   */
  private def getPower:Route = routePost("weather/v1/power", actors(WE_POWER_ACTOR))

}
