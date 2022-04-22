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

import akka.stream.scaladsl.SourceQueueWithComplete
import ch.qos.logback.classic.Logger

class MsInsight(queue: SourceQueueWithComplete[String], logger:Logger) {

  def computeAnomalies(startTime:Long = 0L, endTime:Long = 0L):String = {
    ???
  }
  def readAnomalies(startTime:Long = 0L, endTime:Long = 0L):String = {
    ???
  }

  def computeForecasts(startTime:Long = 0L, endTime:Long = 0L):String = {
    ???
  }
  def readForecasts(startTime:Long = 0L, endTime:Long = 0L):String = {
    ???
  }
}
