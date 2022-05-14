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

import de.kp.works.beats.sensor.BeatConf

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

  def getProduct:MsProducts.Value = {

    val productCfg = getProductCfg
    val product = productCfg.getString("name")

    try {
      MsProducts.withName(product)

    } catch {
      case _:Throwable =>
        throw new Exception(s"Milesight product `$product` not supported.")
    }

  }
  /**
   * Retrieve the sensor specific table names of
   * the `SensorBeat` database; these are coded
   * within `MsTables`.
   */
  def getRocksTables: Seq[String] = {
    /*
     * Retrieve the correct product or sensor name
     * and determine the associated hard-coded table
     * names.
     */
    val product = getProduct
    MsTables.getTables(product)

  }
}
