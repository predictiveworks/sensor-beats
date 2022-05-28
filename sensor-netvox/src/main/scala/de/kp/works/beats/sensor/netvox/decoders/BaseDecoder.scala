package de.kp.works.beats.sensor.netvox.decoders

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

/*
 * https://github.com/TheThingsNetwork/lorawan-devices/tree/master/vendor/netvox/payload
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

}
