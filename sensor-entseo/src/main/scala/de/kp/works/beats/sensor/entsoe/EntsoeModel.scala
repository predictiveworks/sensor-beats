package de.kp.works.beats.sensor.entsoe

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

case class EntsoeDot(
  startTs:Long,
  endTs:Long,
  quantity:Int)

/**
 * The EntsoeRequest is registered to access
 * already available request data without the
 * need to perform a HTTP request.
 *
 * This mechanism is introduced to cope with
 * the request limit of the ENTSOE API.
 */
case class EntsoeRequest(
  timestamp:Long,
  params: Map[String,String]
)

case class EntsoeSeries(
  startTs:Long,
  endTs:Long,
  resolution:String,
  unitOfMeasure:String,
  dots:Seq[EntsoeDot])
