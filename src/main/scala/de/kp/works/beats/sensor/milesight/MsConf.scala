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

import de.kp.works.beats.sensor.{BeatConf, BeatTasks}

import scala.collection.JavaConversions.asScalaBuffer

object MsConf {

  private var instance:Option[MsConf] = None

  def getInstance:MsConf = {
    if (instance.isEmpty) instance = Some(new MsConf())
    instance.get
  }

}

class MsConf extends BeatConf {
  /**
   * The (internal) resource folder file name
   */
  override var path: String = "milesight.conf"
  /**
   * The name of the configuration file used
   * with logging
   */
  override var logname: String = "Beat"
  /**
   * Channels in the context of a `SensorBeat` are
   * actors that receive a `BeatSensor` message and
   * perform specific data operations like sending
   * to RocksDB, a FIWARE Context Broker and more
   */
  def getChannels:Seq[String] = {
    val outputCfg = getOutputCfg
    outputCfg.getStringList("channels")
  }
  /**
   * This method provides the number of threads used
   * to build the deep learning monitors
   */
  override def getNumThreads: Int = ???
  /**
   * Retrieve the sensor specific table names of
   * the `SensorBeat` database; these are coded
   * within `MsTables`.
   */
  override def getRocksTables: Seq[String] = {
    MsTables.values
      .map(value => value.toString)
      .toSeq
  }
  /**
   * The scheduler intervals for the deep learning
   * jobs for anomaly detection as well as time series
   * forecasting
   */
  override def getSchedulerIntervals: Map[BeatTasks.Value, Int] = ???
}
