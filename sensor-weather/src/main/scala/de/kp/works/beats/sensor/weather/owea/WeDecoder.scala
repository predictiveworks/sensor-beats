package de.kp.works.beats.sensor.weather.owea

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

import com.google.gson.{JsonElement, JsonObject}
import de.kp.works.beats.sensor.weather.json.JsonUtil

object WeDecoder extends JsonUtil {
  /**
   * This method extract the attributes provided by
   * the OpenWeather response and transforms them
   * into a { key: value } format.
   *
   * The  fields (keys) are defined in [WeFields]
   */
  def decode(jsonObject: JsonObject): JsonElement = {

    val weather = initialObject
    /*
     * Extract and append `main`
     *
     *		"main": {
     *
     * 		Default unit: Kelvin
     *   	"temp": 293.06,
     *  		"feels_like": 292.23,
     *  		"temp_min": 291.98,
     *  		"temp_max": 294.25,
     *
     *  		Default hPa on the sea level if there is no `sea_level`
     *  		or `grnd_level` data
     *  		"pressure": 1014,
     *
     *  		[optional]
     *  		sea_level: [hPa],
     *  		grnd_level: [hPa]
     *
     *  		"humidity": 43 [percentage]
     *		}
     *
     */
    val jMain = getAsObject(jsonObject, "main")
    if (jMain != null) {

      weather.addProperty("temp", getOrElseDouble(jMain, "temp", 0D) - 273.15)
      weather.addProperty("feels_like", getOrElseDouble(jMain, "feels_like", 0D) - 273.15)
      weather.addProperty("temp_min", getOrElseDouble(jMain, "temp_min", 0D) - 273.15)
      weather.addProperty("temp_max", getOrElseDouble(jMain, "temp_max", 0D) - 273.15)

      weather.addProperty("pressure", getOrElseDouble(jMain, "pressure", -1D))
      weather.addProperty("humidity", getOrElseDouble(jMain, "humidity", -1D))

    }
    /*
     * 	"visibility": 10000 [meter]
     */
    weather.addProperty("visibility", getOrElseDouble(jsonObject, "visibility", -1))
    /*
     *
     * 	"wind": {
     *  	"speed": 4.02, [meter/sec]
     *   	"deg": 138,		[Degrees, meteorological]
     *   	"gust": 6.71   [meter/sec]
     *	}
     *
     */
    val jWind = getAsObject(jsonObject, "wind")
    if (jWind != null) {

      weather.addProperty("wind_speed", getOrElseDouble(jWind, "speed", -1D))
      weather.addProperty("wind_deg", getOrElseDouble(jWind, "deg", -1D))
      weather.addProperty("wind_gust", getOrElseDouble(jWind, "gust", -1D))

    }
    /*
     *		"clouds": {
     *   	  "all": 0 [Cloudiness, %]
     *		}
     *
     */
    val jclouds = getAsObject(jsonObject, "clouds")
    if (jclouds != null) {
      weather.addProperty("cloudiness", getOrElseDouble(jclouds, "all", -1D))
    }
    /*
     * 	"rain": {
     * 		"1h": ... [mm]
     * 		"3h": ... [mm]
     *  }
     */
    val jRain = getAsObject(jsonObject, "rain")
    if (jRain != null) {
      weather.addProperty("rain_1h", getOrElseInt(jRain, "1h", -1))
      weather.addProperty("rain_3h", getOrElseInt(jRain, "3h", -1))
    }
    /*
     * 	"snow": {
     * 		"1h": ... [mm]
     * 		"3h": ... [mm]
     *  }
     */
    val jSnow = getAsObject(jsonObject, "snow")
    if (jSnow != null) {
      weather.addProperty("snow_1h", getOrElseInt(jSnow, "1h", -1))
      weather.addProperty("snow_3h", getOrElseInt(jSnow, "3h", -1))
    }

    weather

  }

  private def initialObject: JsonObject = {

    val weather = new JsonObject()

    /* The default temperature: 0 K = -273.15 °C */
    weather.addProperty("temp", -273.15)
    weather.addProperty("feels_like", -273.15)
    weather.addProperty("temp_min", -273.15)
    weather.addProperty("temp_max", -273.15)

    weather.addProperty("pressure", -1D)
    weather.addProperty("humidity", -1D)

    weather.addProperty("wind_speed", -1D)
    weather.addProperty("wind_deg", -1D)
    weather.addProperty("wind_gust", -1D)

    weather.addProperty("cloudiness", -1D)

    weather.addProperty("rain_1h", -1)
    weather.addProperty("rain_3h", -1)

    weather.addProperty("snow_1h", -1)
    weather.addProperty("snow_3h", -1)

    weather

  }

}
