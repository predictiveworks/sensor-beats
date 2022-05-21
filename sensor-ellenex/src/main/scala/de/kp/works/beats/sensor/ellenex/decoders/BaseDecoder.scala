package de.kp.works.beats.sensor.ellenex.decoders

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

  def decodeHex(hexstring:String, fport:Int):JsonObject = {
    /*
     * HINT: It is important to covert the hex string
     * into an INT Array to ensure proper decoding
     */
    val bytes = hexstring.sliding(2,2).toArray.map(hex => {
      Integer.parseInt(hex, 16)
    })

    decode(bytes, fport)

  }

  def decode(bytes:Array[Int], fport:Int):JsonObject

  /**
   * The readHex2bytes function is built to decode
   * a signed 16-bit integer represented by 2 bytes.
   *
   * Note, the original (Ellenex) version does not
   * work for Scala | Java
   */
  def readHex2bytes(byte1:Int, byte2:Int):Int = {

    val bytes = Array(byte2, byte1)
    readInt16LE(bytes)

  }

  def readUInt16LE(bytes:Array[Int]): Int = {
    val value = (bytes(1) << 8) + bytes(0)
    value & 0xffff
  }

  def readInt16LE(bytes:Array[Int]): Int = {
    val ref = readUInt16LE(bytes)
    if (ref > 0x7fff) ref - 0x10000 else ref
  }

}
