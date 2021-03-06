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

import akka.stream.scaladsl.SourceQueueWithComplete
import com.google.gson.JsonObject
/**
 * Actor implementation to publish consumed Things Stack
 * events to the SSE queue.
 */
class BeatSse(queue:SourceQueueWithComplete[String]) extends BeatSink {

  override def execute(request: BeatRequest): Unit = {

    val sensor = request.sensor
    /*
     * The SSE event is harmonized with the Works Beat
     * services
     */
    val eventType = s"sensor/${sensor.sensorBrand.toLowerCase}/${sensor.sensorType.toLowerCase}"

    val json = new JsonObject
    json.addProperty("type", eventType)
    json.addProperty("event", sensor.toJson.toString)

    if (queue != null) queue.offer(json.toString)

  }

}
