package de.kp.works.beats.sensor.weather.dwd

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

import de.kp.works.beats.sensor.http.HttpConnect
import de.kp.works.beats.sensor.weather.WeLogging
import de.kp.works.beats.sensor.weather.h3.H3
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, LongType, StructField, StructType}

import java.io.{File, FileInputStream, PrintWriter}
import java.nio.file.{Files, Paths, StandardCopyOption}
import java.time.Instant
import java.util.TimeZone
import java.util.zip.ZipInputStream
import scala.xml.XML

case class MxStation(
  /*
   * The unique identifier
   */
  id:String,
  /*
   * The ICAO code
   */
  icao:String,
  /*
   * The name
   */
  name:String,
  /*
   * The latitude
   */
  lat:Double,
  /*
   * The longitude
   */
  lon:Double,
  /*
   * The H3 index of the geospatial
   * coordinate
   */
  h3index:Long,
  /*
   * The H3 resolution used to compute
   * the respective index
   */
  h3res:Int = 7,
  /*
   * The altitude
   */
  altitude:Double,
  /*
   * The place
   */
  place:String)

/*
 * A unique data point
 */
case class MxDot(ts:Long, name:String, value:Double)
/**
 * [MxStations] supports basic data operations for DWD MOSMIX stations.
 *
 * Note, it is not guaranteed that these stations are operational now.
 *
 * MOSMIX forecasts represent statistically optimized point forecasts.
 * They are available for about 5400 locations around the world and are
 * based on statistical optimizations of the numerical weather prediction
 * model forecasts:
 *
 * All numerical weather prediction (NWP) models have shortcomings.
 * NWP models simplify the surface conditions, because they represent
 * the earth surface as a net of grid points.
 *
 * Small scale effects and heterogeneities cannot be calculated at
 * arbitrary accuracy. Moreover, it is not always possible to explicitly
 * calculate all desired variables and to represent all geographical points.
 *
 * Furthermore the forecasts of the NWP models have systematical biases.
 *
 * Model Output Statistics (MOS) is a statistical post-processing technique,
 * which determines the systematical dependencies between local prediction
 * variables and local observations as well as larger scale model variables
 * for specific meteorological situations.
 *
 * Due to their high forecast accuracy, MOS techniques turned out to be a valuable
 * practical interpretation aid for forecast meteorologists at DWD. In particular,
 * the MOS product MOSMIX is often used as an operational forecast guidance.
 *
 * All MOS products at DWD are regularly updated by enlargement of the observational
 * model forecast data sets and by addition of new stations.
 *
 * MOSMIX
 *
 * DWD’s fully automatic MOSMIX product optimizes and interprets the forecast calculations
 * of the NWP models ICON (DWD) and IFS (ECMWF), combines these and calculates statistically
 * optimized weather forecasts in terms of point forecasts (PFCs).
 *
 * Thus, statistically corrected, updated forecasts for the next ten days are calculated for
 * about 5400 locations around the world. Most forecasting locations are spread over Germany
 * and Europe.
 *
 * MOSMIX forecasts (PFCs) include nearly all common meteorological parameters measured by
 * weather stations. Also weather parameters like visibility which do not represent standard
 * prediction variables in NWP models are calculated in MOSMIX by application of statistical
 * parametrizations and interpretations.
 *
 * Moreover, MOSMIX provides probabilistic forecasts, i.e. probability statements for the
 * occurrence of e.g. strong wind gusts or high precipitation events.
 */
object MxStations extends HttpConnect with WeLogging {

  private val H3_RESOLUTION = 7

  private val downloadCfg = config.getDownloadCfg
  private val identRE = "href=\"(.*?)\"".r
  /**
   * The endpoint refers to the latest forecast
   * values of a certain MOSMIX station
   */
  private val latestUrl = "https://opendata.dwd.de/weather/local_forecasts/mos/MOSMIX_L/single_stations/{stationid}/kml/MOSMIX_L_LATEST_{stationid}.kmz"
  /**
   * The endpoint refers to those stations that
   * have weather (forecast) data assigned
   */
  private val stationUrl = "https://opendata.dwd.de/weather/local_forecasts/mos/MOSMIX_L/single_stations/"
  /**
   * The file refers to the MOSMIX stations catalog
   * from 2018
   */
  private val stationFile = "/mosmix-stations.txt"

  private val timezone = TimeZone.getTimeZone("UTC")

  def main(args:Array[String]):Unit = {

    val kmlFile = "/Users/krusche/IdeaProjects/sensor-beats/sensor-weather/downloads/mosmix_1653308890186_01001.kml"
    latestToCSV(kmlFile, "01001")

    System.exit(0)
  }

  private def toEpochMillis(datetime:String):Long = {
    Instant.parse(datetime).toEpochMilli
  }

