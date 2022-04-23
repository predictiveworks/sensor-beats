package de.kp.works.beats.sensor.milesight

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

import de.kp.works.beats.sensor.BeatRocks

object MsRocksApi {

  private var instance:Option[MsRocksApi] = None

  def getInstance: MsRocksApi = instance.get

  def getInstance(options:MsOptions):MsRocksApi = {
    if (instance.isEmpty) instance = Some(new MsRocksApi(options))
    instance.get
  }

  def isInstance:Boolean = instance.nonEmpty

}

class MsRocksApi(options:MsOptions) {
  /**
   * Initialize RocksDB with provided tables; every table
   * refers to a certain column family.
   */
  val tables: Seq[String] = options.getRocksTables
  BeatRocks.getOrCreate(tables, options.getRocksFolder)

  def put(table:String, time:Long, value:String):Unit = {
    BeatRocks.putTs(table, time, value)
  }

  def scan(table:String):Seq[(Long, String)] = {
    BeatRocks.scanTs(table)
  }

}
