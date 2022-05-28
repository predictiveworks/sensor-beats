package de.kp.works.beats.sensor.uradmonitor.decoders

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

trait BaseDecoder {

  def decodeHex(hexstring:String):JsonObject = {
    /*
     * HINT: It is important to covert the hex string
     * into an INT Array to ensure proper decoding
     */
    val bytes = hexstring.sliding(2,2).toArray.map(hex => {
      Integer.parseInt(hex, 16)
    })

    decode(bytes)

  }

  def decode(bytes:Array[Int]):JsonObject

  def uint16(value1:Int, value2:Int):Int = {
    (value1 << 8) + value2
  }

  def uint24(value1:Int, value2:Int, value3:Int):Int = {
    (value1 << 16) + (value2 << 8) + value3
  }

  def uint16double(value1:Int, value2:Int, multiplier:Int):Double = {

    val value = uint16(value1, value2)

    if ((value & 0x8000) == 1)
      return (value & 0x7FFF).toDouble / -multiplier

    value.toDouble / multiplier

  }

  def uint24double(value1:Int, value2:Int, value3:Int, multiplier:Int):Double = {

    val value = uint24(value1, value2, value3)

    if ((value & 0x800000) == 1)
      return (value & 0x7FFFFF).toDouble / -multiplier

    value.toDouble / multiplier

  }

  def fields:Seq[String]

}