  def extractLatest(kmlFile:String):(Long, Seq[MxDot]) = {

    val xmlFile = XML.loadFile(kmlFile)
    /*
     * XML query is performed without providing
     * the respective namespace
     */
    val definitions = xmlFile \\ "ExtendedData" \\ "ProductDefinition"
    val issuedTime = toEpochMillis((definitions \\ "IssueTime").text)

    val timeSteps = definitions \\ "ForecastTimeSteps" \\ "TimeStep"
    /*
     * NOTE:
     *
     * The DWD time steps are assigned to the end of the period,
     * whereas some libraries like PVLIB are assigned to the
     * beginning of a cycle.
     */
    val timestamps = timeSteps.map(timeStep => toEpochMillis(timeStep.text))

    val forecasts = (xmlFile \\ "Placemark" \\ "ExtendedData" \\ "Forecast")
      .flatMap(forecast => {
        /*
         * Extract attribute name
         */
        val name = forecast.attributes.asAttrMap("dwd:elementName")
        /*
         * Extract attribute values
         */
        val values = (forecast \\ "value")
          .text.trim
          .split("\\s+")
          .map(value => {
            if (value.trim == "-") Double.NaN else value.trim.toDouble
          })

        timestamps.zip(values).map{case(k,v) => MxDot(k,name, v)}

      })

    (issuedTime, forecasts)

  }
  /**
   * A helper method to extract a MOSMIX station's latest
   * forecasts into a pivoted dataset that can be converted
   * into a Spark Dataframe with ease.
   */
  def extractLatestAsRows(kmlFile:String):(Long, Seq[Row], StructType) = {

    val xmlFile = XML.loadFile(kmlFile)
    /*
     * XML query is performed without providing
     * the respective namespace
     */
    val definitions = xmlFile \\ "ExtendedData" \\ "ProductDefinition"
    val issuedTime = toEpochMillis((definitions \\ "IssueTime").text)

    val timeSteps = definitions \\ "ForecastTimeSteps" \\ "TimeStep"
    val timestamps = timeSteps.map(timeStep => toEpochMillis(timeStep.text))

    val forecasts = (xmlFile \\ "Placemark" \\ "ExtendedData" \\ "Forecast")
      .flatMap(forecast => {
        /*
         * Extract attribute name
         */
        val name = forecast.attributes.asAttrMap("dwd:elementName")
        /*
         * Extract attribute values
         */
        val values = (forecast \\ "value")
          .text.trim
          .split("\\s+")
          .map(value => {
            if (value.trim == "-") Double.NaN else value.trim.toDouble
          })

        timestamps.zip(values).map{case(k,v) => MxDot(k,name, v)}

      })
    /*
     * Extract schema definition from forecasts; the current
     * implementation restricts fields to [Double].
     *
     * The field names are sorted in ascending alphabetical
     * order to be consistent with the row value extraction
     */
    var fields = forecasts.map(_.name).distinct.sorted
      .map(fieldName => StructField(fieldName, DoubleType, nullable = true)).toArray
    /*
     * Append `timestamp` and specify the respective schema
     */
    fields = Array(StructField("timestamp", LongType, nullable = false)) ++ fields
    val schema = StructType(fields)
    /*
     * Transform column-oriented forecasts into rows
     */
    val rows = forecasts
      .groupBy(_.ts)
      /*
       * Transform the respective row into an ordered
       * row
       */
      .map{case(ts, columns) =>
        val values = Seq(ts) ++ columns.sortBy(_.name).map(_.value)
        Row.fromSeq(values)
      }
      .toSeq

    (issuedTime, rows, schema)

  }
  /**
   * This is a helper method to transform a MOSMIX
   * *.kml file into a *.csv file and persists the
   * result.
   */
  def latestToCSV(kmlFile:String, stationId:String):Unit = {

    val xmlFile = XML.loadFile(kmlFile)
    /*
     * XML query is performed without providing
     * the respective namespace
     */
    val definitions = xmlFile \\ "ExtendedData" \\ "ProductDefinition"
    val issuedTime = toEpochMillis((definitions \\ "IssueTime").text)

    val timeSteps = definitions \\ "ForecastTimeSteps" \\ "TimeStep"
    val timestamps = timeSteps.map(timeStep => toEpochMillis(timeStep.text))

    val forecasts = (xmlFile \\ "Placemark" \\ "ExtendedData" \\ "Forecast")
      .flatMap(forecast => {
        /*
         * Extract attribute name
         */
        val name = forecast.attributes.asAttrMap("dwd:elementName")
        /*
         * Extract attribute values
         */
        val values = (forecast \\ "value")
          .text.trim
          .split("\\s+")
          .map(value => {
            if (value.trim == "-") Double.NaN else value.trim.toDouble
          })

        timestamps.zip(values).map{case(k,v) => MxDot(k,name, v)}
      })

    val folder = downloadCfg.getString("folder")
    val csvFile = s"$folder/mosmix_${issuedTime}_$stationId.csv"

    val writer = new PrintWriter(new File(csvFile))
    /*
     * The field names are sorted in ascending alphabetical
     * order to be consistent with the row value extraction
     */
    val fields = Seq("timestamp") ++ forecasts.map(_.name).distinct.sorted
    val header = fields.mkString(",") + "\n"

    writer.write(header)
    /*
     * Transform column-oriented forecasts into ordered
     * rows compliant with the header definition
     */
    forecasts
      .groupBy(_.ts)
      .foreach{case(ts, columns) =>
        val values = Seq(ts) ++ columns.sortBy(_.name).map(_.value)
        val row = values.mkString(",") + "\n"

        writer.write(row)
      }

    writer.close()

  }
  /**
   * This public method retrieves the latest MOSMIX forecasts
   * for a certain MOSMIX station and transforms the downloaded
   * *.kmz file into a *.csv file for later data analysis
   */
  def downloadLatest(stationId:String):Unit = {
    /*
     * STEP #1: Build the file name
     */
    val folder = downloadCfg.getString("folder")
    val timestamp = System.currentTimeMillis()

    val kmzFile = s"$folder/mosmix_${timestamp}_$stationId.kmz"
    val kmlFile = s"$folder/mosmix_${timestamp}_$stationId.kml"
    /*
     * STEP #2: Download the associated *.kmz file
     */
    val endpoint = latestUrl.replace("{stationid}", stationId)
    val result = download(endpoint, kmzFile)

    if (result.status.isSuccess) {
      /*
       * STEP #3: In case of successful download of
       * the *.kmz file, unzip file
       */
      val zis = new ZipInputStream(
        new FileInputStream(new File(kmzFile)))

      var zipEntry = zis.getNextEntry
      while (zipEntry != null) {
        Files.copy(zis, Paths.get(kmlFile), StandardCopyOption.REPLACE_EXISTING)
        zipEntry = zis.getNextEntry
      }

      zis.closeEntry()
      /*
       * STEP #4: Transform latest forecasts for provided
       * station into *.csv file and write to file system
       */
      latestToCSV(kmlFile, stationId)

      //Files.delete(Paths.get(kmlFile))
      Files.delete(Paths.get(kmzFile))

    } else {
      throw new Exception(result.getError)
    }

  }
  /**
   * Helper method to retrieve the dataset names
   * of the operational MOSMIX stations; the name
   * directly refers to the station ident.
   */
  def getStationIds:Seq[String] = {

    val bytes = get(stationUrl)
    val lines = extractTextBody(bytes)
      /*
       * Extract lines
       */
      .split("\\n")
      /*
       * Restrict to lines that start with <a href="
       */
      .filter(line => line.startsWith("<a href=\""))
      .map(line => {
        var ident:String = null
        identRE.findAllIn(line).matchData.foreach(m => {
          ident = m.group(1).trim
        })

        ident = ident.replace("/", "")
        ident

      })

    lines

  }
  /**
   * Helper method to extract the MOSMIX stations
   * from the DWD stations catalog file.
   */
  def getStations:Seq[MxStation] = {

    /*
     * STEP #1: Retrieve MOSMIX station identifiers
     * that have weather forecasts assigned
     */
    val ids = getStationIds
    /*
     * STEP #2: Transform MOSMIX station descriptions
     * into [MsStation]s and validate whether the `id`
     * has forecast data assigned
     */
    val is = getClass.getResourceAsStream(stationFile)
    val lines = scala.io.Source.fromInputStream(is, "iso8859-1").getLines()

    val stations = lines
      /*
       * The MOSMIX stations catalog contains additional
       * information that is not relevant for extraction
       *
       * Lines which specify stations start with 9
       */
      .filter(line => line.startsWith("9"))
      /*
       * The relevant information starts at CHAR position
       * `12`, e.g.
       */
      .map(line => line.substring(12))
      /*
       * Extract the respective station data and turn
       * into a comma-separated list
       */
      .map(line => {
        /*
         * [0:4] the identifier of the MOSMIX station
         */
        val id = line.substring(0,5)
        /*
         * [6:9] the ICAO of the MOSMIX station
         */
        var icao = line.substring(6,10)
        if (icao.startsWith("-")) icao = ""
        /*
         * [11:31] the name of the MOSMIX station
         */
        val name = line.substring(11,32).trim
        /*
         * [32:38] the latitude of the MOSMIX station
         */
        val lat = line.substring(32, 39).trim
        /*
         * [40:46] the longitude of the MOSMIX station
         */
        val lon = line.substring(40,46).trim
        /*
         * [46:52] the elevation of the MOSMIX station
         */
        val altitude = line.substring(46,52).trim
        /*
         * [60:] place of the MOSMIX station
         */
        val place = line.substring(60).trim
        /*
         * Leverage H3 geospatial indexing
         */
        val h3index = H3.instance
          .geoToH3(lat.toDouble, lon.toDouble, H3_RESOLUTION)

        MxStation(
          id       = id,
          icao     = icao,
          name     = name,
          lat      = lat.toDouble,
          lon      = lon.toDouble,
          h3index  = h3index,
          h3res    = H3_RESOLUTION,
          altitude = altitude.toDouble,
          place    = place)

      })
      /*
       * Filter stations that refer to those that have
       * assigned forecast data
       */
      .filter(station => ids.contains(station.id))

    stations.toSeq

  }

}
