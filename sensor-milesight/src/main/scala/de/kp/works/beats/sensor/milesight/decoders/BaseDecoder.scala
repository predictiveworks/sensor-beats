package de.kp.works.beats.sensor.milesight.decoders

import com.google.gson.JsonObject

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

  /*******************************************
   * bytes to number
   ********************************************/

  def readUInt8LE(bytes:Int): Int = {
    bytes & 0xFF
  }

  def readInt8LE(bytes:Int):Int = {
    val ref = readUInt8LE(bytes)
    if (ref > 0x7F) ref - 0x100 else ref
  }

  def readUInt16LE(bytes:Array[Int]): Int = {
    val value = (bytes(1) << 8) + bytes(0)
    value & 0xffff
  }

  def readInt16LE(bytes:Array[Int]): Int = {
    val ref = readUInt16LE(bytes)
    if (ref > 0x7fff) ref - 0x10000 else ref
  }

  def readUInt32LE(bytes:Array[Int]):Int = {
    val value = (bytes(3) << 24) + (bytes(2) << 16) + (bytes(1) << 8) + bytes(0)
    value & 0xFFFFFFFF
  }

  def readInt32LE(bytes:Array[Int]):Int = {
    val ref = readUInt32LE(bytes);
    if (ref > 0x7FFFFFFF) ref - 0x10000000 else ref
  }

  def readFloatLE(bytes:Array[Int]): Double = {
    val bits = bytes(3) << 24 | bytes(2) << 16 | bytes(1) << 8 | bytes(0)
    val sign = if (bits >>> 31 == 0) 1.0 else -1.0

    val e = bits >>> 23 & 0xff
    val m = if (e == 0) (bits & 0x7fffff) << 1 else (bits & 0x7fffff) | 0x800000

    sign * m * Math.pow(2, e - 150)
  }

}
