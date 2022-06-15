package de.kp.works.beats.sensor.weather.api

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

import akka.actor.Actor
import akka.stream.scaladsl.SourceQueueWithComplete
import com.google.gson.JsonObject
import de.kp.works.beats.sensor.weather.WeLogging
import de.kp.works.beats.sensor.weather.pvlib.{PVlibHandler, PVlibJob, PVlibUtil}

abstract class SolarWorker(
  queue:SourceQueueWithComplete[String]) extends Actor with WeLogging {

  def buildHandler():PVlibHandler = {

    val handler = new PVlibHandler {
      override def doComplete(output: Seq[String]): Unit = {

        if (output.nonEmpty) {

          val transformed = PVlibUtil.result2Json(output.head)
          val event = new JsonObject

          event.addProperty("id", this.getJid.getOrElse(""))
          event.addProperty("type",  s"urn:works:pvlib:${this.getMethod.getOrElse("not-defined")}")

          event.add("data", transformed)
          queue.offer(event.toString)

        }

      }
    }

    handler

  }

  def publishJob(job:PVlibJob):Unit = {

    val event = new JsonObject

    event.addProperty("id", job.jid)
    event.addProperty("type",  s"urn:works:pvlib:status")

    event.add("data", job.toJson)
    queue.offer(event.toString)

  }

}
