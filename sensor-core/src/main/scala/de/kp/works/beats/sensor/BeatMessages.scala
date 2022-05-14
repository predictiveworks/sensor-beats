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

object BeatMessages {

  def anomalyStarted():String =
    "Anomaly detection started."

  def anomalyFailed(t:Throwable):String =
     s"Anomaly request failed: ${t.getLocalizedMessage}"

  def deepFailed(t:Throwable):String =
    s"Deep learning task failed: ${t.getLocalizedMessage}"

  def emptyIndicator():String =
    s"Request contains no technical indicator."

  def emptyJob():String =
    s"Request contains no deep learning job identifier."

  def emptySql():String =
    s"Request contains empty SQL query."

  def forecastStarted():String =
    "Forecast computing started."

  def forecastFailed(t:Throwable):String =
    s"Forecast request failed: ${t.getLocalizedMessage}"

  def insightFailed(t:Throwable):String =
    s"Insight request failed: ${t.getLocalizedMessage}"

  def invalidJson(): String =
    s"Request did not contain valid JSON."

  def monitorFailed(t:Throwable):String =
    s"Monitor request failed: ${t.getLocalizedMessage}"

  def rocksNotInitialized():String =
    s"The RocksDB is not initialized yet."
}
