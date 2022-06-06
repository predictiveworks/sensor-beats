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
import scala.util.control.Breaks.{break, breakable}

/**
 * Payload Decoder for Milesight VS121
 *
 * TODO The region count decoding is from Milesight
 * VS121 decoding, but the expected result differs
 * from the computed one.
 */
object VS121 extends BaseDecoder {
  /*
   *
   * VS121 is a real-time AI-based occupancy sensor
   * to better understand the people using and working
   * within spaces.
   *
   * The VS121 supports up to 12 custom regions, and
   * determine the detection side of these regions.
   *
   * --------------------- Payload Definition ---------------------
   *
   *                         [channel_id]  [channel_type] [channel_value]
   * FF: protocol_version  -> 0xFF           0x01          [1byte  ] Unit:
   * FF: serial_number     -> 0xFF           0x08          [6bytes ] Unit:
   * FF: hardware_version  -> 0xFF           0x09          [2bytes ] Unit:
   * FF: firmware_version  -> 0xFF           0x0A          [4bytes ] Unit:
   *
   * 04: counter           -> 0x04           0xC9          [4bytes ] Unit:
   * 05: passing           -> 0x05           0xCC          [2bytes ] Unit:
   * 06: max               -> 0x06           0xCD          [1byte  ] Unit:
   * -------------------------------------------------------- VS121
   */
  override def decode(bytes: Array[Int]): JsonObject = {

    val decoded = new JsonObject

    var i = -1
    breakable {
      while (i < bytes.length - 1) {

        // CHANNEL
        i += 1
        val channel_id = bytes(i)
//     val hexstring = "ff 01 01 // ff 08 66 00 b0 94 09 76 // ff 09 01 00 // ff 0a 1f 07 00 4b // 04 c9 03 05 || 00 a1".replace(" ","")
        i += 1
        val channel_type = bytes(i)

        // PROTOCOL VERSION
        if (channel_id == 0xff && channel_type == 0x01) {
          i += 1
          val version = bytes(i)
          decoded.addProperty("protocol_version",  version)
        }

        // SERIAL NUMBER
        else if (channel_id == 0xff && channel_type == 0x08) {
          i += 1
          val serial_number = readString(bytes.slice(i, i + 6))
          decoded.addProperty("sn",  serial_number)
          i += 5
        }

        // HARDWARE VERSION
        else if (channel_id == 0xff && channel_type == 0x09) {
          i += 1
          val hardware_version = readVersion(bytes.slice(i, i + 2))
          decoded.addProperty("hardware_version",  hardware_version)
          i += 1
        }

        // FIRMWARE VERSION
        else if (channel_id == 0xff && channel_type == 0x0a) {
          i += 1
          val firmware_version = readVersion(bytes.slice(i, i + 4))
          decoded.addProperty("firmware_version",  firmware_version)
          i += 3
        }

        // PEOPLE COUNTER
        else if (channel_id == 0x04 && channel_type == 0xc9) {
          i += 1
          val people_counter_all = bytes(i)
          decoded.addProperty("people_counter_all", people_counter_all)

          val region_count = bytes(i + 1)
          decoded.addProperty("region_count", region_count)
          /*
           * Describe region specific information:
           * 0 = no people detected in this region
           * 1 = there are people in this region
           */
          var region = readUInt16BE(bytes.slice(i + 2, i + 4))
          (0 until region_count).foreach(r => {
            val k = "region_" + r
            // TODO Review decoding
            println((region >> r).toBinaryString)
            val v = (region >> r) & 1
            decoded.addProperty(k,  v)
          })
          i += 3
        }

        // PEOPLE IN/OUT
        else if (channel_id == 0x05 && channel_type ==  0xcc) {
          i += 1
          val in = readInt16LE(bytes.slice(i, i + 2))
          decoded.addProperty("in",  in)

          val out = readInt16LE(bytes.slice(i + 2, i + 4))
          decoded.addProperty("out",  out)

          i += 3
        }

        // PEOPLE MAX
        else if (channel_id == 0x06 && channel_type ==  0xcd) {
          i += 1
          val people_max = bytes(i)
          decoded.addProperty("people_max",  people_max)

        } else {
          break
        }

      }
    }
    /*
     * Remove those fields that do not refer
     * to measurements
     */
    val excluded = Seq(
      "firmware_version",
      "hardware_version",
      "protocol_version",
      "sn"
    )
    /*
    excluded.foreach(exclude =>
      if (decoded.has(exclude)) decoded.remove(exclude)
    )
    */
    decoded

  }
  /*
   * The tables must be determined dynamically
   */
  override def fields: Seq[String] = Seq.empty[String]
}
