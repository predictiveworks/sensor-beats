package de.kp.works.beats.sensor.uradmonitor

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
 * Uradmonitor output channel for ThingsBoard
 */
class UmBoard(options:UmOptions)
  extends thingsboard.Producer[UmConf](options.toBoard) with UmLogging {
}

/**
 * Implementation of the FIWARE output channel
 */
class UmFiware(options:UmOptions)
  extends fiware.Producer[UmConf](options.toFiware) {
}

/**
 * Implementation of the RocksDB output channel
 */
class UmRocks(options:UmOptions) extends BeatSink {

  /**
   * Retrieve the predefined tables that represent
   * a combination of static tables, configured
   * measurements and mappings of decoded field
   * names
   */
  private val specs = options.getRocksTables
  private val rocksApi:BeatRocksApi = BeatRocksApi.getInstance

  initRocksDB()

  override def execute(request: BeatRequest): Unit = {
    /*
     * The RocksDB output channel is restricted
     * to `put` requests
     */
    val sensor = request.sensor
    val time   = sensor.sensorTime
    val attrs  = sensor.sensorAttrs
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
      /*
       * Make sure that only those attributes are
       * persisted that are compliant to the table
       * specifications
       */
      if (specs.contains(table)) {

        val value = new JsonObject
        value.addProperty("type", attr.attrType)
        value.addProperty("value", attr.attrValue)

        rocksApi.put(table, time, value.toString)

      }

    })

  }

  private def initRocksDB():Unit = {

    if (rocksApi.isInit) return
    /*
     * Finally create the RocksDB column families
     * from the configured table specifications.
     */
    val folder = options.getRocksFolder
    rocksApi.createIfNotExist(specs, folder)

  }
}