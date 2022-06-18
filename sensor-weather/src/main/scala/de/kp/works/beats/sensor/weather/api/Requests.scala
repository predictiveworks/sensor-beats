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

/**
 * Retrieve weather forecast data from a DWD
 * MOSMIX station near to the provided geo
 * spatial point.
 */
case class ForecastReq(
  /*
   * The latitude of the geospatial point
   */
  latitude:Double,
  /*
   * The longitude of the geospatial point
   */
  longitude:Double,
  /*
   * The H3 indexing resolution that is and
   * also must be used
   */
  resolution:Int = 7,
  /*
   * An indicator to specify weather the latest
   * forecast (to be downloaded) or an existing
   * one must be used
   */
  latest:Boolean = true)

/**
 * Retrieve parameters of a certain CEC inverter,
 * specified by its name. This request supports
 * subsequent requests to compute estimated power
 * generation.
 */
case class InverterReq(
  /*
   * The name of the CEC_INVERTER
   */
  name:String)

case class InvertersReq(
  /*
   * The external table name is CEC_INVERTERS
   * and must be replaced by the internal
   * table name before the SQL query is
   * executed.
   */
  sql:String)

/**
 * Retrieve solar irradiance based on the
 * MOSMIX station forecast.
 */
case class IrradianceReq(
   /*
    * The latitude of the geospatial point
    */
   latitude:Double,
   /*
    * The longitude of the geospatial point
    */
   longitude:Double,
   /*
    * The altitude of the geospatial point
    */
   altitude:Double = 0D,
   /*
    * The H3 indexing resolution that is and
    * also must be used
    */
   resolution:Int = 7,
   /*
    * The name of the algorithm used to estimate
    * solar irradiance. Supported values are
    * 'dirint', 'disc' and 'erbs'
    */
   algorithm:String)

/**
 * Retrieve parameters of a certain CEC module,
 * specified by its name and manufacturer.
 *
 * This request supports subsequent requests to
 * compute estimated power generation.
 */
case class ModuleReq(
  /*
   * The manufacturer of the CEC_MODULE
   */
  manufacturer:String,
  /*
   * The name of the CEC_MODULE
   */
  name:String)

case class ModulesReq(
  /*
   * The external table name is CEC_MODULES
   * and must be replaced by the internal
   * table name before the SQL query is
   * executed.
   */
  sql:String
)

/**
 * Retrieve solar position based on the
 * MOSMIX station forecast.
 */
case class PositionsReq(
  /*
   * The latitude of the geospatial point
   */
  latitude:Double,
  /*
   * The longitude of the geospatial point
   */
  longitude:Double,
  /*
   * The altitude of the geospatial point
   */
  altitude:Double = 0D,
  /*
   * The H3 indexing resolution that is and
   * also must be used
   */
  resolution:Int = 7)

/**
 * Retrieve the power generation forecast
 * for the provided PV system, which can
 * be a combination of multiple modules
 * and inverters
 */
case class PVModule(
  /*
   * Unique identifier of the solar module
   */
  id:String,
  /*
   * The manufacturer of the CEC module
   */
  manufacturer:String,
  /*
   * The name of the CEC module
   */
  name:String,
  /*
   * The name of the inverter that is used as
   * defined by Sandia
   */
  inverter:String,
  /*
   * The azimuth of the solar module in degrees
   * (West=270, South=180, East=90)
   */
  azimuth:Int,
  /*
   * The inclination angle of the solar module in degrees
   * (0 degrees would be horizontal).
   */
  elevation:Int,
  /*
   * The number of panels per string in the solar module
   */
  modulesPerString:Int,
  stringsPerInverter:Int)

case class PowerReq(
  /*
   * The latitude of the PV system
   */
  latitude:Double,
  /*
   * The longitude of the PV system
   */
  longitude:Double,
  /*
   * The altitude in metre of the PV system location
   */
  altitude:Double,
  /*
   * The H3 indexing resolution that is and
   * also must be used
   */
  resolution:Int = 7,
  /*
   * The modules that form the PV system
   */
  modules:Seq[PVModule],
  /*
   * The measure of the diffuse reflection of solar
   * radiation out of the total solar radiation and
   * measured on a scale from 0 to 1
   */
  albedo:Double,
  /*
   * The temperature model (see pvlib documentation)
   * that is the closest match. Samples:
   *
   * Roof mounted systems = `open_rack`
   */
  tempModel:String)
