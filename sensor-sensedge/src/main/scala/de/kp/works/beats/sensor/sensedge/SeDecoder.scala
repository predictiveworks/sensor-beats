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

import com.google.gson.JsonObject
import de.kp.works.beats.sensor.sensedge.enums.SeProducts
import de.kp.works.beats.sensor.sensedge.enums.SeProducts.SeProduct

object SeDecoder {

  def decodeHex(product: SeProduct, hexstring: String, fport:Int): JsonObject = {

    val readings = product match {
      case SeProducts.SENSTICK_PRO =>
        decoders.SENSTICK_PRO.decodeHex(hexstring, fport)

      case SeProducts.SENSTICK_PURE =>
        decoders.SENSTICK_PURE.decodeHex(hexstring, fport)

    }

    readings

  }
}
