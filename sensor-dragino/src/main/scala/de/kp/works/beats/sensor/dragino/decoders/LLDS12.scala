package de.kp.works.beats.sensor.dragino.decoders
import com.google.gson.JsonObject
import de.kp.works.beats.sensor.dragino.enums.DoFields._

object LLDS12 extends BaseDecoder {
  /*
   * The Dragino LLDS12 is a LoRaWAN LiDAR ToF (Time of Flight)
   * Distance Sensor. It is capable to measure the distance to
   * an object as close as 10 centimeters (+/- 5cm up to 6m) and
   * as far as 12 meters (+/-1% starting at 6m).
   *
   * The LiDAR probe uses laser induction technology for distance
   * measurement. The LLDS12 can be applied to scenarios such as
   * horizontal distance measurement, parking management system,
   * object proximity and presence detection, intelligent trash
   * can management system, robot obstacle avoidance, automatic
   * control, sewer, etc.
   */
  override def decode(bytes: Array[Int], fport: Int): JsonObject = {

    val decoded = new JsonObject

    fport match {
      case 2 =>
        if ((bytes(0) != 0x03) && (bytes(10)!=0x02)) {

          // Bat
          val battery = ((bytes(0) << 8 | bytes(1)) & 0x3FFF).toDouble / 1000
          decoded.addProperty("Bat", battery)

          // TempC_DS18B20
          val temperature = {
            val value = (bytes(2) << 24 >> 16)| bytes(3)
            value.toDouble / 10
          }
          decoded.addProperty("TempC_DS18B20", temperature)

          // Lidar_distance
          val distance = (bytes(4) << 8 | bytes(5)).toDouble / 10
          decoded.addProperty("Lidar_distance", distance)

          // Lidar_signal
          val signal = bytes(6) << 8 | bytes(7)
          decoded.addProperty("Lidar_signal", signal)

          // Lidar_temp
          val lidar_temp = bytes(9) << 24 >> 24
          decoded.addProperty("Lidar_temp", lidar_temp)

          // Interrupt_flag
          val flag = bytes(8)
          decoded.addProperty("Interrupt_flag", flag)

          // Message_type
          val mes_type= bytes(10)
          decoded.addProperty("Message_type", mes_type)

        }

      case _ =>
        throw new Exception(s"Unknown `fport` = $fport detected.")

    }

    val result = new JsonObject
    result.add("data", decoded)

    result

  }

  override def fields: Seq[String] = {
    Seq(
      Bat,
      Lidar_distance,
      Lidar_signal,
      Lidar_temp
    )
  }
}
