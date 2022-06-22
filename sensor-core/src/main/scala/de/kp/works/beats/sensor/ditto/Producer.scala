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
import org.eclipse.ditto.json.JsonFactory
import org.eclipse.ditto.things.model._

import scala.collection.JavaConverters.asJavaIterableConverter

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

  private def createSensor(sensor:BeatSensor):Boolean = {

    if (dittoClient.isEmpty) return false

    try {

      val twin = dittoClient.get.twin

      /***** CREATE (STATIC) THING ATTRIBUTES *****/

      /*
       * Build `Thing` identifier from configured
       * namespace and provided sensor identifier
       */
      val thingId = ThingId.of(s"${options.getNS}:${sensor.sensorId}")
      /*
       * Leverage the [ThingsModelFactory] to build
       * a new Ditto `thing`
       */
      val thingsBuilder = ThingsModelFactory.newThingBuilder()
      thingsBuilder.setId(thingId)
      /*
       * The parameters sensorBrand, sensorInfo and
       * sensorType are described as attributes
       */
      val sensorBrand = JsonFactory.newPointer("brand")
      thingsBuilder.setAttribute(sensorBrand, JsonFactory.newValue(sensor.sensorBrand))

      val sensorType = JsonFactory.newPointer("type")
      thingsBuilder.setAttribute(sensorType, JsonFactory.newValue(sensor.sensorType))

      val sensorInfo = JsonFactory.newPointer("info")
      thingsBuilder.setAttribute(sensorInfo, JsonFactory.newValue(sensor.sensorInfo.toString))

      twin
        .create(thingsBuilder.build())
        .toCompletableFuture
        .get

      /***** UPDATE (DYNAMIC) THING FEATURES *****/

      updateSensor(sensor)

    } catch {
      case t:Throwable =>

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

    val features = buildFeatures(sensor.sensorTime, sensor.sensorAttrs)
    handle.setFeatures(features).toCompletableFuture.get

  }

  private def buildFeatures(sensorTime:Long, sensorAttrs:Seq[BeatAttr]):Features = {

    val features = sensorAttrs.map(sensorAttr => {
      buildFeature(sensorTime,sensorAttr)
    }).asJava

    ThingsModelFactory.newFeaturesBuilder(features).build

  }

  private def buildFeature(sensorTime:Long, beatAttr:BeatAttr):Feature = {

    val properties = ThingsModelFactory.newFeaturePropertiesBuilder
      .set("createdAt", sensorTime)
      .set("type",      beatAttr.attrType)
      .set("value",     beatAttr.attrValue.doubleValue())
      .build

    ThingsModelFactory.newFeatureBuilder()
      .properties(properties)
      .withId(beatAttr.attrName)
      .build

  }

  def error(message:String):Unit

  def info(message:String):Unit

}
