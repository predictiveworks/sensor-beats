package de.kp.works.beats.sensor.milesight

import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.loriot.{Consumer, LoriotUplink}

class MsLoriot(options: MsOptions) extends Consumer[MsConf](options.toLoriot) with MsLogging {

  override protected def getLogger: Logger = ???

  override def publish(message: LoriotUplink): Unit = ???

}
