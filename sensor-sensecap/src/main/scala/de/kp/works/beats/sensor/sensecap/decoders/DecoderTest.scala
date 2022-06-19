package de.kp.works.beats.sensor.sensecap.decoders

import com.google.gson.JsonObject

object DecoderTest {

  def main(args:Array[String]):Unit = {

    val remove_sensor = Array(
      1, 1, 0, 1, 1, 0, 1, 1, 2, 0, 106, 1, 0, 21, 1, 3, 0, 48, 241, 247, 44, 1, 4, 0, 9, 12, 19, 20, 1, 5, 0, 127, 77, 0, 0, 1, 6, 0, 0, 0, 0, 0, 76, 190
    )

    val decoder = new CommonDecoder {
      override def fields: Seq[String] = ???
    }

    println(decoder.decode(remove_sensor))

  }

}
