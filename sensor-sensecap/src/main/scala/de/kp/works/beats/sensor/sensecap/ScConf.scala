package de.kp.works.beats.sensor.sensecap

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
import de.kp.works.beats.sensor.sensecap.enums.ScProducts.ScProduct
import de.kp.works.beats.sensor.sensecap.enums.ScProducts

object ScConf {

  private var instance:Option[ScConf] = None

  def getInstance:ScConf = {
    if (instance.isEmpty) instance = Some(new ScConf())
    instance.get
  }

}

class ScConf extends BeatConf {
  /**
   * The (internal) resource folder file name
   */
  override var path: String = "sensecap.conf"

  def getProduct:ScProduct = {

    val productCfg = getProductCfg
    val product = productCfg.getString("name")

    try {
      ScProducts.withName(product)

    } catch {
      case _:Throwable =>
        throw new Exception(s"SenseCap product `$product` not supported.")
    }

  }
  /**
   * Retrieve the sensor specific table names of
   * the `SensorBeat` database. These tables are
   * generated by leveraging static product specific
   * tables (see ScDecoder), dynamically configured
   * measurements and configured mappings
   */
  def getRocksTables: Seq[String] = {
    /*
     * Retrieve the correct product or sensor name
     * and determine the associated hard-coded table
     * names.
     */
    val product = getProduct
    val monitoring = {
      /*
       * Retrieve those tables that are statically
       * assigned to a certain product
       */
      val statics = ScDecoder.tables(product)
      /*
       * Join static tables with optionally configured
       * measurements
       */
      val measurements = getMeasurements
      val joined = (statics ++ measurements).distinct
      /*
       * Finally apply optionally configured mappings
       */
      val mappings = getMappings
      if (mappings.isEmpty) joined
      else {
        joined.map(name => {
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
