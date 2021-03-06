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

import com.google.gson.JsonObject
import de.kp.works.beats.sensor._

/**
 * OpenWeather output channel for ThingsBoard
 */
class WeBoard(options:WeOptions)
  extends thingsboard.Producer[WeConf](options.toBoard) with WeLogging {
}

/**
 * Implementation of the FIWARE output channel
 */
class WeFiware(options:WeOptions)
  extends fiware.Producer[WeConf](options.toFiware) {
}

/**
 * Implementation of the RocksDB output channel
 */
class WeRocks(options:WeOptions) extends BeatSink {

  private val rocksApi:BeatRocksApi = BeatRocksApi.getInstance

  override def execute(request: BeatRequest): Unit = {
    /*
     * The RocksDB output channel is restricted
     * to `put` requests
     */
    val sensor = request.sensor
    val time = sensor.sensorTime
    val attrs  = sensor.sensorAttrs
    /*
     * Sensors exist where the supported measurements
     * cannot be inferred from the product name.
     */
    val tables = attrs.map(_.attrName)
    initRocksDB(tables)
    /*
     * Individual sensor attributes are mapped onto
     * RocksDB
     */
    attrs.foreach(attr => {
      /*
       * The attribute name is used as the `table`
       * name of the respective RocksDB
       */
      val table = attr.attrName

      val value = new JsonObject
      value.addProperty("type", attr.attrType)
      value.addProperty("value", attr.attrValue)

      rocksApi.put(table, time, value.toString)

    })

  }

  private def initRocksDB(tables:Seq[String]):Unit = {

    //TODO
  }
}