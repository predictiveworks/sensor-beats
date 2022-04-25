package de.kp.works.beats.sensor.milesight

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

import de.kp.works.beats.sensor.Channels
import de.kp.works.beats.sensor.fiware.{Options => FiwareOptions}
import de.kp.works.beats.sensor.thingsstack.{Options => ThingsOptions}

object MsFields {
  /**
   * Milesight devices decoded payload fields
   */
  val BATTERY: String     = "battery"
  val HUMIDITY: String    = "humidity"
  val TEMPERATURE: String = "temperature"

}

object MsProducts extends Enumeration {
  type MsProduct = Value

  val EM_300: MsProducts.Value = Value(1, "EM_300")

}
/**
 * Wrapper for Milesight sensors specific
 * service configuration
 */
class MsOptions(config:MsConf) {

  private val fiwareOptions = new FiwareOptions[MsConf](config)
  private val thingsOptions = new ThingsOptions[MsConf](config)

  /**
   * Channels in the context of a `SensorBeat` are
   * actors that receive a `BeatSensor` message and
   * perform specific data operations like sending
   * to RocksDB, a FIWARE Context Broker and more
   */
  def getChannels:Seq[Channels.Value] = {
    try {
      config.getChannels.map(Channels.withName)

    } catch {
      case _:Throwable => Seq.empty[Channels.Value]
    }

  }

  def getProduct:MsProducts.Value = ???

  def getRocksFolder:String = ???
  /**
   * Public wrapper method to retrieve the RocksDB
   * column families that are specified for the
   * Milesight sensor.
   */
  def getRocksTables:Seq[String] = config.getRocksTables

  def toFiware:FiwareOptions[MsConf] = fiwareOptions

  def toThings:ThingsOptions[MsConf] = thingsOptions

}
