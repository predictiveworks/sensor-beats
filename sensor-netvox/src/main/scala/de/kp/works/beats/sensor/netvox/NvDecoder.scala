package de.kp.works.beats.sensor.netvox

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
import de.kp.works.beats.sensor.netvox.enums.NvProducts
import de.kp.works.beats.sensor.netvox.enums.NvProducts.NvProduct

object NvDecoder {

  def decodeHex(product: NvProduct, hexstring: String, fport:Int): JsonObject = {

    val readings = product match {
      case NvProducts.R711 =>
        decoders.R711.decodeHex(hexstring, fport)

      case NvProducts.R718A =>
        decoders.R718A.decodeHex(hexstring, fport)

      case NvProducts.R718AB =>
        decoders.R718AB.decodeHex(hexstring, fport)

      case NvProducts.R720A =>
        decoders.R720A.decodeHex(hexstring, fport)

      case NvProducts.RB11E =>
        decoders.RB11E.decodeHex(hexstring, fport)

    }

    readings

  }

  def tables(product: NvProduct):Seq[String] = {

    val fields = product match {
      case NvProducts.R711 =>
        decoders.R711.fields

      case NvProducts.R718A =>
        decoders.R718A.fields

      case NvProducts.R718AB =>
        decoders.R718AB.fields

      case NvProducts.R720A =>
        decoders.R720A.fields

      case NvProducts.RB11E =>
        decoders.RB11E.fields

    }

    fields

  }
}
