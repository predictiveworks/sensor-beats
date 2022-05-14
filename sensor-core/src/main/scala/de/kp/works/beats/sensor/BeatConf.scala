package de.kp.works.beats.sensor

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

import akka.http.scaladsl.ConnectionContext
import com.typesafe.config.{Config, ConfigFactory}
import de.kp.works.beats.sensor.BeatTasks.{ANOMALY, FORECAST}
import de.kp.works.beats.sensor.ssl.SslOptions

import scala.collection.JavaConversions.asScalaBuffer

/**
 * The base class for all sensor specific
 * `beat` configurations.
 */
abstract class BeatConf {
  /**
   * The (internal) resource folder file name
   */
  var path: String
  /**
   * The name of the configuration file used
   * with logging
   */
  var logname: String = "Beat"
  /**
   * In case of a deployed `Sensor Beat`, the file system
   * path to the configuration folder is provided as system
   * property `config.dir`
   */
  val folder: String = System.getProperty("config.dir")
  /**
   * This is the reference to the overall configuration
   * file that holds all configuration required for this
   * application
   */
  var cfg: Option[Config] = None
  /**
   * This method provides the configuration of the
   * API actors of the Sensor Beat
   */
  def getActorCfg: Config = getCfg("actor")
  /**
   * This method provides the configuration of the
   * HTTP Binding of the Sensor Beat
   */
  def getBindingCfg: Config = getCfg("binding")
  /**
   * A utility method to retrieve configurations by
   * name from the entire Sensor Beat configuration
   */
  def getCfg(name: String): Config = {

    if (cfg.isEmpty)
      throw new Exception(s"Configuration not initialized.")

    cfg.get.getConfig(name)

  }
  /**
   * Channels in the context of a `SensorBeat` are
   * actors that receive a `BeatSensor` message and
   * perform specific data operations like sending
   * to RocksDB, a FIWARE Context Broker and more
   */
  def getChannels:Seq[String] = {
    val outputCfg = getOutputCfg
    outputCfg.getStringList("channels").toSeq
  }
  /**
   * Method to build the HTTPS connection context;
   * this method is used by the `BeatService`
   */
  def getConnectionContext:Option[ConnectionContext] = {

    val security = getSecurityCfg
    if (security.getString("ssl") == "false") None

    else
      Some(SslOptions.buildConnectionContext(security))

  }
  /**
   * FIWARE is one of the output channels of a
   * `SensorBeat` and this configuration describes
   * respective access parameters
   */
  def getFiwareCfg: Config = getCfg("fiware")
  /**
   * Method to determine the logging folder for the
   * `SensorBeat` project
   */
  def getLogFolder:String = {
    /*
     * Determine the logging folder from the system
     * property `logging.dir`. If this property is
     * not set, fallback to the logging configuration
     */
    val folder = System.getProperty("logging.dir")
    if (folder == null)
      getLoggingCfg.getString("folder")

    else folder

  }
  /**
   * This method provides the logging configuration of
   * the Sensor Beat
   */
  def getLoggingCfg: Config = getCfg("logging")
  /**
   * This method provides the number of threads used
   * to build the deep learning monitors
   */
  def getNumThreads: Map[BeatTasks.Value, Int] = {

    val learningCfg = getCfg("learning")
    /*
     * The number of threads can be configured
     * independently whether it is the anomaly
     * detection or the timeseries forecasting
     * monitor.
     */
    Map(
      ANOMALY ->
        learningCfg.getInt("anomThreads"),

      FORECAST ->
        learningCfg.getInt("foreThreads")
    )

  }
  /**
   * This method provides the configuration for the
   * `SensorBeat`'s output channels
   */
  def getOutputCfg: Config = getCfg("output")
  /**
   * This method provides the configuration for the
   * SensorBeats's physical sensor product info
   */
  def getProductCfg: Config = getCfg("product")
  /**
   * This method provides the RocksDB configuration of
   * the Sensor Beat
   */
  def getRocksDBCfg: Config = getCfg("rocksdb")
  /**
   * Method to determine the RocksDB folder for the
   * `SensorBeat` project
   */
  def getRocksFolder:String = {
    /*
     * Determine the RocksDB folder from the system
     * property `rocksdb.dir`. If this property is
     * not set, fallback to the logging configuration
     */
    val folder = System.getProperty("rocksdb.dir")
    if (folder == null)
      getRocksDBCfg.getString("folder")

    else folder

  }
  def getRocksTables: Seq[String]
  /**
   * The scheduler intervals for the deep learning
   * jobs for anomaly detection as well as time series
   * forecasting
   */
  def getSchedulerIntervals: Map[BeatTasks.Value, Int] = {

    val learningCfg = getCfg("learning")
    /*
     * The scheduling intervals can be configured
     * independently whether it is the anomaly
     * detection or the timeseries forecasting
     * monitor.
     */
    Map(
      ANOMALY ->
        learningCfg.getInt("anomInterval"),

      FORECAST ->
        learningCfg.getInt("foreInterval")
    )

  }
  /**
   * This method provides the SSL configuration of the
   * Sensor Beat (HTTP Server)
   */
  def getSecurityCfg: Config = getCfg("security")
  /**
   * ThingsBoard is one of the output channels of a
   * `SensorBeat` and this configuration describes
   * respective access parameters
   */
  def getThingsBoardCfg: Config = getCfg("thingsboard")
  /**
   * The Things Stack is one of the input channels of a
   * `SensorBeat` and this configuration describes
   * respective access parameters
   */
  def getThingsStackCfg: Config = getCfg("thingsstack")

  def init(): Boolean = {

    if (cfg.isDefined) true
    else {
      try {

        val config = loadAsString
        cfg = if (config.isDefined) {
          /*
           * An external configuration file is provided
           * and must be transformed into a Config
           */
          Option(ConfigFactory.parseString(config.get))

        } else {
          /*
           * The internal reference file is used to
           * extract the required configurations
           */ Option(ConfigFactory.load(path))

        }
        true

      } catch {
        case _: Throwable =>
          false
      }
    }
  }

  def isInit: Boolean = {
    cfg.isDefined
  }

  def loadAsString: Option[String] = {

    val name = "Sensor Beat"
    try {

      val configFile =
        if (folder == null) None else Some(s"$folder$path")

      if (configFile.isEmpty) {
        println(s"Launch `$name` with internal $logname configuration.")
        None

      } else {
        println(s"Launch `$name` with external $logname configuration.")

        val source = scala.io.Source.fromFile(new java.io.File(configFile.get))
        val config = source.getLines.mkString("\n")

        source.close
        Some(config)

      }

    } catch {
      case t: Throwable =>
        println(s"Loading `$name` $logname configuration failed: ${t.getLocalizedMessage}")
        None
    }

  }

}
