package de.kp.works.beats.sensor.publish

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

import akka.stream.scaladsl.SourceQueueWithComplete
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

case class PubEvent(message:String, provider:String, `type`: String, value:Double)

object Publisher {

  private var instance:Option[Publisher] = None

  def getInstance:Publisher = {
    getInstance(null)
  }

  def getInstance(queue:SourceQueueWithComplete[String]):Publisher = {

    if (instance.isEmpty)
      instance = Some(new Publisher(queue))

    instance.get

  }
}

class Publisher(queue:SourceQueueWithComplete[String]) {

  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def pushEvent(message:String, provider:String, `type`: String, progress:Double):Unit = {

    val event = PubEvent(enrichEvent(message), provider, `type`, progress)
    val serialized = mapper.writeValueAsString(event)

    if (queue != null) queue.offer(serialized)

  }

  private def enrichEvent(event:String):String = {
    s"${new java.util.Date().toString} [INFO] $event"
  }

}