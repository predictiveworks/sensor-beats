package de.kp.works.beats.sensor.weather

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

object WeMessages {

  def forecastFailed(t:Throwable):String =
    s"Retrieving MOSMIX forecast failed: ${t.getLocalizedMessage}"

  def inverterFailed(t:Throwable):String =
    s"Retrieving CEC inverter failed: ${t.getLocalizedMessage}"

  def invertersFailed(t:Throwable):String =
    s"Retrieving CEC inverters failed: ${t.getLocalizedMessage}"

  def irradianceReceived():String =
    "Request to compute solar irradiance received."

  def moduleFailed(t:Throwable):String =
    s"Retrieving CEC module failed: ${t.getLocalizedMessage}"

  def modulesFailed(t:Throwable):String =
    s"Retrieving CEC modules failed: ${t.getLocalizedMessage}"

  def positionsReceived():String =
    "Request to compute solar positions received."
}
