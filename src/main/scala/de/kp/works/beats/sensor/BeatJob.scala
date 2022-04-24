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

import scala.collection.mutable

case class BeatJob(
  /*
   * The unique identifier of a deep
   * learning job
   */
  id:String,
  /*
   * The timestamp, the deep learning
   * job was started
   */
  createdAt:Long,
  /*
   * The timestamp, the deep learning
   * job was updated.
   */
  updatedAt:Long,
  /*
   * The current status of the deep
   * learning job
   */
  status:String
)
/**
 * This object defines the deep learning job
 * registry of the `SensorBeat` and also starts
 * scheduled anomaly detection and time series
 * forecasting.
 */
object BeatJobs {

  private val registry = mutable.HashMap.empty[String, BeatJob]
  /**
   * Determine whether a certain job defined
   * by its unique id is registered
   */
  def isRegistered(id:String):Boolean =
    registry.contains(id)

  /**
   * Public method to register a certain deep
   * learning job
   */
  def register(job:BeatJob):Unit = {
    registry += job.id -> job
  }

}
