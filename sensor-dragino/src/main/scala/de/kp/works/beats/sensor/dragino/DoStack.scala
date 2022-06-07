package de.kp.works.beats.sensor.dragino

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
import de.kp.works.beats.sensor.dragino.enums.DoProducts.{LDDS04, LDDS20, LDDS45}
import de.kp.works.beats.sensor.thingsstack.Consumer
import org.eclipse.paho.client.mqttv3.MqttMessage

import scala.collection.JavaConversions.asScalaSet

class DoStack(options: DoOptions) extends Consumer[DoConf](options.toStack) with DoLogging {

  private val BRAND_NAME = "Dragino"
  /**
   * The configured data sinks configured to send
   * Dragino sensor readings to
   */
  private val sinks = options.getSinks

  override protected def getLogger: Logger = logger

  override protected def publish(mqttMessage: MqttMessage): Unit = {

    try {

      val (deviceId, sensorReadings) = unpack(mqttMessage)
      /*
        * Convert decoded sensors that refer to textual values
        */
      val product = options.getProduct
      product match {
        case LDDS04 =>
          try {
            // EXTI_Trigger
            {
              val key = "EXTI_Trigger"
              val value = sensorReadings.remove(key).getAsString
              sensorReadings.addProperty(key, if (value.toLowerCase == "true") 1D else 0D)
            }

          } catch {
            case _: Throwable => /* Do nothing */
          }

        case LDDS20 | LDDS45 =>
          try {
            // Distance
            {
              val key = "Distance"
              val value = sensorReadings.remove(key).getAsString

              val distance =
                if (value.toLowerCase == "no sensor") -1
                else if (value.toLowerCase == "invalid reading") -2
                else
                  value.replace("mm", "").trim.toInt

              sensorReadings.addProperty(key, distance)

            }

          } catch {
            case _: Throwable => /* Do nothing */
          }

        case _ => /* Do nothing */

      }
      /*
       * Apply field mappings and replace those decoded field
       * names by their aliases that are specified on the
       * provided mappings
       */
      val mappings = options.getMappings
      if (mappings.nonEmpty) {
        val fields = sensorReadings.keySet()
        fields.foreach(name => {
          if (mappings.contains(name)) {
            val alias = mappings(name)
            val property = sensorReadings.remove(name)

            sensorReadings.addProperty(alias, property.getAsDouble)
          }
        })
      }
      /*
       * STEP #4: Send sensor readings (payload) to the
       * configured data sinks; note, attributes are
       * restricted to [Number] fields.
       */
      send2Sinks(deviceId, BRAND_NAME, product.toString, sensorReadings, sinks)

    } catch {
      case t: Throwable =>
        val message = s"Publishing Things Stack $BRAND_NAME event failed: ${t.getLocalizedMessage}"
        getLogger.error(message)
    }
  }

}