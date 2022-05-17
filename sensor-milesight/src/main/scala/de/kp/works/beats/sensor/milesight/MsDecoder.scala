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

import com.google.gson.JsonObject
import de.kp.works.beats.sensor.milesight.enums.MsProducts
import de.kp.works.beats.sensor.milesight.enums.MsProducts.MsProduct

object MsDecoder {

  def decodeHex(product: MsProduct, hexstring: String): JsonObject = {

    val readings = product match {
      case MsProducts.EM300_TH =>
        decoders.EM300_TH.decodeHex(hexstring)

      case MsProducts.EM500_CO2 =>
        decoders.EM500_CO2.decodeHex(hexstring)

      case MsProducts.EM500_UDL =>
        decoders.EM500_CO2.decodeHex(hexstring)

      case MsProducts.UC500 =>
        decoders.UC500.decodeHex(hexstring)

    }

    readings

  }
}
