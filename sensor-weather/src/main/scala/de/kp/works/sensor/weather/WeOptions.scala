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

import de.kp.works.beats.sensor.BeatOutputs
import de.kp.works.beats.sensor.fiware.{Options => FiwareOptions}
import de.kp.works.beats.sensor.thingsboard.{Options => BoardOptions}
import de.kp.works.beats.sensor.thingsstack.{Options => ThingsOptions}
import de.kp.works.sensor.weather.enums.WeProducts.WeProduct

/**
 * Wrapper for OpenWeather sensors specific
 * service configuration
 */
class WeOptions(config:WeConf) {
  /**
   * Channels in the context of a `SensorBeat` are
   * actors that receive a `BeatSensor` message and
   * perform specific data operations like sending
   * to RocksDB, a FIWARE Context Broker and more
   */
  def getChannels:Seq[BeatOutputs.Value] = {
    try {
      config.getSinks.map(BeatOutputs.withName)

    } catch {
      case _:Throwable => Seq.empty[BeatOutputs.Value]
    }

  }
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

  def toBoard:BoardOptions[WeConf] =
    new BoardOptions[WeConf](config)

  def toFiware:FiwareOptions[WeConf] =
    new FiwareOptions[WeConf](config)

}
