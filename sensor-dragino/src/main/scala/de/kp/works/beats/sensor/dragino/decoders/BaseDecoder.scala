package de.kp.works.beats.sensor.dragino.decoders

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

  def decodeHex(hexstring: String, fport: Int): JsonObject = {
    /*
     * HINT: It is important to covert the hex string
     * into an INT Array to ensure proper decoding
     */
    val bytes = hexstring.sliding(2, 2).toArray.map(hex => {
      Integer.parseInt(hex, 16)
    })

    decode(bytes, fport)

  }

  def decode(bytes: Array[Int], fport: Int): JsonObject


  def fields: Seq[String]

  def timestamp2Date(ts:Int):String = {
    /*
     * Check whether the timestamp describes
     * seconds or milliseconds
     */

    ???

  }
//
//  function getzf(c_num){
//    if(parseInt(c_num) < 10)
//      c_num = '0' + c_num;
//
//    return c_num;
//  }
//
//  function getMyDate(str){
//    var c_Date;
//    if(str > 9999999999)
//      c_Date = new Date(parseInt(str));
//    else
//      c_Date = new Date(parseInt(str) * 1000);
//
//    var c_Year = c_Date.getFullYear(),
//    c_Month = c_Date.getMonth()+1,
//    c_Day = c_Date.getDate(),
//    c_Hour = c_Date.getHours(),
//    c_Min = c_Date.getMinutes(),
//    c_Sen = c_Date.getSeconds();
//    var c_Time = c_Year +'-'+ getzf(c_Month) +'-'+ getzf(c_Day) +' '+ getzf(c_Hour) +':'+ getzf(c_Min) +':'+getzf(c_Sen);
//
//    return c_Time;
//  }

}
