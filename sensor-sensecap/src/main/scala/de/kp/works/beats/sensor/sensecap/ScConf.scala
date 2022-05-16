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
import de.kp.works.beats.sensor.sensecap.enums.{ScProducts, ScTables}

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

  def getProduct:ScProducts.Value = {

    val productCfg = getProductCfg
    val product = productCfg.getString("name")

    try {
      ScProducts.withName(product)

    } catch {
      case _:Throwable =>
        throw new Exception(s"Milesight product `$product` not supported.")
    }

  }
  /**
   * Retrieve the sensor specific table names of
   * the `SensorBeat` database; these are coded
   * within `ScTables`.
   */
  def getRocksTables: Seq[String] = {
    /*
     * Retrieve the correct product or sensor name
     * and determine the associated hard-coded table
     * names.
     */
    val product = getProduct
    ScTables.getTables(product)

  }

}
