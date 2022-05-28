package de.kp.works.beats.sensor.elsys.decoders

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

import com.google.gson.{JsonArray, JsonObject}

/**
 * Common decoder to decode all Elsys sensor payloads
 *
 * Source: https://github.com/TheThingsNetwork/lorawan-devices/blob/master/vendor/elsys/elsys.js
 */
abstract class CommonDecoder extends BaseDecoder {

  val TYPE_TEMP         = 0x01 // temp 2 bytes -3276.8°C --> 3276.7°C
  val TYPE_RH           = 0x02 // Humidity 1 byte  0-100%
  val TYPE_ACC          = 0x03 // acceleration 3 bytes X,Y,Z -128 --> 127 +/-63=1G
  val TYPE_LIGHT        = 0x04 // Light 2 bytes 0-->65535 Lux
  val TYPE_MOTION       = 0x05 // No of motion 1 byte  0-255
  val TYPE_CO2          = 0x06 // Co2 2 bytes 0-65535 ppm
  val TYPE_VDD          = 0x07 // VDD 2byte 0-65535mV
  val TYPE_ANALOG1      = 0x08 // VDD 2byte 0-65535mV
  val TYPE_GPS          = 0x09 // 3bytes lat 3bytes long binary
  val TYPE_PULSE1       = 0x0A // 2bytes relative pulse count
  val TYPE_PULSE1_ABS   = 0x0B // 4bytes no 0->0xFFFFFFFF
  val TYPE_EXT_TEMP1    = 0x0C // 2bytes -3276.5C-->3276.5C
  val TYPE_EXT_DIGITAL  = 0x0D // 1bytes value 1 or 0
  val TYPE_EXT_DISTANCE = 0x0E // 2bytes distance in mm
  val TYPE_ACC_MOTION   = 0x0F // 1byte number of vibration/motion
  val TYPE_IR_TEMP      = 0x10 // 2bytes internal temp 2bytes external temp -3276.5C-->3276.5C
  val TYPE_OCCUPANCY    = 0x11 // 1byte data
  val TYPE_WATERLEAK    = 0x12 // 1byte data 0-255
  val TYPE_GRIDEYE      = 0x13 // 65byte temperature data 1byte ref+64byte external temp
  val TYPE_PRESSURE     = 0x14 // 4byte pressure data (hPa)
  val TYPE_SOUND        = 0x15 // 2byte sound data (peak/avg)
  val TYPE_PULSE2       = 0x16 // 2bytes 0-->0xFFFF
  val TYPE_PULSE2_ABS   = 0x17 // 4bytes no 0->0xFFFFFFFF
  val TYPE_ANALOG2      = 0x18 // 2bytes voltage in mV
  val TYPE_EXT_TEMP2    = 0x19 // 2bytes -3276.5C-->3276.5C
  val TYPE_EXT_DIGITAL2 = 0x1A // 1bytes value 1 or 0
  val TYPE_EXT_ANALOG_UV= 0x1B // 4 bytes signed int (uV)
  val TYPE_TVOC         = 0x1C // 2bytes 0-->65535 ppb
  val TYPE_DEBUG        = 0x3D // 4bytes debug

