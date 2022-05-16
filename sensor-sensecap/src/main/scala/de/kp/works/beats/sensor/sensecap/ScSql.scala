package de.kp.works.beats.sensor.sensecap

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

import de.kp.works.beats.sensor._
import org.apache.spark.sql.BeatSession

/**
 * [MsSql] supports the `Sensor as a Table` concept,
 * that is taken from Osquery.
 */
class ScSql extends ScLogging {

  private val session = BeatSession.getSession
  private val beatSql = new BeatSql(session, logger)

  def read(sql:String):String =
    beatSql.read(sql)

  def trend(sql:String, indicator:String, timeframe:Int=5):String =
    beatSql.trend(sql, indicator, timeframe)

}
