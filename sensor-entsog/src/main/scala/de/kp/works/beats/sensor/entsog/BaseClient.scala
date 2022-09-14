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

import com.google.gson.{JsonElement, JsonObject, JsonParser}
import de.kp.works.beats.sensor.http.HttpConnect
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{BeatSession, DataFrame, SaveMode, SparkSession}

import java.io.FileWriter
import java.text.SimpleDateFormat
import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.JavaConverters.seqAsJavaListConverter

trait BaseClient extends HttpConnect {

  val DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd")

  val session: SparkSession = BeatSession.getSession
  import session.implicits._

  def computeInterval(from:String, to:String, meta:JsonObject):(Boolean, String,String) = {

    var request = true

    var reqFrom = from
    var reqTo   = to

    if (meta.size() > 0) {
      /*
       * Determine the correct request parameters
       * with respect to `from` and `to`
       */
      val from_old = DATE_FORMAT.parse(meta.get("from").getAsString)
      val from_new = DATE_FORMAT.parse(from)

      val from_cmp = from_new.compareTo(from_old)

      val to_old = DATE_FORMAT.parse(meta.get("to").getAsString)
      val to_new = DATE_FORMAT.parse(to)

      val to_cmp = to_new.compareTo(to_old)

      if (from_cmp >= 0 && to_cmp <= 0) {
        /*
         * The current request is completely
         * covered by the previous request
         */
        request = false
      }
      if (from_cmp >= 0 && to_cmp > 0) {
        /*
         * The current request retrieves the
         * delta between the previous and
         * current request.
         *
         * In this case the `from` parameter
         * is not consider to ensure a seamless
         * dataset
         */
        reqFrom = meta.get("to").getAsString
        reqTo   = to
      }
      if (from_cmp < 0 && to_cmp <= 0) {
        /*
         * The current request retrieves the
         * delta between the previous and
         * current request
         */
        reqFrom = meta.get("from").getAsString
        reqTo   = from
      }
      if (from_cmp < 0 && to_cmp > 0) {
        /*
         * The current request encloses the
         * previous request and the current
         * implementation initiates another
         * API request
         */
        reqFrom = from
        reqTo   = to
      }

    }

    (request, reqFrom, reqTo)

  }

  def extractDots(json:JsonElement, field:Option[String] = None):List[String] = {

    val jsonArray =
      if (field.isEmpty) json.getAsJsonArray
      else {
        json
          .getAsJsonObject
          .get(field.get).getAsJsonArray

      }

    jsonArray
      .map(point => point.getAsJsonObject.toString)
      .toList

  }

  def readDF(path:String):DataFrame = {

    try {
      session.read.json(path)

    } catch {
      case _:Throwable => session.emptyDataFrame
    }

  }

  def writeDF(dots:List[String], path:String):DataFrame = {

    val stored = readDF(path)

    if (dots.isEmpty) stored
    else {

      val ds = session.createDataset(dots.asJava)
      val df = session.read.json(ds)

      val selcols = df.schema.fieldNames.sorted.map(col)
      /*
       * Merge with existing dataset
       */
      val output =
        if (stored.isEmpty) df.select(selcols:_*)
        else
          stored.union(df.select(selcols:_*))

      output
        .distinct
        .coalesce(1)
        .write.mode(SaveMode.Overwrite)
        .json(path)

      output

    }

  }

  def writeMetadata(meta:JsonObject, path:String):Unit = {

    val writer = new FileWriter(path, false)
    writer.write(meta.toString)
    writer.close()

  }

  def getMetadata(path:String):JsonObject = {

    try {

      val source = scala.io.Source.fromFile(new java.io.File(path))
      val document = source.getLines.mkString

      val metadata = JsonParser.parseString(document)
        .getAsJsonObject

      source.close
      metadata

    } catch {
      case _:Throwable => new JsonObject

    }
  }

}
