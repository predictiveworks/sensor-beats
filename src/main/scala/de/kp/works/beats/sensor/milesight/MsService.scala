package de.kp.works.beats.sensor.milesight

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

import akka.NotUsed
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Source, SourceQueueWithComplete}
import de.kp.works.beats.sensor.{BeatConf, BeatRoute, BeatService}

class MsService extends BeatService {

  override protected var config:BeatConf = MsConf.getInstance
  override protected var serviceName: String = "MsService"

  override def buildRoute(queue: SourceQueueWithComplete[String],
                          source: Source[ServerSentEvent, NotUsed]):Route = {
    /*
     * A common route for all `SensorBeat`s is
     * the SSE based event route
     */
    val eventRoute = new BeatRoute(source).event

    ???
  }


  override def onStart(queue:SourceQueueWithComplete[String]): Unit = ???

}
