package de.kp.works.beats.sensor.weather

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

import de.kp.works.beats.sensor.BeatOutputs
import de.kp.works.beats.sensor.fiware.{Options => FiwareOptions}
import de.kp.works.beats.sensor.thingsboard.{Options => BoardOptions}
import de.kp.works.beats.sensor.weather.enums.WeProducts.WeProduct

case class WeStation(
  /*
   * Unique identifier of a certain
   * weather station
   */
  id: String,
  /*
   * Name of the weather station
   */
  name: String,
  /*
   * Latitude of the weather station
   */
  lat: Double,
  /*
   * Longitude of the weather station
   */
  lon: Double)
/**
 * Wrapper for OpenWeather sensors specific
 * service configuration
 */
class WeOptions(config:WeConf) {

  private val weatherCfg = config.getWeatherCfg
  /**
   * The API key to access the OpenWeather API
   */
  def getApiKey:String = weatherCfg.getString("apiKey")
  /**
   * The base url of the OpenWeather REST server
   */
  def getBaseUrl:String = weatherCfg.getString("serverUrl")
  /**
   * Public method to retrieve the supported
   * OpenWeather product name; in case of an
   * unsupported sensor, an exception is thrown
   */
  def getProduct:WeProduct =
    config.getProduct
  /**
   * Public wrapper method to retrieve the RocksDB
   * file system folder
   */
  def getRocksFolder:String =
    config.getRocksFolder
  /**
   * Public wrapper method to retrieve the RocksDB
   * column families that are specified for the
   * OpenWeather sensor.
   */
  def getRocksTables:Seq[String] =
    config.getRocksTables

  def getSource:WeStation = ???
  /**
   * Sinks in the context of a `SensorBeat` are
   * actors that receive a `BeatSensor` message
   * and perform specific data operations like
   * sending to RocksDB, a FIWARE Context Broker
   * and more
   */
  def getSinks:Seq[BeatOutputs.Value] = {
    try {
      config.getSinks.map(BeatOutputs.withName)

    } catch {
      case _:Throwable => Seq.empty[BeatOutputs.Value]
    }
  }
  /**
   * The time interval determine how often the
   * OpenWeather API is requested to retrieve
   * location specific data
   */
  def getTimeInterval:Long = {

    val interval = weatherCfg.getString("interval")
    interval match {
      case "5m" =>
        1000 * 60 * 5
      case "10m" =>
        1000 * 60 * 10
      case "15m" =>
        1000 * 60 * 15
      case "30m" =>
        1000 * 60 * 30
      case "1h" =>
        1000 * 60 * 60
      case "3h" =>
        1000 * 60 * 60 * 3
      case "6h" =>
        1000 * 60 * 60 * 6
      case "12h" =>
        1000 * 60 * 60 * 12
      case _ =>
        val now = new java.util.Date()
        throw new Exception(s"[ERROR] $now.toString - The time interval `$interval` is not supported.")

    }

  }

  def toBoard:BoardOptions[WeConf] =
    new BoardOptions[WeConf](config)

  def toFiware:FiwareOptions[WeConf] =
    new FiwareOptions[WeConf](config)

}
