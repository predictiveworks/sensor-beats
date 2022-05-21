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
import de.kp.works.beats.sensor.milesight.enums.MsProducts.MsProduct
import de.kp.works.beats.sensor.milesight.enums.MsTables.MsTable
import de.kp.works.beats.sensor.milesight.enums.{MsProducts, MsTables}

object MsDecoder {

  def decodeHex(product: MsProduct, hexstring: String): JsonObject = {

    val readings = product match {
      case MsProducts.AM100 =>
        decoders.AM100.decodeHex(hexstring)

      case MsProducts.AM300 =>
        decoders.AM300.decodeHex(hexstring)

      case MsProducts.EM300_MCS =>
        decoders.EM300_MCS.decodeHex(hexstring)

      case MsProducts.EM300_SLD | MsProducts.EM300_ZLD =>
        decoders.EM300_SLD.decodeHex(hexstring)

      case MsProducts.EM300_TH =>
        decoders.EM300_TH.decodeHex(hexstring)

      case MsProducts.EM500_CO2 =>
        decoders.EM500_CO2.decodeHex(hexstring)

      case MsProducts.EM500_LGT =>
        decoders.EM500_LGT.decodeHex(hexstring)

      case MsProducts.EM500_PP =>
        decoders.EM500_PP.decodeHex(hexstring)

      case MsProducts.EM500_PT100 =>
        decoders.EM500_PT100.decodeHex(hexstring)

      case MsProducts.EM500_SMT =>
        decoders.EM500_SMT.decodeHex(hexstring)

      case MsProducts.EM500_SWL =>
        decoders.EM500_SWL.decodeHex(hexstring)

      case MsProducts.EM500_UDL =>
        decoders.EM500_CO2.decodeHex(hexstring)

      case MsProducts.UC500 =>
        decoders.UC500.decodeHex(hexstring)

    }

    readings

  }

  def tables(product: MsProduct):Seq[String] = {

    val fields = product match {
      case MsProducts.AM100 =>
        decoders.AM100.fields

      case MsProducts.AM300 =>
        decoders.AM300.fields

      case MsProducts.EM300_MCS =>
        decoders.EM300_MCS.fields

      case MsProducts.EM300_SLD | MsProducts.EM300_ZLD =>
        decoders.EM300_SLD.fields

      case MsProducts.EM300_TH =>
        decoders.EM300_TH.fields

      case MsProducts.EM500_CO2 =>
        decoders.EM500_CO2.fields

      case MsProducts.EM500_LGT =>
        decoders.EM500_LGT.fields

      case MsProducts.EM500_PP =>
        decoders.EM500_PP.fields

      case MsProducts.EM500_PT100 =>
        decoders.EM500_PT100.fields

      case MsProducts.EM500_SMT =>
        decoders.EM500_SMT.fields

      case MsProducts.EM500_SWL =>
        decoders.EM500_SWL.fields

      case MsProducts.EM500_UDL =>
        decoders.EM500_CO2.fields

      case MsProducts.UC500 =>
        decoders.UC500.fields

    }

    fields.map(field => {

      val tableName = field.replace(".", "_")
      val msTable = MsTables.withName(tableName)

      msTable.toString

    })

  }
}
