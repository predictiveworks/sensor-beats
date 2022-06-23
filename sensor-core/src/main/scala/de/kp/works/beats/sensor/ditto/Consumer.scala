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

import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.{BeatConf, BeatSensor, BeatSource}
import org.eclipse.ditto.client.changes.ThingChange
import org.eclipse.ditto.client.messaging.MessagingProvider
import org.eclipse.ditto.client.{DittoClient, DittoClients}

abstract class Consumer[T <: BeatConf](options:Options[T]) extends BeatSource {
  /**
   * Build Ditto web socket client
   */
  private val dittoClient:Option[DittoClient] = buildDittoClient
  protected def getLogger:Logger
  /**
   * Public method to subscribe to the Web Socket
   * server specified (like Ditto).
   */
  def subscribeAndPublish():Unit = {
    /*
     * Subscriptions are registered in method
     * `connect` and subscription consumers
     * publish events and messages
     */
    if (dittoClient.nonEmpty) connect()

  }

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

  def connect(): Unit = {

    if (dittoClient.isEmpty) return
    /*
     * This Ditto web socket client subscribes to twin events:
     *
     * - PROTOCOL_CMD_START_SEND_EVENTS   :: "START-SEND-EVENTS"
     *
     * Subscription to events is based on Ditto's twin implementation
     * and refers to the TwinImpl.CONSUME_TWIN_EVENTS_HANDLER which
     * is initiated in the twin's doStartConsumption method
     *
     */
    registerForTwinEvents()
    dittoClient.get.twin.startConsumption.toCompletableFuture.get

  }

  def disconnect(): Unit = {

    if (dittoClient.isEmpty) return

    val twin = dittoClient.get.twin
    twin.suspendConsumption

    if (options.getThingChanges)
      twin.deregister("DITTO_THING_CHANGES")

    dittoClient.get.destroy()

  }

  /**
   * SensorBeats are represented as Ditto `Things`,
   * and this method subscribes to thing changes
   */
  private def registerForTwinEvents() {

    if (!options.getThingChanges) return

    val twin = dittoClient.get.twin
    /*
     * A single consumer is used for all
     * thing change events
     */
    val consumer = new java.util.function.Consumer[ThingChange] {
      override def accept(change:ThingChange):Unit = {

        val sensor = DittoTransform.thing2Sensor(change, options.getNS)
        if (sensor.nonEmpty) publish(sensor.get)

      }
    }

    val handler = "DITTO_THING_CHANGES"

    val thingIds = options.getThingIds
    if (thingIds.isEmpty) {
      twin.registerForThingChanges(handler, consumer)

    } else {
      thingIds.foreach(thingId => {
        twin.forId(thingId).registerForThingChanges(handler, consumer)
      })

    }

  }

  protected def publish(sensor:BeatSensor):Unit

}
