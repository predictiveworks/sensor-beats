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

object WSC1_L extends BaseDecoder {
  /*
   * WSC1-L is the main process unit in Dragino
   * Weather Station solutions. WSCL-1 is an outdoor
   * LoRaWAN RS485 end node.
   *
   * It is powered by external 12v solar power and
   * has a built-in li-on backup battery.
   *
   * WSC1-L reads values from various sensors and upload
   * them via LoRaWAN wireless protocol.
   */

  private val algorithm = Array(
    0x03,0x01,0x01,0x11,
    0x20,0x20,0x01,0x01,
    0x01,0x01,0x20,0x20,
    0x20,0x01)

  private val direction = Map(
    0 -> "N",
    1 -> "NNE",
    2 -> "NE",
    3 -> "ENE",
    4 -> "E",
    5 -> "ESE",
    6 -> "SE",
    7 -> "SSE",
    8 -> "S",
    9 -> "SSW",
    10 -> "SW",
    11 -> "WSW",
    12 -> "W",
    13 -> "WNW",
    14 -> "NW",
    15 -> "NNW")

  /* The field names of the supported RS485 sensors */
  private val sensor = Array(
    "bat",
    "wind_speed",
    "wind_direction_angle",
    "illumination",
    "rain_snow",
    "CO2",
    "TEM",
    "HUM",
    "pressure",
    "rain_gauge",
    "PM2_5",
    "PM10",
    "PAR",
    "TSR")

  private val sensor_diy = Array("A1","A2","A3","A4")

  override def decode(bytes: Array[Int], fport: Int): JsonObject = {

    val decoded = new JsonObject

    fport match {
      case 2 =>

        if (bytes(0) < 0xE0) {

          var i = 0
          while (i < bytes.length) {

            val len = bytes(i+1)
            if (bytes(i) < 0xA1) {

              val sensor_type= bytes(i)

              val count = algorithm(sensor_type) & 0x0F
              val operation = algorithm(sensor_type) >> 4

              if (operation == 0) {
                if (sensor_type == 0x06)	{
                  if ((bytes(i+2) & 0x80) == 1) {
                    val value = (((bytes(i+2) << 8) | bytes(i+3)) - 0xFFFF).toDouble / (count*10.0)
                    val name = sensor(sensor_type)
                    decoded.addProperty(name, value)
                  }
                  else {
                    val value = ((bytes(i+2) << 8) | bytes(i+3)).toDouble / (count*10.0)
                    val name = sensor(sensor_type)
                    decoded.addProperty(name, value)
                  }
                }
                else {
                  val value = ((bytes(i+2) << 8) | bytes(i+3)).toDouble / (count*10.0)
                  val name = sensor(sensor_type)
                  decoded.addProperty(name, value)
                }
              }
              else if (operation ==1) {
                val value = ((bytes(i+2) <<8 ) | bytes(i+3)) * (count*10)
                val name = sensor(sensor_type)
                decoded.addProperty(name, value)
              }
              else {
                // RAIN_SNOW
                if(sensor_type == 0x04) {
                  val value = bytes(i+2)
                  val name = sensor(sensor_type)
                  decoded.addProperty(name, value)
                }
                else {
                  val value = (bytes(i+2) << 8) | bytes(i+3)
                  val name = sensor(sensor_type)
                  decoded.addProperty(name, value)
                }
              }

              if (sensor_type == 0x01) {
                val value = bytes(i+4)
                val name = "wind_speed_level"
                decoded.addProperty(name, value)
              }
              else if (sensor_type == 0x02) {
                val value = direction(bytes(i+4))
                val name = "wind_direction"
                decoded.addProperty(name, value)
              }
            }
            else {
              val value = (bytes(i+2) << 8) | bytes(i+3)
              val name = sensor_diy(bytes(i) - 0xA1)
              decoded.addProperty(name, value)
            }

            i= i + 2 + len

          }

        }

      case _ =>
        throw new Exception(s"Unknown `fport` = $fport detected.")

    }

    val result = new JsonObject
    result.add("data", decoded)

    result
  }
  /**
   * The WSC1-L is a sensor hub for multiple
   * RS485 sensors and is made to build the
   * platform of a weather station.
   *
   * This implies that the provided measurements
   * are dynamic and cannot be specified as part
   * of the decoder
   */
  override def fields: Seq[String] = {
    Seq()
  }
}
