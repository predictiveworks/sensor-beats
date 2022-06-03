package de.kp.works.beats.sensor.dragino

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
import de.kp.works.beats.sensor.dragino.enums.DoProducts.{DoProduct, LLMS01, LMDS200, LSE01, LSNPK01, LSPH01, WSC1_L}

object DoDecoder {

  def decodeHex(product: DoProduct, hexstring: String, fport:Int): JsonObject = {

    val readings = product match {
      case LLMS01 =>
        decoders.LLMS01.decodeHex(hexstring, fport)

      case LMDS200 =>
        decoders.LMDS200.decodeHex(hexstring, fport)

      case LSE01 =>
        decoders.LSE01.decodeHex(hexstring, fport)

      case LSNPK01 =>
        decoders.LSNPK01.decodeHex(hexstring, fport)

      case LSPH01 =>
        decoders.LSPH01.decodeHex(hexstring, fport)

      case WSC1_L =>
        decoders.WSC1_L.decodeHex(hexstring, fport)

    }

    readings

  }

  def tables(product: DoProduct):Seq[String] = {

    val fields = product match {
      case LLMS01 =>
        decoders.LLMS01.fields

      case LMDS200 =>
        decoders.LMDS200.fields

      case LSE01 =>
        decoders.LSE01.fields

      case LSNPK01 =>
        decoders.LSNPK01.fields

      case LSPH01 =>
        decoders.LSPH01.fields

      case WSC1_L =>
        decoders.WSC1_L.fields

    }

    fields

  }

}
