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
 * Plug and play advanced air quality monitoring station,
 * enclosed in an aluminium body for rugged design, it has
 * sensors for Particulate Matter (PM2.5, PM1, PM10), Ozone,
 * Formaldehyde, Carbon Dioxide, Volatile Organic Compounds (VOC),
 * temperature, barometric pressure, air humidity and noise.
 */
object A3 extends BaseDecoder {
  /**
   * Advanced Air Quality Monitoring
   *
   * Source: https://github.com/TheThingsNetwork/lorawan-devices/tree/master/vendor/uradmonitor
   */
  override def decode(bytes: Array[Int]): JsonObject = {

    if (bytes.length != 32) return null
    /*
     * This decoding implementation ignores
     * hardware & firmware versions, and model
     */
    val decoded = new JsonObject

    // ° Celsius
    val temperature = uint16double(bytes(10), bytes(11), 100)
    decoded.addProperty("temperature", temperature)

    // Pa
    val pressure = uint16(bytes(12), bytes(13)) + 65535
    decoded.addProperty("pressure", pressure)

    // RH (%)
    val humidity = bytes(14).toDouble / 2
    decoded.addProperty("humidity", humidity)

    // VOC (OHM)
    val gas_resistance = uint24(bytes(15), bytes(16), bytes(17))
    decoded.addProperty("gas_resistance", gas_resistance)

    // dBA
    val sound = bytes(18).toDouble / 2
    decoded.addProperty("sound", sound)

    // PPM
    val co2 = uint16(bytes(19), bytes(20))
    decoded.addProperty("co2", co2)

    // Formaldehyde CH20 (PPB)
    val ch20 = uint16(bytes(21), bytes(22))
    decoded.addProperty("ch20", ch20)

    // PPB
    val o3 = uint16(bytes(23), bytes(24))
    decoded.addProperty("o3", o3)

    // µg/m3
    val pm1 = uint16(bytes(25), bytes(26))
    decoded.addProperty("pm1", pm1)

    // µg/m3
    val pm2_5 = uint16(bytes(27), bytes(28))
    decoded.addProperty("pm2_5", pm2_5)

    // µg/m3
    val pm10 = uint16(bytes(29), bytes(30))
    decoded.addProperty("pm10", pm10)

    val iaq = Math.round(Math.log(gas_resistance) + 0.04 * humidity)
    decoded.addProperty("iaq", iaq)

    val result = new JsonObject
    result.add("data", decoded)

    result

  }

  override def fields: Seq[String] = {
    Seq(
      TEMPERATURE,
      PRESSURE,
      HUMIDITY,
      GAS_RESISTANCE,
      SOUND,
      CO2,
      CH20,
      O3,
      PM1,
      PM2_5,
      PM10,
      IAQ)
  }

}
