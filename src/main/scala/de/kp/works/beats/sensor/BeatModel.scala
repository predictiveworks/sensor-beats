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

import com.google.gson.JsonObject

object BeatActions extends Enumeration {
  type BeatAction = Value

  val READ: BeatActions.Value   = Value(1, "read")
  val WRITE: BeatActions.Value  = Value(2, "write")
  val CREATE: BeatActions.Value = Value(3, "create")
  val UPDATE: BeatActions.Value = Value(4, "update")
  val DELETE: BeatActions.Value = Value(5, "delete")

}

/**
 * Unique definition of a sensor attribute
 */
case class BeatAttr(
  /*
   * The name of the attribute
   */
  attrName:String,
  /*
   * Data type of the attribute
   */
  attrType:String,
  /*
   * The value of the of attribute
   */
  attrValue:Number)
/**
 * Unique definition of a time point, i.e.,
 * a (time, value) pair
 */
case class BeatDot(time:Long, value:Double)

object BeatInfos extends Enumeration {
  type BeatInfo = Value

  val ANOMALY: BeatInfos.Value  = Value(1, "anomaly")
  val FORECAST: BeatInfos.Value = Value(2, "forecast")
  val MONITOR: BeatInfos.Value  = Value(3, "monitor")

}

/**
 * A unique definition of a BeatChannel
 * request
 */
case class BeatRequest(action:BeatActions.Value, sensor:BeatSensor)
/**
 * Unique definition of `Beat` sensor and its
 * mapping to the e.g. FIWARE (output) channel.
 */
case class BeatSensor(
  /*
   * Unique identifier of the sensor as required
   * by FIWARE interface
   */
  sensorId:String,
  /*
   * Type of the sensor as required by FIWARE
   * interface
   */
  sensorType:String,
  /*
   * The brand name of the sensor; this is mapped
   * onto a regular FIWARE attribute
   */
  sensorBrand:String,
  /*
   * The information type, this sensor event
   * refers to
   */
  sensorInfo:BeatInfos.Value,
  /*
   * The timestamp, this sensor representation
   * was created
   */
  sensorTime:Long,
  /*
   * The attributes assigned to this sensor
   */
  sensorAttrs:Seq[BeatAttr]) {

  def toJson:JsonObject = {

    val json = new JsonObject
    json.addProperty("id", sensorId)
    json.addProperty("type", sensorType)
    /*
     * Brand as a regular NGSI attribute
     */
    val brand = new JsonObject
    brand.addProperty("type", "String")
    brand.addProperty("value", sensorBrand)

    json.add("brand", brand)
    /*
     * Information type as a regular NGSI attribute
     */
    val infoType = new JsonObject
    infoType.addProperty("type", "String")
    infoType.addProperty("value", sensorInfo.toString)

    json.add("infoType", infoType)

    sensorAttrs.foreach(sensorAttr => {

      val attr = new JsonObject
      attr.addProperty("type", sensorAttr.attrType)
      attr.addProperty("value", sensorAttr.attrValue)

      json.add(sensorAttr.attrName, attr)

    })

    json

  }
}
