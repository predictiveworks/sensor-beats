package de.kp.works.beats.sensor.netvox

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
 * Netvox output channel for ThingsBoard
 */
class NvBoard(options:NvOptions)
  extends thingsboard.Producer[NvConf](options.toBoard) with NvLogging {
}

/**
 * Implementation of the FIWARE output channel
 */
class MsFiware(options:NvOptions)
  extends fiware.Producer[NvConf](options.toFiware) {
}

/**
 * Implementation of the RocksDB output channel
 */
class MsRocks(options:NvOptions) extends BeatSink {

  private val rocksApi:BeatRocksApi = BeatRocksApi.getInstance

  override def execute(request: BeatRequest): Unit = {
    /*
     * The RocksDB output channel is restricted
     * to `put` requests
     */
    val sensor = request.sensor
    val time   = sensor.sensorTime
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
  /**
   * This method assumes that the tables names have
   * their configured names at this stage (see MsHelium
   * and others)
   */
  private def initRocksDB(tables:Seq[String]):Unit = {

    if (rocksApi.isInit) return
    /*
     * Retrieve the predefined tables that represent
     * a combination of static tables, configured
     * measurements and mappings of decoded field
     * names
     */
    val specs = options.getRocksTables
    /*
     * Validate that the decoded and optionally mapped
     * table names are compliant with the specified
     * ones
     */
    tables.foreach(table => {
      if (!specs.contains(table))
        throw new Exception(s"Unknown table name `$table` detected")
    })
    /*
     * Finally create the RocksDB column families
     * from the provided table names
     */
    val folder = options.getRocksFolder
    rocksApi.createIfNotExist(tables, folder)

  }
}