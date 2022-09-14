package de.kp.works.beats.sensor.entsog

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

import de.kp.works.beats.sensor.http.HttpConnect
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{BeatSession, Column, SaveMode}

import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.JavaConverters.seqAsJavaListConverter

/**
 * The [EntsogClient] retrieves dynamic gas flow
 * information, and associated reference data like
 * operators, storage facilities etc.
 */
object EntsogClient extends HttpConnect {

  private val folder = ""
  private val session = BeatSession.getSession

  import session.implicits._

  def writeBalancingZones():Unit = write("balancingzones")

  def writeConnectionPoints():Unit = write("connectionpoints")

  def writeInterconnections():Unit = write("Interconnections")

  def writeOperationalData(params:Array[(String,String)]=Array.empty[(String,String)]):Unit = {

    val selcols = Seq(
      "pointKey",
      "pointLabel",
      "operatorKey",
      "operatorLabel",
      "directionKey",
      "flowStatus",
      "unit",
      "value",
      "indicator",
      "periodFrom",
      "periodTo",
      "periodType").map(col)

    write("operationalData", params, selcols)

  }

  def writeOperators():Unit = write("operators")

  def writeOperatorPointDirections():Unit = write("operatorpointdirections")

  private def write(key:String,
                    params:Array[(String,String)]=Array.empty[(String,String)],
                    selcols:Seq[Column]=Seq.empty[Column]):Unit = {

    var endpoint = EntsogDefs.ENTSOG_ENDPOINT + s"/$key?limit=-1"
    if (params.nonEmpty)
      endpoint = endpoint + "&" + params
        .map{case(k,v) => s"$k=$v"}.mkString("&")

    endpoint = endpoint.replace(" ", "%20")
    val source = get(endpoint)

    val json = extractJsonBody(source)

    val points = json
      .getAsJsonObject.get(key)
      .getAsJsonArray
      .map(point => point.getAsJsonObject.toString)
      .toList.asJava

    val ds = session.createDataset(points)
    val sample = {

      if (selcols.nonEmpty)
        session.read.json(ds).select(selcols:_*)

      else
        session.read.json(ds)

    }

    sample
      .coalesce(1)
      .write.mode(SaveMode.Overwrite)
      .json(s"${folder}entsog.${key.toLowerCase}.json")

  }
}
