package de.kp.works.beats.sensor

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
/**
 * The number of input channels that can be configured
 * to consume sensor events.
 */

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.gson.JsonObject
import de.kp.works.beats.sensor.BeatOutputs.BeatOutput

import scala.collection.JavaConversions.asScalaSet

object BeatInputs extends Enumeration {
  type BeatInput = Value

  val HELIUM: BeatInputs.Value       = Value(1, "HELIUM")
  val LORIOT: BeatInputs.Value       = Value(2, "LORIOT")
  val THINGS_STACK: BeatInputs.Value = Value(3, "THINGS_STACK")

}

trait BeatSource {

  protected val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def subscribeAndPublish():Unit

  protected def send2Sinks(deviceId:String, sensorBrand:String, sensorType:String, readings:JsonObject, sinks:Seq[BeatOutput]):Unit = {

    val sensorAttrs = readings.entrySet().map(entry => {
      /*
       * The sensor readings extracted for sending
       * to the Sensor Beat's output channel are
       * restricted to [Number] attributes
       */
      val attrName = entry.getKey
      val attrType = "Number"
      val attrValue = entry.getValue.getAsNumber

      BeatAttr(attrName, attrType, attrValue)

    }).toSeq
    /*
     * Build sensor specification for output channel processing.
     * For use cases, where various `SensorBeat`s are used, the
     * sensor type and also the brand name are published to distinguish
     * different beats.
     */
    val sensor = BeatSensor(
      sensorId = deviceId,
      sensorType = sensorType,
      sensorBrand = sensorBrand,
      sensorInfo  = BeatInfos.MONITOR,
      sensorTime = System.currentTimeMillis,
      sensorAttrs = sensorAttrs)

    val request = BeatRequest(action = BeatActions.WRITE, sensor = sensor)
    /*
     * Build sensor beat and send to configured output channels
     * for further processing; note, the MsRocks channel is always
     * defined.
     */
    sinks.foreach(sink => {
      /*
       * Note, a `BeatChannel` is implemented as an Akka actor
       */
      val beatChannel = BeatSinks.getChannel(sink)
      if (beatChannel.nonEmpty) beatChannel.get ! request

    })

  }

}
