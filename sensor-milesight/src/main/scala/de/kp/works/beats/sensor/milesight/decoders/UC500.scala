package de.kp.works.beats.sensor.milesight.decoders

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
import de.kp.works.beats.sensor.milesight.enums.MsFields._

import scala.util.control.Breaks.breakable
/**
 * Milesight UC500 is a LoRaWAN node with multiple
 * interfaces to connect to sensors.
 *
 * The field or attribute names are generic and
 * refer to the interfaces.
 *
 * Note, these generic field names should be replaced
 * by more meaningful names e.g. via configuration
 */
object UC500 extends BaseDecoder {

  def decode(bytes:Array[Int]):JsonObject = {

    val decoded = new JsonObject

    var i = -1
    breakable {
      while (i < bytes.length - 1) {
        // CHANNEL
        i += 1
        val channel_id = bytes(i)

        i += 1
        val channel_type = bytes(i)
        // BATTERY
        if (channel_id == 0x01 && channel_type == 0x75) {
          i += 1
          val battery = bytes(i)
          decoded.addProperty("battery", battery)
        }
        // GPIO1: Value = 0 is 'off', and 1 is 'on'
        else if (channel_id == 0x03 && channel_type != 0xC8) {
          i += 1
          val gpio1 = bytes(i)
          decoded.addProperty("gpio1", gpio1)
        }
        // GPIO2: Value = 0 is 'off', and 1 is 'on'
        else if (channel_id == 0x04 && channel_type != 0xC8) {
          i += 1
          val gpio2 = bytes(i)
          decoded.addProperty("gpio2", gpio2)
        }
        // PULSE COUNTER 1
        else if (channel_id == 0x03 && channel_type == 0xc8) {
          i += 1
          val counter1 = readUInt32LE(bytes.slice(i, i + 4))
          decoded.addProperty("counter1", counter1)
          i += 3
        }
        // PULSE COUNTER 2
        else if (channel_id == 0x04 && channel_type == 0xc8) {
          i += 1
          val counter2 = readUInt32LE(bytes.slice(i, i + 4))
          decoded.addProperty("counter2", counter2)
          i += 3
        }
        // ADC 1
        else if (channel_id == 0x05) {
          i += 1

          val cur = readInt16LE(bytes.slice(i, i + 2)).toDouble / 100
          decoded.addProperty("adc1.cur", cur)

          val min = readInt16LE(bytes.slice(i + 2, i + 4)).toDouble / 100
          decoded.addProperty("adc1.min", min)

          val max = readInt16LE(bytes.slice(i + 4, i + 6)).toDouble / 100
          decoded.addProperty("adc1.max", max)

          val avg = readInt16LE(bytes.slice(i + 6, i + 8)).toDouble / 100
          decoded.addProperty("adc1.avg", avg)

          i += 7
        }
        // ADC 2
        else if (channel_id == 0x06) {
          i += 1

          val cur = readInt16LE(bytes.slice(i, i + 2)).toDouble / 100
          decoded.addProperty("adc2.cur", cur)

          val min = readInt16LE(bytes.slice(i + 2, i + 4)).toDouble / 100
          decoded.addProperty("adc2.min", min)

          val max = readInt16LE(bytes.slice(i + 4, i + 6)).toDouble / 100
          decoded.addProperty("adc2.max", max)

          val avg = readInt16LE(bytes.slice(i + 6, i + 8)).toDouble / 100
          decoded.addProperty("adc2.avg", avg)

          i += 7
        }
        // MODBUS
        else if (channel_id == 0xFF && channel_type == 0x0E) {
          // CHANNEL TYPE 0x0E refers to the RS485 interface
          // of the LoRaWAN sensor hub
          //
          // Sample: The RS485 channel identifier 08 refers to
          // channel 2, which is the Modbus master channel
          i += 1
          val modbus_chn_id = bytes(i) - 6
          // DATA TYPE specifies e.g. Float & data length
          //
          // 25 => 00100101
          //
          i += 1
          val package_type = bytes(i)

          val data_type = package_type & 7
          /*
           * The Modbus channel is the generic field name
           * assigned to the respective sensor.
           *
           * Configuration must be used to determine whether
           * this is temperature, dissolved oxygen or others
           */
          val chn = "chn" + modbus_chn_id

          data_type match {
            case 0 =>
              i += 1
              decoded.addProperty(chn, bytes(i))
            case 1 =>
              i += 1
              decoded.addProperty(chn, bytes(i))
            case 2 | 3 =>
              i += 1
              decoded.addProperty(chn, readUInt16LE(bytes.slice(i, i + 2)))
              i += 1
            case 4 | 6 =>
              i += 1
              decoded.addProperty(chn, readUInt32LE(bytes.slice(i, i + 4)))
              i += 3
            case 5 | 7 =>
              i += 1
              decoded.addProperty(chn, readFloatLE(bytes.slice(i, i + 4)))
              i += 3
            case _ =>

          }
        }

      }
    }

    decoded
  }
  /**
   * Note: The current implementation does not
   * reflect the Modbus field names as these are
   * generated dynamically.
   */
  override def fields: Seq[String] = {
    Seq(
      BATTERY,
      GPIO1,
      GPIO2,
      COUNTER1,
      COUNTER2,
      ADC1_AVG,
      ADC1_CUR,
      ADC1_MAX,
      ADC1_MIN,
      ADC2_AVG,
      ADC2_CUR,
      ADC2_MAX,
      ADC2_MIN
    )
  }
}
