package de.kp.works.sensor.weather.dwd

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

import de.kp.works.sensor.weather.WeLogging
import de.kp.works.sensor.weather.dwd.MxParams._
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.io.File

object MxFrameUtil extends Serializable {

  def kelvin2Celsius: UserDefinedFunction = udf{ (value:Double) => {
    value - 273.15
  }}

  def joule2Watt: UserDefinedFunction = udf{ (value:Double) => {
    value * 0.277778
  }}
}

class MxFrame(session:SparkSession) extends WeLogging {

  private val mosmixCfg = config.getMosmixCfg
  /**
   * Public method to retrieve the MOSMIX station
   * forecast data for a pre-defined set of params
   * that are relevant for PV output prediction.
   *
   * This method also transforms temperature from
   * Kelvin to Celsius, and global irradiance to
   * Watt hour per square meter.
   */
  def forecastPV(lat:Double, lon:Double, resolution:Int = 7):DataFrame = {

    val mxParams = Seq(
      /* Wind direction: 0°..360° */
      DD,
      /* Wind speed: m/s */
      FF,
      /* Significant Weather: - */
      ww,
      /* Global Irradiance: kJ/m2 */
      Rad1h,
      /* Global irradiance within the last hour: % (0..80) */
      RRad1,
      /* Temperature 2m above surface: K */
      TTT,
      /* Surface pressure, reduced: Pa */
      PPPP,
      /* Dewpoint 2m above surface: K */
      Td,
      /* Total cloud cover: % (0..100) */
      N)

    val columns = Seq("timestamp") ++ mxParams.map(_.toString)
    val dataset = forecast(lat, lon, resolution, columns)

    if (dataset.isEmpty) return dataset
    dataset
      /*
       * Convert temperature values from
       * Kelvin to Celsius
       */
      .withColumn(TTT.toString, MxFrameUtil.kelvin2Celsius(col(TTT.toString)))
      .withColumn(Td.toString, MxFrameUtil.kelvin2Celsius(col(Td.toString)))
      /*
       * Global radiation is in kJ/m^2, transform into Wh/m^2
       */
      .withColumn(Rad1h.toString, MxFrameUtil.joule2Watt(col(Rad1h.toString)))

  }

  def forecast(lat:Double, lon:Double, resolution:Int = 7, columns:Seq[String]):DataFrame = {

    val dataset = load(lat, lon, resolution)
    if (dataset.isEmpty) return dataset

    val selcols = columns.map(col)
    dataset.select(selcols: _*)

  }
  /**
   * This method retrieves the latest downloaded MOSMIX
   * station forecast (10 days) file and transforms it
   * into an Apache Spark [DataFrame].
   *
   * This `load` method is usually the initial method
   * for DataFrame-based weather computation.
   */
  def load(lat:Double, lon:Double, resolution:Int = 7):DataFrame = {
    /*
     * STEP #1: Retrieve available weather station
     * from the provided geospatial location
     */
    val station = MxRegistry.getByLatLon(lat, lon, resolution)
    if (station.isEmpty) {

      val message = s"No MOSMIX station found for lat=$lat and lon=$lon"
      warn(message)

      return session.emptyDataFrame

    }
    /*
     * STEP #2: Determine the latest *.csv file that matches
     * the identified `station`.
     */
    val folder = mosmixCfg.getString("downloads")
    val file = new File(folder)

    if (!file.isDirectory) {

      val message = s"The download folder `$folder` does not exist"
      error(message)

      return session.emptyDataFrame

    }

    val postfix = s"_${station.get.id}.csv"
    val files = file.listFiles()
      .filter(f => f.isFile && f.getName.endsWith(postfix))

    if (files.isEmpty) {

      val message = s"No files found for `${station.get.id}`"
      error(message)

      return session.emptyDataFrame

    }

    val latest = files
      .map(f => {
        val tokens = f.getName
          .replace(postfix, "")
          .split("_")

        val timestamp = tokens.last.toLong
        (timestamp, f)
      })
      .maxBy { case (t, _) => t }._2

    /*
     * STEP #3: Load latest station forecasts (10 days)
     * as Apache spark compliant dataframe
     */
    val dataframe =
      session.read.option("header", value = true).csv(latest.getAbsolutePath)

    dataframe

  }

}
