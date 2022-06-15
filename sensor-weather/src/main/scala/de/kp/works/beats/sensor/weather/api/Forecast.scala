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

import com.google.gson.{JsonArray, JsonElement}
import de.kp.works.beats.sensor.weather.WeMessages
import de.kp.works.beats.sensor.weather.dwd.MxFrame
import org.apache.spark.sql.SparkSession

class Forecast(session:SparkSession) extends JsonActor {
  /**
   * The response of this request is a JsonArray;
   * in case of an invalid request, an empty response
   * is returned
   */
  override def getEmpty = new JsonArray
  /**
   * The [DataFrame] interface to MOSMIX station
   * forecasts
   */
  private val mxFrame = new MxFrame(session)

  override def executeJson(json:JsonElement): String = {

    val req = mapper.readValue(json.toString, classOf[ForecastReq])

    val lat = req.latitude
    val lon = req.longitude

    val resolution = req.resolution
    val latest = req.latest

    try {

      val dataframe =
        mxFrame.load(lat=lat, lon=lon, resolution=resolution, latest=latest)

      if (dataframe.isEmpty) emptyResponse.toString
      else {

        val result = dataframeToJson(dataframe)
        result.toString

      }

    } catch {
      case t: Throwable =>
        error(WeMessages.forecastFailed(t))
        emptyResponse.toString
    }

  }

}
