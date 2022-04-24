package de.kp.works.beats.sensor.dl.forecast

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
import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.dl.BeatWorker

/**
 * [ForecastWorker] is responsible for executing the
 * deep learning timeseries forecasting task. This task
 * can be executed either on an adhoc basis (API) or
 * scheduled
 */
class ForecastWorker(queue: SourceQueueWithComplete[String], logger:Logger) extends BeatWorker {

  override def execute(table:String, start:Long, end:Long): Unit = ???

}
