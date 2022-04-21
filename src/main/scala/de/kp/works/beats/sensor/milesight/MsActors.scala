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

import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.SourceQueueWithComplete
import ch.qos.logback.classic.Logger
import com.google.gson.{JsonArray, JsonObject}
import de.kp.works.beats.sensor.{BeatConf, BeatMessages}
import de.kp.works.beats.sensor.api.{AnomalyReq, ApiActor, ForecastReq, MonitorReq}
/**
 * The [AnomalyActor] supports the re-training
 * of the SensorBeat's anomaly detection model,
 * and also the provisioning of detected anomalies
 */
class AnomalyActor(queue: SourceQueueWithComplete[String]) extends ApiActor {

  override protected var logger: Logger = MsLogger.getLogger
  override protected var config: BeatConf = MsConf.getInstance

  override def execute(request: HttpRequest): String = {

    val json = getBodyAsJson(request)
    if (json == null) {

      logger.warn(BeatMessages.invalidJson())
      /*
       * The response of this request is a JsonArray;
       * in case of an invalid request, an empty response
       * is returned
       */
      val empty = new JsonArray
      return empty.toString

    }

    val req = mapper.readValue(json.toString, classOf[AnomalyReq])

    ???
  }

}
/**
 * The [ForecastActor] supports the re-training
 * of the SensorBeat's timeseries forecast model,
 * and also the provisioning of forecasted values.
 */
class ForecastActor(queue: SourceQueueWithComplete[String]) extends ApiActor {

  override protected var logger: Logger = MsLogger.getLogger
  override protected var config: BeatConf = MsConf.getInstance

  override def execute(request: HttpRequest): String = {

    val json = getBodyAsJson(request)
    if (json == null) {

      logger.warn(BeatMessages.invalidJson())
      /*
       * The response of this request is a JsonArray;
       * in case of an invalid request, an empty response
       * is returned
       */
      val empty = new JsonArray
      return empty.toString

    }

    val req = mapper.readValue(json.toString, classOf[ForecastReq])

    ???
  }

}
/**
 * The [MonitorActor] supports the provisioning of
 * sensor events based on a SQL statement. This actor
 * is part of the `Sensor as a Table` approach.
 */
class MonitorActor(queue: SourceQueueWithComplete[String]) extends ApiActor {

  override protected var logger: Logger = MsLogger.getLogger
  override protected var config: BeatConf = MsConf.getInstance

  override def execute(request: HttpRequest): String = {

    val json = getBodyAsJson(request)
    if (json == null) {

      logger.warn(BeatMessages.invalidJson())
      /*
       * The response of this request is a JsonArray;
       * in case of an invalid request, an empty response
       * is returned
       */
      val empty = new JsonArray
      return empty.toString

    }

    val req = mapper.readValue(json.toString, classOf[MonitorReq])

    ???
  }

}

