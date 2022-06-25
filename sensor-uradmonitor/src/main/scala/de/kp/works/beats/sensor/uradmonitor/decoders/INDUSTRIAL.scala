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
import de.kp.works.beats.sensor.uradmonitor.enums.UmFields._

/**
 * An automated, fixed monitoring station that tracks a total of
 * 11 important air quality parameters including Particulate Matter,
 * Carbon Monoxide, Ozone, Sulphur Dioxide, Nitrogen Dioxide and more
 * for the INDUSTRIAL sector.
 */
object INDUSTRIAL extends BaseDecoder {

  /**
   * Source: https://github.com/TheThingsNetwork/lorawan-devices/tree/master/vendor/uradmonitor
   */
  override def decode(bytes: Array[Int]): JsonObject = {

    if (bytes.length != 50) return null
    /*
     * This decoding implementation ignores
     * hardware & firmware versions, and model
     */
    val decoded = new JsonObject

    // ° Celsius
    val temperature = uint16double(bytes(20), bytes(21), 100)
    decoded.addProperty("temperature", temperature)

    // Pa
    val pressure = uint16(bytes(22), bytes(23)) + 65535
    decoded.addProperty("pressure", pressure)

    // RH (%)
    val humidity = bytes(24).toDouble / 2
    decoded.addProperty("humidity", humidity)

    // VOC (OHM)
    val gas_resistance = uint24(bytes(25), bytes(26), bytes(27))
    decoded.addProperty("gas_resistance", gas_resistance)

    // dBA
    val sound = bytes(28).toDouble / 2
    decoded.addProperty("sound", sound)

    /* Organic sensors are optional, and different
     * sensors support different value combinations
     */

    var tuple = decodeOrganicSensor(bytes(29), bytes(30), bytes(31), bytes(32))
    if (tuple._1 != null)
      decoded.addProperty(tuple._1, tuple._2)

    tuple = decodeOrganicSensor(bytes(33), bytes(34), bytes(35), bytes(36))
    if (tuple._1 != null)
      decoded.addProperty(tuple._1, tuple._2)

    tuple = decodeOrganicSensor(bytes(37), bytes(38), bytes(39), bytes(40))
    if (tuple._1 != null)
      decoded.addProperty(tuple._1, tuple._2)

    tuple = decodeOrganicSensor(bytes(41), bytes(42), bytes(43), bytes(44))
    if (tuple._1 != null)
      decoded.addProperty(tuple._1, tuple._2)

    // µg/m3 : decoded as difference from PM2.5 (-)
    var pm1 = bytes(45)

    // µg/m3 : decoded as difference from PM2.5 (+)
    val pm2_5 = uint16(bytes(46), bytes(47))

    // µg/m3
    var pm10 = bytes(48)

    /* Corrections */
    pm1  = pm2_5 - pm1
    pm10 = pm2_5 + pm10

    decoded.addProperty("pm1", pm1)
    decoded.addProperty("pm2_5", pm2_5)

    decoded.addProperty("pm10", pm10)

    val iaq = Math.floor(Math.log(gas_resistance) + 0.04 * humidity)
    decoded.addProperty("iaq", iaq)

    val result = new JsonObject
    result.add("data", decoded)

    result

  }

  private def decodeOrganicSensor(`type`:Int, value1:Int, value2:Int, value3:Int):(String, Double) = {

    val name = `type` match {
      case 0x2A => "o3"
      case 0x2B => "so2"
      case 0x2C => "no2"
      case 0x04 => "co"
      case 0x03 => "h2s"
      case 0x02 => "nh3"
      case 0x31 => "cl2"
      case _ => null

    }

    if (name == null) return (name, Double.NaN)

    val value = uint24double(value1, value2, value3, 1000)
    (name, value)

  }

}
