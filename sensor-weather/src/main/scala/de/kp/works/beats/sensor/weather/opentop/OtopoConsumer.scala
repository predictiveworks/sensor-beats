package de.kp.works.beats.sensor.weather.opentop

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

import com.google.gson.JsonParser
import de.kp.works.beats.sensor.http.HttpConnect
import de.kp.works.beats.sensor.weather.WeLogging

import scala.collection.JavaConversions.iterableAsScalaIterable

case class OtopoDS(
  name:String,
  resolution:String,
  extent: String,
  source: String
)
/**
 * The [OpenTopoData] consumer determines the altitude
 * (or elevation) of a certain geospatial point.
 *
 * The consumer is built to be used in combination with
 * PV power generation requests, in cases where altitude
 * is not provided.
 */
class OtopoConsumer extends HttpConnect with WeLogging {
  /**
   * https://www.opentopodata.org/
   *
   * API limitations:
   *
   * - Max 100 locations per request.
   * - Max 1 call per second.
   * - Max 1000 calls per day.
   */
  private val BASE_URL = "https://api.opentopodata.org"
  private val DATASETS =
    """
      |[
      |{
      |"name: "aster30m",
      |"resolution": "30m",
      |"extent": "Global",
      |"source" "NASA"
      |},
      |{
      |"name: "bkg200m",
      |"resolution": "200m",
      |"extent": "Germany",
      |"source" "BKG"
      |},
      |{
      |"name: "emod2018",
      |"resolution": "100m",
      |"extent": "Bathymetry for ocean and sea in Europe",
      |"source" "EMODnet"
      |},
      |{
      |"name: "etop1",
      |"resolution": "1800m",
      |"extent": "Global, including bathymetry and ice surface elevation near poles",
      |"source" "NOAA"
      |},
      |{
      |"name: "eudem25m",
      |"resolution": "25m",
      |"extent": "Europe",
      |"source" "EEA"
      |},
      |{
      |"name: "gebco2020",
      |"resolution": "450m",
      |"extent": "Global bathymetry and land elevation",
      |"source" "GEBCO"
      |},
      |{
      |"name: "mapzen",
      |"resolution": "30m",
      |"extent": "Global, including bathymetry",
      |"source" "Mapzen"
      |},
      |{
      |"name: "ned10m",
      |"resolution": "10m",
      |"extent": "Continental USA, Hawaii, parts of Alaska",
      |"source" "USGS"
      |},
      |{
      |"name: "nzdem8m",
      |"resolution": "8m",
      |"extent": "New Zealand",
      |"source" "LINZ"
      |},
      |{
      |"name: "srtm30m",
      |"resolution": "30m",
      |"extent": "Latitudes from -60 to 60",
      |"source" "USGS"
      |},
      |{
      |"name: "srtm90m",
      |"resolution": "90m",
      |"extent": "Latitudes from -60 to 60",
      |"source" "USGS"
      |}
      |]
      |""".stripMargin

  private val datasets = buildDatasets
    .map(ds => (ds.name, ds)).toMap

  def buildDatasets:Seq[OtopoDS] = {

    val json = JsonParser.parseString(DATASETS)
      .getAsJsonArray

    json.map(elem => {
      val ds = elem.getAsJsonObject
      OtopoDS(
        name       = ds.get("name").getAsString,
        resolution = ds.get("resolution").getAsString,
        extent     = ds.get("extent").getAsString,
        source     = ds.get("source").getAsString
      )
    }).toSeq

  }

  def getElevation(lat:Double, lon:Double, dataset:String="mapzen"):Double = {

    val ds = if (datasets.contains(dataset)) dataset else "mapzen"
    val endpoint = s"$BASE_URL/v1/$ds?locations=$lat,$lon"

    val bytes = get(endpoint)
    val response = extractJsonBody(bytes).getAsJsonObject

    val status = response.get("status").getAsString
    if (status.toLowerCase == "ok") {

      val results = response.get("results").getAsJsonArray
      results.head.getAsJsonObject.get("elevation").getAsDouble

    }
    else
      Double.NaN

  }
  def getDatasets:Seq[String] = {

    val endpoint = s"$BASE_URL/datasets"

    val bytes = get(endpoint)
    val response = extractJsonBody(bytes).getAsJsonObject

    val status = response.get("status").getAsString
    if (status.toLowerCase == "ok") {

      response.get("results")
        .getAsJsonArray
        .map(elem =>
          elem.getAsJsonObject.get("name").getAsString
        )
        .toSeq

    } else {
      Seq.empty[String]
    }

  }

}
