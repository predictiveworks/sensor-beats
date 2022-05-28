package de.kp.works.beats.sensor.uradmonitor

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

import de.kp.works.beats.sensor.fiware.{Options => FiwareOptions}
import de.kp.works.beats.sensor.helium.{Options => HeliumOptions}
import de.kp.works.beats.sensor.loriot.{Options => LoriotOptions}
import de.kp.works.beats.sensor.thingsboard.{Options => BoardOptions}
import de.kp.works.beats.sensor.thingsstack.{Options => StackOptions}
import de.kp.works.beats.sensor.uradmonitor.enums.UmProducts.UmProduct
import de.kp.works.beats.sensor.{BeatInputs, BeatOutputs}

/**
 * Wrapper for Uradmonitor sensors specific
 * service configuration
 */
class UmOptions(config:UmConf) {
  /**
   * Public method to retrieve the supported
   * Uradmonitor product name; in case of an
   * unsupported sensor, an exception is thrown
   */
  def getProduct:UmProduct =
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
   * Uradmonitor sensor.
   */
  def getRocksTables:Seq[String] =
    config.getRocksTables
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
  def getSource:BeatInputs.Value = {
    try {
      BeatInputs.withName(config.getSource)

    } catch {
      case _:Throwable => null
    }
  }
  def toBoard:BoardOptions[UmConf] =
    new BoardOptions[UmConf](config)

  def toFiware:FiwareOptions[UmConf] =
    new FiwareOptions[UmConf](config)
  /**
   * The Helium configuration to access the Helium
   * network, which is one of the input channels of
   * a Sensor Beat
   */
  def toHelium:HeliumOptions[UmConf] =
    new HeliumOptions[UmConf](config)
  /**
   * The Loriot configuration to access the Loriot
   * network, which is one of the input channels of
   * a Sensor Beat
   */
  def toLoriot:LoriotOptions[UmConf] =
    new LoriotOptions[UmConf](config)
  /**
   * The ThingsStack configuration to access The Things
   * Network, which is one of the input channels of a
   * Sensor Beat
   */
  def toStack:StackOptions[UmConf] =
    new StackOptions[UmConf](config)

}
