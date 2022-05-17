package de.kp.works.beats.sensor.ellenex

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

import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.helium.{Consumer, HeliumUplink}
import org.eclipse.paho.client.mqttv3.MqttMessage

class ExHelium(options: ExOptions) extends Consumer[ExConf](options.toHelium) with ExLogging {

  private val BRAND_NAME = "Milesight"
  override protected def getLogger: Logger = logger

  /**
   * Public method to persist the content of the
   * received Helium uplink message in the internal
   * RocksDB of the `SensorBeat`.
   */
  override def publish(uplinkMessage: HeliumUplink): Unit = {

    try {

      // TODO

    } catch {
      case t: Throwable =>
        val message = s"Publishing Milesight event failed: ${t.getLocalizedMessage}"
        getLogger.error(message)
    }
  }

}
