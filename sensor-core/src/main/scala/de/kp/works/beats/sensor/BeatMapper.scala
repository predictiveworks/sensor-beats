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

import com.google.gson.JsonParser
import scala.collection.JavaConversions.iterableAsScalaIterable

case class BeatMapping(alias:String, name:String, unit:String)

abstract class BeatMapper {

  var MAPPINGS:String

  private val mappings = getMappings

  /**
   * This method harmonizes the SensorBeat
   * specific measurement names
   */
  def harmonize(name:String):String = {

    if (mappings.contains(name))
      mappings(name).alias

    else
      name

  }

  private def getMappings:Map[String,BeatMapping] = {

    val json = JsonParser
      .parseString(MAPPINGS).getAsJsonArray

    json.map(elem => {
      val mapping = elem.getAsJsonObject

      val k = mapping.get("name").getAsString
      val v = BeatMapping(
        alias = mapping.get("alias").getAsString,
        name  = k,
        unit  = mapping.get("unit").getAsString
      )

      (k,v)

    }).toMap

  }

}
