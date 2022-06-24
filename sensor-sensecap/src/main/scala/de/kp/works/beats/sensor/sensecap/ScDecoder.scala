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

import com.google.gson.JsonObject
import de.kp.works.beats.sensor.sensecap.enums.ScProducts
import de.kp.works.beats.sensor.sensecap.enums.ScProducts.ScProduct

object ScDecoder {

  def decodeHex(product: ScProduct, hexstring: String): JsonObject = {

    val readings = product match {
      case ScProducts.AIR_TH =>
        decoders.AIR_TH.decodeHex(hexstring)

      case ScProducts.BARO_PRESSURE =>
        decoders.BARO_PRESSURE.decodeHex(hexstring)

      case ScProducts.CO2 =>
        decoders.CO2.decodeHex(hexstring)

      case ScProducts.LIGHT_INTENSITY =>
        decoders.LIGHT_INTENSITY.decodeHex(hexstring)

      case ScProducts.SOIL_MT =>
        decoders.SOIL_MT.decodeHex(hexstring)

    }

    readings

  }

  def tables(product: ScProduct):Seq[String] = {

    val fields = product match {
      case ScProducts.AIR_TH =>
        decoders.AIR_TH.fields

      case ScProducts.BARO_PRESSURE =>
        decoders.BARO_PRESSURE.fields

      case ScProducts.CO2 =>
        decoders.CO2.fields

      case ScProducts.LIGHT_INTENSITY =>
        decoders.LIGHT_INTENSITY.fields

      case ScProducts.SOIL_MT =>
        decoders.SOIL_MT.fields

    }

    fields

  }
}
