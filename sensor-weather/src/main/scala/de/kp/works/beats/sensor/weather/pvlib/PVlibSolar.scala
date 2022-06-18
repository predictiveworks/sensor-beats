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

import com.google.gson._
import de.kp.works.beats.sensor.weather.sandia.SAMRegistry
import PVlibMethods._
import de.kp.works.beats.sensor.weather.dwd.MxFrame
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import scala.collection.JavaConversions.asScalaSet
import scala.collection.JavaConverters.seqAsJavaListConverter
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
  override var engine:String = "solar"
  override var program: String = pythonCfg.getString("solar")
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
  private def toTimerange(sample: Array[Row]):JsonArray = {

    val values = sample
      .map(row => row.getString(0).toLong - 3600 * 1000)

    val json = new JsonArray
    values.foreach(value => json.add(value))

    json

  }

  private def toJsonArray(sample:Array[Row], pos:Int):JsonElement = {

    val schema = sample.head.schema
    val ftype = schema.fields(pos).dataType

    val values = sample
      .map(row => {
        ftype match {

          case DoubleType =>
            row.getDouble(pos)

          case StringType =>
            row.getString(pos).toDouble
        }

      })

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
      handler.onComplete(output=Seq.empty[String])

    else {
      /*
       * Extract data from MOSMIX forecasts:
       *
       * - timerange
       */
      val selcols = Seq("timestamp").map(col)
      val sample = dataset.select(selcols:_*).collect

      val timerange = toTimerange(sample)
      /*
       * Build PVLIB request
       */
      val request = new JsonObject
      request.addProperty("timezone", TIMEZONE)

      request.addProperty("latitude", latitude)
      request.addProperty("longitude", longitude)

      request.addProperty("altitude", altitude)
      request.add("timerange", timerange)

      execute(POSITIONS, request, handler)

    }
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
      handler.onComplete(output=Seq.empty[String])

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

      val timerange = toTimerange(sample)

      val ghi      = toJsonArray(sample, pos=1)
      val pressure = toJsonArray(sample, pos=2)
      val dewpoint = toJsonArray(sample, pos=3)
      /*
       * Build PVLIB request
       */
      val request = new JsonObject
      request.addProperty("timezone", TIMEZONE)

      request.addProperty("latitude", latitude)
      request.addProperty("longitude", longitude)

      request.addProperty("altitude", altitude)
      request.add("timerange", timerange)

      if (!ghi.isJsonNull)
        request.add("ghi", ghi)

      if (!pressure.isJsonNull)
        request.add("pressure", pressure)

      if (!dewpoint.isJsonNull)
        request.add("dew_point", dewpoint)

      /*
       * Minimum requirement to retrieve estimates
       * from `pvlib` is global irradiance
       */
      if (request.has("ghi"))
        execute(DNI_DIRINT, request, handler)

    }

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
      handler.onComplete(output=Seq.empty[String])

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

      val timerange = toTimerange(sample)

      val ghi = toJsonArray(sample, pos=1)
      val pressure = toJsonArray(sample, pos=2)
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

      /*
       * Minimum requirement to retrieve estimates
       * from `pvlib` is global irradiance
       */
      if (request.has("ghi"))
        execute(DNI_DISC, request, handler)

    }

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
      handler.onComplete(output=Seq.empty[String])

    else {
      /*
       * Extract data from MOSMIX forecasts:
       *
       * - timerange
       * - global horizontal irradiance (Rad1h)
       */
      val selcols = Seq("timestamp", "Rad1h").map(col)
      val sample = dataset.select(selcols:_*).collect

      val timerange = toTimerange(sample)
      val ghi = toJsonArray(sample, pos=1)
      /*
       * Build PVLIB request
       */
      val request = new JsonObject
      request.addProperty("timezone", TIMEZONE)

      request.addProperty("latitude", latitude)
      request.addProperty("longitude", longitude)

      request.addProperty("altitude", altitude)
      request.add("timerange", timerange)

      if (!ghi.isJsonNull)
        request.add("ghi", ghi)

      /*
       * Minimum requirement to retrieve estimates
       * from `pvlib` is global irradiance
       */
      if (request.has("ghi"))
        execute(DNI_ERBS, request, handler)

    }

  }

  def systemByDisc(handler:PVlibHandler, modules:Option[JsonArray]=None):Unit = {

    if (dataset.isEmpty)
      handler.onComplete(output=Seq.empty[String])

    else {
      /*
       * Extract data from MOSMIX forecasts:
       *
       * - timerange
       * - global horizontal irradiance (Rad1h)
       * - surface pressure, reduced (PPPP)
       * - dewpoint 2m above surface (Td)
       * - temperature 2m above surface (TTT)
       * - wind speed (FF)
       */

      val selcols = Seq("timestamp", "Rad1h", "PPPP", "Td", "TTT", "FF").map(col)
      val sample = dataset.select(selcols:_*).collect

      val timerange = toTimerange(sample)

      val ghi        = toJsonArray(sample, pos=1)
      val pressure   = toJsonArray(sample, pos=2)
      val dewpoint   = toJsonArray(sample, pos=3)
      val temp_air   = toJsonArray(sample, pos=4)
      val wind_speed = toJsonArray(sample, pos=5)
      /*
       * Build PVLIB request
       */
      val request = new JsonObject
      request.addProperty("timezone", TIMEZONE)

      request.addProperty("latitude", latitude)
      request.addProperty("longitude", longitude)

      request.addProperty("altitude", altitude)
      request.add("timerange", timerange)

      if (!ghi.isJsonNull)
        request.add("ghi", ghi)

      if (!pressure.isJsonNull)
        request.add("pressure", pressure)

      if (!dewpoint.isJsonNull)
        request.add("dew_point", dewpoint)

      if (!temp_air.isJsonNull)
        request.add("temp_air", temp_air)

      if (!wind_speed.isJsonNull)
        request.add("wind_speed", wind_speed)

      if (modules.isEmpty)
        request.add("modules", modulesFromCfg)

      else
        request.add("modules", modules.get)
      /*
       * Minimum requirement to retrieve estimates
       * from `pvlib` is global irradiance
       */
      //if (request.has("ghi"))
      execute(SYSTEM_DISC, request, handler)

    }

  }

  private def modulesFromCfg:JsonArray = {

     val modules = new JsonArray

    // TESTING
    val module = new JsonObject
    module.addProperty("id", "my_module")

    module.addProperty("surface_tilt", 40) // elevation
    module.addProperty("surface_azimuth", 101)

    module.addProperty("albedo", 0.14)
    module.addProperty("modules_per_string", 7)

    module.addProperty("module", "LG_Electronics_Inc__LG355N1C_V5")

    val module_parameters = SAMRegistry
      .getModuleParams(session, manufacturer = "LG Electronics Inc", name = "LG355N1C-V5")
    module.add("module_parameters", module_parameters)

    modules.add(module)

    module.addProperty("inverter", "LG_Electronics_Inc_LG350M1K_L5_[240V]")

    // LG Electronics Inc : LG350M1K-L5 [240V]
    // LG350M1K-L5 [240V]
    val inverter_parameters = SAMRegistry
      .getInverterParams(session, "LG350M1K-L5 [240V]")
    module.add("inverter_parameters", inverter_parameters)

    /*
            """
            Describes the module's construction. Valid strings are
            'glass_polymer' and 'glass_glass'.

            Used for cell and module temperature calculations.
            """
            module_type = spec.get("module_type", None)
            temperature_model_parameters = spec.get("temperature_model_parameters", None)

            strings_per_inverter = spec.get("strings_per_inverter", 1)

            racking_model = spec.get("racking_model", "open_rack")
            losses_parameters = spec.get("losses_parameters", None)

     */
    modules

  }

  def systemToDF(input:String):DataFrame = {

    val json = JsonParser.parseString(input).getAsJsonObject
    /*
     * The result specifies a JSON object, where
     * each field specifies a timeseries
     */
    val fnames = json.keySet()
    val result = fnames.flatMap(fname => {

      /* The timeseries is formatted as JSON object */
      val series = json.get(fname).getAsJsonObject
      series.keySet().map(ts => {

        val value = series.get(ts)

        if (value.isJsonNull)
          (ts.toLong, fname, Double.NaN)

        else
          (ts.toLong, fname, value.getAsDouble)

      })

    }).toArray

    var fields = fnames.to.distinct.sorted
      .map(fieldName => StructField(fieldName, DoubleType, nullable = true)).toArray
    /*
     * Append `timestamp` and specify the respective schema
     */
    fields = Array(StructField("timestamp", LongType, nullable = false)) ++ fields
    val schema = StructType(fields)

    val rows = result
      .groupBy{case(ts, _, _) => ts}
      /*
       * Transform the respective row into an ordered
       * row
       */
      .map{case(ts, columns) =>
        val values = Seq(ts) ++ columns.sortBy{case(_, name, _) => name}.map{case(_, _, value) => value}
        Row.fromSeq(values)
      }
      .toList

    session.createDataFrame(rows.asJava, schema)

  }

  def weatherByDisc(handler:PVlibHandler):Unit = {

    if (dataset.isEmpty)
      handler.onComplete(output=Seq.empty[String])

    else {
      /*
       * Extract data from MOSMIX forecasts:
       *
       * - timerange
       * - global horizontal irradiance (Rad1h)
       * - surface pressure, reduced (PPPP)
       * - dewpoint 2m above surface (Td)
       * - temperature 2m above surface (TTT)
       * - wind speed (FF)
       */

      val selcols = Seq("timestamp", "Rad1h", "PPPP", "Td", "TTT", "FF").map(col)
      val sample = dataset.select(selcols:_*).collect

      val timerange = toTimerange(sample)

      val ghi        = toJsonArray(sample, pos=1)
      val pressure   = toJsonArray(sample, pos=2)
      val dewpoint   = toJsonArray(sample, pos=3)
      val temp_air   = toJsonArray(sample, pos=4)
      val wind_speed = toJsonArray(sample, pos=5)
      /*
       * Build PVLIB request
       */
      val request = new JsonObject
      request.addProperty("timezone", TIMEZONE)

      request.addProperty("latitude", latitude)
      request.addProperty("longitude", longitude)

      request.addProperty("altitude", altitude)
      request.add("timerange", timerange)

      if (!ghi.isJsonNull)
        request.add("ghi", ghi)

      if (!pressure.isJsonNull)
        request.add("pressure", pressure)

      if (!dewpoint.isJsonNull)
        request.add("dew_point", dewpoint)

      if (!temp_air.isJsonNull)
        request.add("temp_air", temp_air)

      if (!wind_speed.isJsonNull)
        request.add("wind_speed", wind_speed)

      /*
       * Minimum requirement to retrieve estimates
       * from `pvlib` is global irradiance
       */
      //if (request.has("ghi"))
      execute(WEATHER_DISC, request, handler)

    }

  }

}
