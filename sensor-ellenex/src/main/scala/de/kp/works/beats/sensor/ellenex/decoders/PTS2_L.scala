package de.kp.works.beats.sensor.ellenex.decoders

import com.google.gson.JsonObject

/**
 * Payload Decoder for Ellenex PTS2_L:
 *
 * LoRaWAN low power standard pressure transmitter
 * for liquid and gas media compatible with stainless
 * steel (water, air, diesel, oil)
 *
 * https://github.com/TheThingsNetwork/lorawan-devices/tree/master/vendor/ellenex
 */

object PTS2_L extends BaseDecoder {

  override def decode(bytes: Array[Int], fport:Int): JsonObject = {

    if (fport == 1) {
      /*
       * Throw an error if length of Bytes is not 8
       */
      if (bytes.length != 8) {
        throw new Exception(s"Invalid uplink payload: length is not 8 bytes.")
      }
      val pressure = readHex2bytes(bytes(3), bytes(4)).toDouble
      val battery = bytes(7).toDouble / 10

      val decoded = new JsonObject
      // BATTERY = VOLTAGE
      decoded.addProperty("battery", battery)
      // PRESSURE = BAR
      decoded.addProperty("pressure", pressure)

      decoded

    }
    else
      throw new Exception(s"Please use `fport` = 1")

  }

}
