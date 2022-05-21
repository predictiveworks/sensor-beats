package de.kp.works.sensor.weather.enums

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

object WeTables extends Enumeration {
  type WeTable = Value

  val CLOUDINESS: WeTable  = Value(1, "cloudiness")
  val FEELS_LIKE: WeTable  = Value(2, "feels_like")
  val HUMIDITY: WeTable    = Value(3, "humidity")
  val PRESSURE: WeTable    = Value(4, "pressure")
  val RAIN_1H: WeTable     = Value(5, "rain_1h")
  val RAIN_3H: WeTable     = Value(6, "rain_3h")
  val SNOW_1H: WeTable     = Value(7, "snow_1h")
  val SNOW_3H: WeTable     = Value(8, "snow_3h")
  val TEMP: WeTable        = Value(9, "temp")
  val TEMP_MAX: WeTable    = Value(10, "temp_max")
  val TEMP_MIN: WeTable    = Value(11, "temp_min")
  val WIND_DEG: WeTable    = Value(12, "wind_deg")
  val WIND_GUST: WeTable   = Value(13, "wind_gust")
  val WIND_SPEED: WeTable  = Value(14, "wind_speed")

}