  override def decode(bytes: Array[Int]): JsonObject = {

    val decoded = new JsonObject
    // 0x01, 0x00, 0xE2, 0x02, 0x29, 0x03, 0x01, 0x27, 0x05, 0x14, 0x06, 0x01, 0x01, 0x11
    var i = 0
    while (i < bytes.length) {

      val byte = bytes(i)
      byte match {
        // Temperature
        case TYPE_TEMP =>
          val binary = (bytes(i+1) <<8) | bytes(i+2)
          val temperature = bin16dec(binary).toDouble / 10

          decoded.addProperty("temperature", temperature)
          i += 3
        // Humidity
        case TYPE_RH =>
          val humidity = bytes(i+1)
          decoded.addProperty("humidity", humidity)
          i+=2

        // Acceleration
        case TYPE_ACC =>
          val x = bin8dec(bytes(i+1))
          decoded.addProperty("x", x)

          val y = bin8dec(bytes(i+2))
          decoded.addProperty("y", y)

          val z = bin8dec(bytes(i+3))
          decoded.addProperty("z", z)

          i+=4

        // Light
        case TYPE_LIGHT =>
          val light = (bytes(i+1) << 8) | bytes(i+2)
          decoded.addProperty("light", light)
          i+=3

        // Motion sensor (PIR)
        case TYPE_MOTION =>
          val motion = bytes(i+1)
          decoded.addProperty("motion", motion)
          i+=2

        // CO2
        case TYPE_CO2 =>
          val co2 = (bytes(i+1) << 8) | bytes(i+2)
          decoded.addProperty("co2", co2)
          i+=3

        // Battery level
        case TYPE_VDD =>
          val vdd = (bytes(i+1) << 8) | bytes(i+2)
          decoded.addProperty("vdd", vdd)
          i+=3

        // Analog input 1
        case TYPE_ANALOG1 =>
          val analog1 = (bytes(i+1) << 8) | bytes(i+2)
          decoded.addProperty("analog1", analog1)
          i+=3

        // LAT & LONG
        case TYPE_GPS =>
          val lat = (bytes(i+1) << 16) | (bytes(i+2) << 8) | bytes(i+3)
          decoded.addProperty("lat", lat)

          val long = (bytes(i+4) << 16) | (bytes(i+5) << 8) | bytes(i+6)
          decoded.addProperty("long", long)

          i+=7

        // Pulse input 1
        case TYPE_PULSE1 =>
          val pulse1 = (bytes(i+1) << 8) | bytes(i+2)
          decoded.addProperty("pulse1", pulse1)
          i+=3

        // Pulse input 1 absolute value
        case TYPE_PULSE1_ABS =>
          val pulseAbs = (bytes(i+1) << 24) | (bytes(i+2) << 16) | (bytes(i+3) << 8) | bytes(i+4)
          decoded.addProperty("pulseAbs", pulseAbs)
          i+=5

        // External temp
        case TYPE_EXT_TEMP1 =>
          val binary = (bytes(i+1) <<8) | bytes(i+2)
          val externalTemperature = bin16dec(binary).toDouble / 10

          decoded.addProperty("externalTemperature", externalTemperature)
          i+=3

        // Digital input
        case TYPE_EXT_DIGITAL =>
          val digital = bytes(i+1)
          decoded.addProperty("digital", digital)
          i+=2

        // Distance sensor input
        case TYPE_EXT_DISTANCE =>
          val distance = (bytes(i+1) << 8) | bytes(i+2)
          decoded.addProperty("distance", distance)
          i+=3

        // Acc motion
        case TYPE_ACC_MOTION =>
          val accMotion = bytes(i+1)
          decoded.addProperty("accMotion", accMotion)
          i+=2

        // IR temperature
        case TYPE_IR_TEMP =>
          val binary1 = (bytes(i+1) << 8) | bytes(i+2)
          val irInternalTemperature = bin16dec(binary1).toDouble / 10
          decoded.addProperty("irInternalTemperature", irInternalTemperature)

          val binary2 = (bytes(i+3) << 8) | bytes(i+4)
          val irExternalTemperature = bin16dec(binary2).toDouble / 10
          decoded.addProperty("irExternalTemperature", irExternalTemperature)

          i+=5

        // Body occupancy
        case TYPE_OCCUPANCY =>
          val occupancy = bytes(i+1)
          decoded.addProperty("occupancy", occupancy)
          i+=2

        // Water leak
        case TYPE_WATERLEAK =>
          val waterleak = bytes(i+1)
          decoded.addProperty("waterleak", waterleak)
          i+=2

        // Grideye data
        case TYPE_GRIDEYE =>
          val ref = bytes(i+1)
          i += 2

          val grideye = new JsonArray
          (0 until 64).foreach(j => {
            val value = ref + (bytes(1+i+j).toDouble / 10.0)
            grideye.add(value)
          })

          decoded.add("grideye", grideye)
          i += 65

        // External Pressure
        case TYPE_PRESSURE =>
          val binary = (bytes(i+1) << 24) | (bytes(i+2) << 16) | (bytes(i+3) << 8) | bytes(i+4)
          val pressure = binary.toDouble / 1000

          decoded.addProperty("pressure", pressure)
          i+=5

        // Sound
        case TYPE_SOUND =>
          val soundPeak = bytes(i+1)
          decoded.addProperty("soundPeak", soundPeak)

          val soundAvg = bytes(i+2)
          decoded.addProperty("soundAvg", soundAvg)

          i+=3

        // Pulse 2
        case TYPE_PULSE2 =>
          val pulse2 = (bytes(i+1) << 8) | bytes(i+2)
          decoded.addProperty("pulse2", pulse2)
          i+=3

        // Pulse input 2 absolute value
        case TYPE_PULSE2_ABS =>
          val pulseAbs2 = (bytes(i+1) << 24) | (bytes(i+2) << 16) | (bytes(i+3) << 8) | bytes(i+4)
          decoded.addProperty("pulseAbs2", pulseAbs2)
          i+=5

        // Analog input 2
        case TYPE_ANALOG2 =>
          val analog2 = (bytes(i+1) << 8) | bytes(i+2)
          decoded.addProperty("analog2", analog2)
          i+=3

        // External temp 2
        case TYPE_EXT_TEMP2 =>
          val binary = (bytes(i+1) << 8) | bytes(i+2)
          val temperature = bin16dec(binary)
          /*
           * The external temperature 2 field finally
           * is decoded as an Array
           */
          if (decoded.has("externalTemperature2")) {

            val field = decoded.get("externalTemperature2")
            if (field.isJsonPrimitive) {
              /* Convert into JSONArray */
              val values = new JsonArray
              values.add(field.getAsDouble)

              decoded.add("externalTemperature2", values)
            }
            else if (field.isJsonArray) {
              val values = field.getAsJsonArray
              values.add(temperature.toDouble / 10)

              decoded.add("externalTemperature2", values)
            }

          }
          else {
            val externalTemperature2 = temperature.toDouble / 10
            decoded.addProperty("externalTemperature2", externalTemperature2)
          }

          i+=3

        // Digital input 2
        case TYPE_EXT_DIGITAL2 =>
          val digital2 = bytes(i+1)
          decoded.addProperty("digital2", digital2)
          i+=2

        // Load cell analog uV
        case TYPE_EXT_ANALOG_UV =>
          val analogUv = (bytes(i+1) << 24) | (bytes(i+2) << 16) | (bytes(i+3) << 8) | bytes(i+4)
          decoded.addProperty("analogUv", analogUv)
          i += 3

        // Total volatile organic compounds ppb
        case TYPE_TVOC =>
          val tvoc = (bytes(i+1) << 8) | bytes(i+2)
          decoded.addProperty("tvoc", tvoc)
          i+=3

        case _ =>
          /* Something is wrong, so stop decoding */
          i = bytes.length
      }
    }

    val result = new JsonObject
    result.add("data", decoded)

    result

  }

}
