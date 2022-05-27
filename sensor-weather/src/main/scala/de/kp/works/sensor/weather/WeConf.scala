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

import com.typesafe.config.Config
import de.kp.works.beats.sensor.BeatConf
import de.kp.works.sensor.weather.enums.WeProducts
import de.kp.works.sensor.weather.enums.WeProducts.WeProduct

object WeConf {

  private var instance:Option[WeConf] = None

  def getInstance:WeConf = {
    if (instance.isEmpty) instance = Some(new WeConf())
    instance.get
  }

}

class WeConf extends BeatConf {
  /**
   * The (internal) resource folder file name
   */
  override var path: String = "weather.conf"

  def getProduct:WeProduct = {

    val productCfg = getProductCfg
    val product = productCfg.getString("name")

    try {
      WeProducts.withName(product)

    } catch {
      case _:Throwable =>
        throw new Exception(s"OpenWeather product `$product` not supported.")
    }

  }

  def getMosmixCfg: Config = getCfg("mosmix")

  override def getRocksTables: Seq[String] = ???

  def getWeatherCfg: Config = getCfg("weather")

}
