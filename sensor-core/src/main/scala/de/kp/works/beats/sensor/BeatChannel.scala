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

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.collection.mutable
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
/**
 * The number of channels that can be configured to
 * publish consumed sensor events.
 */
object Channels extends Enumeration {
  type Channel = Value

  val FIWARE: Channels.Value      = Value(1, "FIWARE")
  val ROCKS_DB: Channels.Value    = Value(2, "ROCKS_DB")
  val SSE: Channels.Value         = Value(3, "SSE")
  val THINGSBOARD: Channels.Value = Value(4, "THINGSBOARD")
}

trait BeatChannel extends Actor {

  override def receive: Receive = {
    case request:BeatRequest =>
      execute(request)

    case _ =>
      throw new Exception(s"A `BeatChannel` supports sensor messages only")
  }

  def execute(request:BeatRequest):Unit

}
/**
 * This object defines the registry of configured
 * output channels for SensorBeat implementations
 */
object BeatChannels {

  private val uuid = java.util.UUID.randomUUID().toString
  /**
   * Akka 2.6 provides a default materializer out of the box, i.e., for Scala
   * an implicit materializer is provided if there is an implicit ActorSystem
   * available. This avoids leaking materializers and simplifies most stream
   * use cases somewhat.
   */
  implicit val system: ActorSystem = ActorSystem(s"sensor-beat-channels-$uuid")
  implicit lazy val context: ExecutionContextExecutor = system.dispatcher

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  /**
   * Common timeout for all Akka connection
   */
  implicit val timeout: Timeout = Timeout(15.seconds)

  private val registeredChannels = mutable.HashMap.empty[Channels.Value, ActorRef]

  def registerChannel(channel:Channels.Value, channelProps:Props):Unit = {

    val channelActor = system.actorOf(channelProps, channel.toString)
    registeredChannels += channel -> channelActor

  }

  def getChannel(channel:Channels.Value):Option[ActorRef] =
    registeredChannels.get(channel)

  def getChannels:Seq[ActorRef] =
    registeredChannels.values.toSeq

}