package de.kp.works.sensor.weather.json

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

import com.google.gson.JsonObject

trait JsonUtil {

  def getDoubleWithScale(json:JsonObject, name:String, scale:Int=2):Double = {

    val raw_value = json.get(name).getAsDouble
    BigDecimal(raw_value).setScale(scale, BigDecimal.RoundingMode.HALF_UP).toDouble

  }

  def getOrElseDouble(json:JsonObject, name:String, default: Double):Double = {

    val value = json.get(name)
    if (value == null) return default
    /*
     * This methods supports [Double] and [Int]
     */
    try {
      value.getAsDouble

    } catch {
      case _:Throwable =>
        value.getAsInt.toDouble
    }
  }

  def getOrElseInt(json:JsonObject, name:String, default: Int):Int = {

    val value = json.get(name)
    if (value == null) return default
    /*
     * This methods supports [Double] and [Int]
     */
    try {
      value.getAsInt

    } catch {
      case _:Throwable =>
        default
    }
  }

  def getAsObject(json:JsonObject, name:String):JsonObject = {

    val value = json.get(name)
    if (value == null) return null

    value.getAsJsonObject

  }

}
