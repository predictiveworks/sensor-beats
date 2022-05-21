package de.kp.works.sensor.weather

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

import com.google.gson.JsonElement
import de.kp.works.beats.sensor.BeatSource
import de.kp.works.beats.sensor.http.HttpConnect

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

class WeMonitor(options:WeOptions, numThreads:Int = 1) extends WeLogging {
  /**
   * The configured time interval the consumer task
   * is executed
   */
  private val interval = options.getTimeInterval

  private var executorService:ScheduledExecutorService = _
  private val consumer = new WeConsumer(options)

  def start():Unit = {

    val worker = new Runnable {

      override def run(): Unit = {
        info(s"OpenWeather Consumer started.")
        consumer.subscribeAndPublish()
      }
    }

    try {

      executorService = Executors.newScheduledThreadPool(numThreads)
      executorService.scheduleAtFixedRate(worker, 0, interval, TimeUnit.MILLISECONDS)


    } catch {
      case t:Exception =>
        error(s"OpenWeather Monitor failed with: ${t.getLocalizedMessage}")
        stop()
    }

  }

  def stop():Unit = {

    executorService.shutdown()
    executorService.shutdownNow()

  }

}

/**
 * This class consumes weather data for a configured
 * geo spatial locations in recurring (configured) time
 * intervals and sends the retrieved data to the output
 * channels
 */
class WeConsumer(options:WeOptions) extends BeatSource with HttpConnect with WeLogging {

  private val BRAND_NAME = "OpenWeather"
  /**
   * The configured data sinks configured to send
   * OpenWeather sensor readings to
   */
  private val sinks = options.getSinks

  private val apiUrl = options.getBaseUrl
  private val apiKey = options.getApiKey

  private val station = options.getSource

  override def subscribeAndPublish(): Unit = {

    info(s"OpenWeather consumer: Extract data from [OpenWeather]")
    /*
     * Retrieve the weather data for the geo spatial
     * coordinates of the provided location
     */
    val lat = station.lat
    val lon = station.lon

    val sensorReadings = getByLatLon(lat, lon).getAsJsonObject

    val deviceId = station.id
    val sensorType = station.name

    send2Sinks(deviceId, BRAND_NAME, sensorType, sensorReadings, sinks)

  }

  private def getByLatLon(lat:Double, lon:Double):JsonElement = {

    val endpoint = s"${apiUrl}lat=$lat&lon=$lon&appid=$apiKey"

    val bytes = get(endpoint)
    val response = extractJsonBody(bytes)

    val weather = WeDecoder.decode(response.getAsJsonObject)
    weather

  }

}
