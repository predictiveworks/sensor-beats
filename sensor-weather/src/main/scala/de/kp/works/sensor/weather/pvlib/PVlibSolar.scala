package de.kp.works.sensor.weather.pvlib

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

import com.google.gson.{JsonArray, JsonElement, JsonNull, JsonObject}
import de.kp.works.sensor.weather.dwd.MxFrame
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
/**
 * [PVlibSolar] is made to bridge between Scala
 * and Python and expose the `pvlib` library to
 * this Scala environment.
 *
 * Data exchange format is JSON and added as call
 * configuration parameters to the Python command.
 *
 * The data provided to compute solar specific
 * parameters is a combination of user data and
 * MOSMIX weather station forecasts
 */
class PVlibSolar(
  session:SparkSession,
  latitude:Double,
  longitude:Double,
  altitude:Double = 0D,
  resolution:Int = 7) extends PVlibWorker {
  /**
   * The MOSMIX station forecast for the provided
   * geospatial coordinates
   */
  private val mxFrame = new MxFrame(session)
  private val dataset = load
  /**
   * The timezone used as a basis for computation
   */
  private val TIMEZONE = "UTC"
  /**
   * Python program indicator
   */
  private val program = pythonCfg.getString("solar")
  /**
   * Load the MOSMIX station forecast data for the
   * provided geospatial coordinate
   */
  private def load:DataFrame =
    mxFrame.forecastPV(latitude, longitude, resolution)

  /**
   * Extract timerange
   *
   * Shift values by 1h to the past, since the DWD Values
   * are assigned to the end of the period, whereas the
   * PVLIB values are assigned to the beginning of a cycle.
   */
  private def toTimerange(sample:Array[Row], pos:Int):JsonArray = {

    val values = sample
      .map(row => row.getString(pos).toLong - 3600 * 1000)

    val json = new JsonArray
    values.foreach(value => json.add(value))

    json

  }
  /**
   * Extract the Global Horizontal Irradiance (GHI)
   */
  private def toGHI(sample:Array[Row], pos:Int):JsonArray = {

    val values = sample
      .map(row => row.getDouble(pos))

    val filtered = values.filter(v => !v.isNaN)

    val json = new JsonArray

    if (values.length == filtered.length)
      values.foreach(value => json.add(value))

    json

  }
  /**
   * Extract the Surface pressure, reduced: Pa
   */
  private def toPressure(sample:Array[Row], pos:Int):JsonElement = {

    val values = sample
      .map(row => row.getString(pos).toDouble)

    val filtered = values.filter(v => !v.isNaN)

    if (values.length == filtered.length) {
      val json = new JsonArray
      values.foreach(value => json.add(value))

      json

    } else JsonNull.INSTANCE

  }
  /**
   * Extract the Surface pressure, reduced: Pa
   */
  private def toDewpoint(sample:Array[Row], pos:Int):JsonElement = {

    val values = sample
      .map(row => row.getString(pos).toDouble)

    val filtered = values.filter(v => !v.isNaN)

    if (values.length == filtered.length) {
      val json = new JsonArray
      values.foreach(value => json.add(value))

      json

    } else JsonNull.INSTANCE

  }

  /**
   * Public method to compute the solar positions for a certain
   * geospatial position and the timerange extracted from the
   * MOSMIX forecasts
   */
  def positions(handler:PVlibHandler):Unit = {

    if (dataset.isEmpty)
      handler.complete(output=Seq.empty[String])

    else {
      /*
       * Extract data from MOSMIX forecasts:
       *
       * - timerange
       */
      val selcols = Seq("timestamp").map(col)
      val sample = dataset.select(selcols:_*).collect

      val timerange = toTimerange(sample, pos=0)
      /*
       * Build PVLIB request
       */
      val request = new JsonObject
      request.addProperty("timezone", TIMEZONE)

      request.addProperty("latitude", latitude)
      request.addProperty("longitude", longitude)

      request.addProperty("altitude", altitude)
      request.add("timerange", timerange)

      positions(request, handler)

    }
  }

  private def positions(params:JsonObject, handler:PVlibHandler):Unit = {
    /*
     * Enrich provided parameters with control
     * information
     */
    params.addProperty("engine", "solar")
    params.addProperty("method", "positions")
    /*
     * Build the python command to retrieve solar
     * positions from the provided parameters
     */
    val command = s"$program -c ${params.toString}"
    run(command, handler)

  }
  /**
   * Public method to determine DNI from GHI using the DIRINT modification
   * of the DISC model. Implements the modified DISC model known as "DIRINT".
   *
   * DIRINT predicts direct normal irradiance (DNI) from measured global
   * horizontal irradiance (GHI).
   *
   * DIRINT improves upon the DISC model by using time-series GHI data
   * and dew point temperature information. The effectiveness of the DIRINT
   * model improves with each piece of information provided.
   *
   * The pvlib implementation limits the clearness index to 1.
   *
   * The computation requires the timerange, ghi, pressure and dewpoint
   * from the MOSMIX forecasts

   */
  def dniByDirint(handler:PVlibHandler):Unit = {

    if (dataset.isEmpty)
      handler.complete(output=Seq.empty[String])

    else {
      /*
       * Extract data from MOSMIX forecasts:
       *
       * - timerange
       * - global horizontal irradiance (Rad1h)
       * - surface pressure, reduced (PPPP)
       * - dewpoint 2m above surface (Td)
       */
      val selcols = Seq("timestamp", "Rad1h", "PPPP", "Td").map(col)
      val sample = dataset.select(selcols:_*).collect

      val timerange = toTimerange(sample, pos=0)
      val ghi = toGHI(sample, pos=1)

      val pressure = toPressure(sample, pos=2)
      val dewpoint = toDewpoint(sample, pos=3)
      /*
       * Build PVLIB request
       */
      val request = new JsonObject
      request.addProperty("timezone", TIMEZONE)

      request.addProperty("latitude", latitude)
      request.addProperty("longitude", longitude)

      request.addProperty("altitude", altitude)
      request.add("timerange", timerange)

      request.add("ghi", ghi)

      if (!pressure.isJsonNull)
        request.add("pressure", pressure)

      if (!dewpoint.isJsonNull)
        request.add("dew_point", dewpoint)

      dniByDirint(request, handler)

    }

  }

  private def dniByDirint(params:JsonObject, handler:PVlibHandler):Unit = {
    /*
     * Enrich provided parameters with control
     * information
     */
    params.addProperty("engine", "solar")
    params.addProperty("method", "dni_dirint")
    /*
     * Build the python command to retrieve solar
     * positions from the provided parameters
     */
    val command = s"$program -c ${params.toString}"
    run(command, handler)

  }
  /**
   * Public method to estimate Direct Normal Irradiance (DNI) from
   * the Global Horizontal Irradiance (GHI) using the DISC model.
   *
   * The DISC algorithm converts global horizontal irradiance to
   * direct normal irradiance through empirical relationships
   * between the global and direct clearness indices.
   *
   * The pvlib implementation limits the clearness index to 1.
   *
   * The computation requires the timerange, ghi and pressure
   * from the MOSMIX forecasts
   */
  def dniByDisc(handler:PVlibHandler):Unit = {

    if (dataset.isEmpty)
      handler.complete(output=Seq.empty[String])

    else {
      /*
       * Extract data from MOSMIX forecasts:
       *
       * - timerange
       * - global horizontal irradiance (Rad1h)
       * - surface pressure, reduced (PPPP)
       */
      val selcols = Seq("timestamp", "Rad1h", "PPPP").map(col)
      val sample = dataset.select(selcols:_*).collect

      val timerange = toTimerange(sample, pos=0)

      val ghi = toGHI(sample, pos=1)
      println(ghi)
      val pressure = toPressure(sample, pos=2)
      /*
       * Build PVLIB request
       */
      val request = new JsonObject
      request.addProperty("timezone", TIMEZONE)

      request.addProperty("latitude", latitude)
      request.addProperty("longitude", longitude)

      request.addProperty("altitude", altitude)
      request.add("timerange", timerange)

      request.add("ghi", ghi)
      if (!pressure.isJsonNull)
        request.add("pressure", pressure)

      dniByDisc(request, handler)

    }

  }

  private def dniByDisc(params:JsonObject, handler:PVlibHandler):Unit = {
    /*
     * Enrich provided parameters with control
     * information
     */
    params.addProperty("engine", "solar")
    params.addProperty("method", "dni_disc")
    /*
     * Build the python command to retrieve solar
     * positions from the provided parameters
     */
    val command = s"$program -c ${params.toString}"
    run(command, handler)

  }

  /**
   * Public method to estimate DNI and DHI from Global Horizontal
   * Irradiance using the Erbs model. The Erbs model estimates the
   * diffuse fraction DF from global horizontal irradiance through
   * an empirical relationship between DF and the ratio of GHI to
   * extraterrestrial irradiance, Kt.
   *
   * The computation requires the timerange and ghi from the MOSMIX
   * forecasts.
   */
  def dniByErbs(handler:PVlibHandler):Unit = {

    if (dataset.isEmpty)
      handler.complete(output=Seq.empty[String])

    else {
      /*
       * Extract data from MOSMIX forecasts:
       *
       * - timerange
       * - global horizontal irradiance (Rad1h)
       */
      val selcols = Seq("timestamp", "Rad1h").map(col)
      val sample = dataset.select(selcols:_*).collect

      val timerange = toTimerange(sample, pos=0)
      val ghi = toGHI(sample, pos=1)
      /*
       * Build PVLIB request
       */
      val request = new JsonObject
      request.addProperty("timezone", TIMEZONE)

      request.addProperty("latitude", latitude)
      request.addProperty("longitude", longitude)

      request.addProperty("altitude", altitude)
      request.add("timerange", timerange)

      request.add("ghi", ghi)

      dniByErbs(request, handler)

    }

  }

  private def dniByErbs(params:JsonObject, handler:PVlibHandler):Unit = {
    /*
     * Enrich provided parameters with control
     * information
     */
    params.addProperty("engine", "solar")
    params.addProperty("method", "dni_erbs")
    /*
     * Build the python command to retrieve solar
     * positions from the provided parameters
     */
    val command = s"$program -c ${params.toString}"
    run(command, handler)

  }

}
