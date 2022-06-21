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

  def fields:Seq[String]

  def getCmd(cmdID:Int):String ={

    cmdID match {
      case 1   => "ConfigReportReq"
      case 2   => "ReadConfigReportReq"
      case 3   => "SetIRDisableTimeReq"
      case 4   => "GetIRDisableTimeReq"
      case 129 => "ConfigReportRsp"
      case 130 => "ReadConfigReportRsp"
      case 131 => "SetIRDisableTimeRsp"
      case 132 => "GetIRDisableTimeRsp"
      case _ =>
        throw new Exception(s"Command cannot be determined.")
    }

  }

  def getCmdID(cmdType:String):Int = {

    cmdType match {
      case "ConfigReportReq"      => 1
      case "ReadConfigReportReq"  => 2
      case "SetIRDisableTimeReq"  => 3
      case "GetIRDisableTimeReq"  => 4
      case "ConfigReportRsp"      => 129
      case "ReadConfigReportRsp"  => 130
      case "SetIRDisableTimeRsp"  => 131
      case "GetIRDisableTimeRsp"  => 132
      case _ =>
        throw new Exception(s"Command identifier cannot be determined.")
    }

  }

  def getDeviceName(dev:Int):String = {

    dev match {
      case 1   => "R711(R712)"
      case 3   => "RB11E"
      case 11  => "R718A"
      case 19  => "R718AB"
      case 110 => "R720A"
      case _ => ""
    }

  }

  def getDeviceType(name:String):Int = {
    name match {
      case "R711(R712)" => 1
      case "RB11E"      => 3
      case "R718A"      => 11
      case "R718AB"     => 19
      case "R720A"      => 110
      case _ => Integer.MIN_VALUE
    }
  }

  def padLeft(str:String, len:Int):String = {

    if (str.length >= len) {
      str

    } else {
      padLeft("0" + str, len)

    }

  }
}
