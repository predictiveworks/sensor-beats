package de.kp.works.beats.sensor.milesight

import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.helium.Consumer
import org.eclipse.paho.client.mqttv3.MqttMessage

class MsHelium(options: MsOptions) extends Consumer[MsConf](options.toHelium) with MsLogging {
  override protected def getLogger: Logger = ???

  /**
   * Public method to persist the content of the
   * received MQTT message in the internal RocksDB
   * of the `SensorBeat`.
   */
  override def publish(mqttMessage: MqttMessage): Unit = ???
}
