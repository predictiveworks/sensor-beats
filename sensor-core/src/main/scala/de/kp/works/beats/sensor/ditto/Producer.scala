package de.kp.works.beats.sensor.ditto

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

import de.kp.works.beats.sensor._
import org.eclipse.ditto.client.messaging.MessagingProvider
import org.eclipse.ditto.client.twin.TwinThingHandle
import org.eclipse.ditto.client.{DittoClient, DittoClients}
import org.eclipse.ditto.things.model._

abstract class Producer[T <: BeatConf](options:Options[T]) extends BeatSink {
  /**
   * Build Ditto web socket client
   */
  private val dittoClient:Option[DittoClient] = buildDittoClient

  private def buildDittoClient:Option[DittoClient] = {

    try {

      val messagingProvider:MessagingProvider = options.getMessagingProvider
      val client = DittoClients.newInstance(messagingProvider)
        .connect
        .toCompletableFuture
        .get

      Some(client)

    } catch {
      case _:Throwable => None
    }

  }

  override def execute(request: BeatRequest): Unit = {

  }

  private def createSensor(sensor: BeatSensor): Boolean = {

    if (dittoClient.isEmpty) return false

    try {

      val twin = dittoClient.get.twin

      /***** CREATE (STATIC) THING ATTRIBUTES *****/

      val thing = DittoTransform.buildAttributes(sensor, options.getNS)
      twin
        .create(thing)
        .toCompletableFuture
        .get

      /***** UPDATE (DYNAMIC) THING FEATURES *****/

      updateSensor(sensor)

    } catch {
      case t: Throwable =>

        val message = s"Creating Ditto thing failed: ${t.getLocalizedMessage}"
        error(message)

        false
    }

  }

  private def updateSensor(sensor:BeatSensor):Boolean = {

    if (dittoClient.isEmpty) return false

    try {

      val twin = dittoClient.get.twin
      val thingId = ThingId.of(s"${options.getNS}:${sensor.sensorId}")

      /***** UPDATE (DYNAMIC) THING FEATURES *****/

      val thingHandle = twin.forId(thingId)
      updateFeatures(thingHandle, sensor)

      true

    } catch {
      case t:Throwable =>

        val message = s"Updating Ditto thing failed: ${t.getLocalizedMessage}"
        error(message)

        false
    }

  }

  private def updateFeatures(handle:TwinThingHandle, sensor:BeatSensor):Unit = {

    val features = DittoTransform.buildFeatures(sensor.sensorTime, sensor.sensorAttrs)
    handle.setFeatures(features).toCompletableFuture.get

  }

  def error(message:String):Unit

  def info(message:String):Unit

}
