package de.kp.works.beats.sensor.sensedge

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

import de.kp.works.beats.sensor.BeatConf
import de.kp.works.beats.sensor.sensedge.enums.SeProducts
import de.kp.works.beats.sensor.sensedge.enums.SeProducts.SeProduct

object SeConf {

  private var instance:Option[SeConf] = None

  def getInstance:SeConf = {
    if (instance.isEmpty) instance = Some(new SeConf())
    instance.get
  }

}

class SeConf extends BeatConf {
  /**
   * The (internal) resource folder file name
   */
  override var path: String = "sensedge.conf"

  def getProduct:SeProduct = {

    val productCfg = getProductCfg
    val product = productCfg.getString("name")

    try {
      SeProducts.withName(product)

    } catch {
      case _:Throwable =>
        throw new Exception(s"Sensedge product `$product` not supported.")
    }

  }

  def getRocksTables: Seq[String] = {

    val monitoring = {

      val measurements = getMeasurements
      /*
       * Finally apply optionally configured mappings
       */
      val mappings = getMappings
      if (mappings.isEmpty) measurements
      else {
        measurements.map(name => {
          if (mappings.contains(name)) mappings(name) else name
        })
      }
    }
    /*
     * SensorBeat supports anomaly detection and
     * timeseries forecasting for all the selected
     * monitoring attributes
     */
    val anomalies = monitoring.map(table => s"${table}_anom")
    val forecasts = monitoring.map(table => s"${table}_fore")

    val tables = monitoring ++ anomalies ++ forecasts
    tables

  }

}
