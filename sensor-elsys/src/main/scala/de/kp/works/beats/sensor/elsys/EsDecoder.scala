package de.kp.works.beats.sensor.elsys

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
import de.kp.works.beats.sensor.elsys.enums.EsProducts
import de.kp.works.beats.sensor.elsys.enums.EsProducts.EsProduct

object EsDecoder {

  def decodeHex(product: EsProduct, hexstring: String): JsonObject = {

    val readings = product match {
      case EsProducts.ELT_2 =>
        decoders.ELT_2.decodeHex(hexstring)

      case EsProducts.ELT_LITE =>
        decoders.ELT_LITE.decodeHex(hexstring)

      case EsProducts.ELT_ULTRASONIC =>
        decoders.ELT_ULTRASONIC.decodeHex(hexstring)

      case EsProducts.EMS =>
        decoders.EMS.decodeHex(hexstring)

      case EsProducts.EMS_DESK =>
        decoders.EMS_DESK.decodeHex(hexstring)

      case EsProducts.EMS_DOOR =>
        decoders.EMS_DOOR.decodeHex(hexstring)

      case EsProducts.EMS_LITE =>
        decoders.EMS_LITE.decodeHex(hexstring)

      case EsProducts.ERS =>
        decoders.ERS.decodeHex(hexstring)

      case EsProducts.ERS_CO2 =>
        decoders.ERS_CO2.decodeHex(hexstring)

      case EsProducts.ERS_CO2_LITE =>
        decoders.ERS_CO2_LITE.decodeHex(hexstring)

      case EsProducts.ERS_EYE =>
        decoders.ERS_EYE.decodeHex(hexstring)

      case EsProducts.ERS_LITE =>
        decoders.ERS_LITE.decodeHex(hexstring)

      case EsProducts.ERS_SOUND =>
        decoders.ERS_SOUND.decodeHex(hexstring)

      case EsProducts.ERS_VOC =>
        decoders.ERS_VOC.decodeHex(hexstring)

    }

    readings

  }

  def tables(product: EsProduct):Seq[String] = {

    val fields = product match {
      case EsProducts.ELT_2 =>
        decoders.ELT_2.fields

      case EsProducts.ELT_LITE =>
        decoders.ELT_LITE.fields

      case EsProducts.ELT_ULTRASONIC =>
        decoders.ELT_ULTRASONIC.fields

      case EsProducts.EMS =>
        decoders.EMS.fields

      case EsProducts.EMS_DESK =>
        decoders.EMS_DESK.fields

      case EsProducts.EMS_DOOR =>
        decoders.EMS_DOOR.fields

      case EsProducts.EMS_LITE =>
        decoders.EMS_LITE.fields

      case EsProducts.ERS =>
        decoders.ERS.fields

      case EsProducts.ERS_CO2 =>
        decoders.ERS_CO2.fields

      case EsProducts.ERS_CO2_LITE =>
        decoders.ERS_CO2_LITE.fields

      case EsProducts.ERS_EYE =>
        decoders.ERS_EYE.fields

      case EsProducts.ERS_LITE =>
        decoders.ERS_LITE.fields

      case EsProducts.ERS_SOUND =>
        decoders.ERS_SOUND.fields

      case EsProducts.ERS_VOC =>
        decoders.ERS_VOC.fields

    }

    fields

  }
}
