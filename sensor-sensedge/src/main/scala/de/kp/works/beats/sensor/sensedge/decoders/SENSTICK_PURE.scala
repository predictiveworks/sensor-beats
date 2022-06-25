package de.kp.works.beats.sensor.sensedge.decoders

import com.google.gson.JsonObject
import de.kp.works.beats.sensor.sensedge.enums.SeFields._

object SENSTICK_PURE extends BaseDecoder {
  /*
   * LoRaWAN indoor air quality sensor
   */
  override def decode(bytes: Array[Int], fport: Int): JsonObject = {

    val decoded = new JsonObject

    fport match {
      case 2 =>

        // Status
        val status = bytes(0)
        decoded.addProperty("Status", status)

        // Temperature
        val temperature = int2Double((bytes(1) << 8) + bytes(2))
        decoded.addProperty("Temperature", temperature)

        // Humidity
        val humidity = ((bytes(3) << 8) + bytes(4)).toDouble / 100
        decoded.addProperty("Humidity", humidity)

        // AirPressure
        val pressure = bytes(5) + 845
        decoded.addProperty("AirPressure", pressure)

        // IAQ
        val iaq = (bytes(6) << 8) | bytes(7)
        decoded.addProperty("IAQ", iaq)

        // StaticIAQ
        val staticIAQ = (bytes(8) << 8) | bytes(9)
        decoded.addProperty("StaticIAQ", staticIAQ)

        // eCO2
        val co2 = (bytes(10) << 8) | bytes(11)
        decoded.addProperty("eCO2", co2)

        // BreathVOC
        val voc = bytes(12).toDouble / 10
        decoded.addProperty("BreathVOC", voc)

        // IAQAccuracy
        val accuracy = bytes(13)
        decoded.addProperty("IAQAccuracy", accuracy)

      case _ =>
        throw new Exception(s"Unknown `fport` = $fport detected.")

    }

    val result = new JsonObject
    result.add("data", decoded)

    result

  }

}