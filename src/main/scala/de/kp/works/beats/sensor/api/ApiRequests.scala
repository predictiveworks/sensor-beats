package de.kp.works.beats.sensor.api

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

case class AnomalyReq(
  /*
   * An anomaly request distinguishes between
   * training & inference phase
   */
  action:String,
  /*
   * The start time of the time series that is
   * involved in the anomaly request
   */
  startTime:Long = 0L,
  /*
   * The end time of the time series that is
   * involved in the anomaly request
   */
  endTime:Long = 0L
)

case class ForecastReq(
  /*
   * An anomaly request distinguishes between
   * training & inference phase
   */
  action:String,
  /*
   * The start time of the time series that is
   * involved in the forecast request
   */
  startTime:Long = 0L,
  /*
   * The end time of the time series that is
   * involved in the forecast request
   */
  endTime:Long = 0L
)
/**
 * Request format for SQL based read requests
 * for non-inferred sensor information
 */
case class MonitorReq(
  /*
   * The SQL statement that is defined to read
   * data from the sensor's Rocks DB.
   */
  sql:String
)
/**
 * Request format for SQL based trend requests
 * for non-inferred sensor information
 */
case class TrendReq(
  /*
   * The SQL statement that is defined to read
   * data from the sensor's Rocks DB.
   */
  sql:String,
  /*
   * The name of the technical indicator that
   * is used for computing moving average
   */
  indicator:String,
  /*
   * The timeframe used for computing the
   * moving average
   */
  timeframe:Int = 5
)
