package de.kp.works.beats.sensor.ellenex

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
import de.kp.works.beats.sensor.ellenex.enums.ExProducts._

object ExDecoder {

  def decodeHex(product: ExProduct, hexstring: String, fport:Int): JsonObject = {

    val readings = product match {
      case PLS2_L =>
        decoders.PLS2_L.decodeHex(hexstring, fport)

      case PTD2_L =>
        decoders.PTD2_L.decodeHex(hexstring, fport)

      case PTS2_L =>
        decoders.PTS2_L.decodeHex(hexstring, fport)

    }

    readings

  }

}
