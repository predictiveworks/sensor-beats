package de.kp.works.beats.sensor.milesight.channel

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
import de.kp.works.beats.sensor.milesight.{MsOptions, MsRocksApi}
import de.kp.works.beats.sensor.{BeatChannel, BeatRequest}

/**
 * Implementation of the RocksDB output channel
 */
class MsRocks(options:MsOptions) extends BeatChannel {

  private val rocksDB =
    if (MsRocksApi.isInstance)
      MsRocksApi.getInstance
    else
      MsRocksApi.getInstance(options)

  override def execute(request: BeatRequest): Unit = {
    /*
     * The RocksDB output channel is restricted
     * to `put` requests
     */
    val sensor = request.sensor
    val time = sensor.sensorTime
    /*
     * Individual sensor attributes are mapped onto
     * RocksDB
     */
    sensor.sensorAttrs.foreach(sensorAttr => {
      /*
       * The attribute name is used as the `table`
       * name of the respective RocksDB
       */
      val table = sensorAttr.attrName

      val value = new JsonObject
      value.addProperty("type", sensorAttr.attrType)
      value.addProperty("value", sensorAttr.attrValue)

      rocksDB.put(table, time, value.toString)

    })

  }

}
