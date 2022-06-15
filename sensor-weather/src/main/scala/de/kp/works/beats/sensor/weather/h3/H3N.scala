package de.kp.works.beats.sensor.weather.h3

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
import com.uber.h3core._
import com.uber.h3core.util.GeoCoord
import org.apache.spark.sql.Row
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._

import java.lang
import java.util.{List => JList}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * Make H3 serializable to enable usage
 * within Apache Spark SQL UDFs
 */
object H3 extends Serializable {
  val instance:H3Core = H3Core.newInstance()
}
/**
 * Scaling spatial operations with H3 is essentially a
 * two step process:
 *
 * The first step is to compute an H3 index for each
 * feature (points, polygons, ...) defined as UDF.
 *
 * The second step is to use these indices for spatial
 * operations such as spatial join (point in polygon,
 * k-nearest neighbors, etc).
 */
object H3Utils extends Serializable {

  def resolutionToM2(res:Int):Double = {
    H3.instance.hexArea(res, AreaUnit.m2)
  }
  /**
   * Indexes the location at the specified resolution,
   * returning the index of the cell containing this
   * location.
   */
  def pointToH3(resolution:Int): UserDefinedFunction =
    udf((lat:Double, lon:Double) =>
      H3.instance.geoToH3(lat, lon, resolution))

  def jsonToMultigon: UserDefinedFunction =
    udf((json:String) => {
      /*
       * The H3 compliant data format the provided
       * `json` is converted to
       */
      val multigon = mutable.ArrayBuffer.empty[mutable.ArrayBuffer[(Double, Double)]]

      val jsonArray = JsonParser.parseString(json).getAsJsonArray
      jsonArray.foreach(elem => {

        val polygon = mutable.ArrayBuffer.empty[(Double, Double)]

        elem.getAsJsonArray.foreach(item => {

          val jPoint = item.getAsJsonObject
          polygon += ((jPoint.get("latitude").getAsDouble, jPoint.get("longitude").getAsDouble))

        })

        multigon += polygon

      })

      multigon

  })

  def multigonToH3(resolution:Int): UserDefinedFunction =
    udf((multigon:mutable.WrappedArray[mutable.WrappedArray[Row]]) => {
      /*
       * This method specifies a polygon without holes
       */
      val holes:List[JList[GeoCoord]] = List()
      val points = multigon.flatMap(polygon => {
        polygon.map(row => {

          val lat = row.getAs[Double](0)
          val lon = row.getAs[Double](1)

          new GeoCoord(lat, lon)

        }).toList

      }).toList

      H3.instance.polyfill(points, holes, resolution).toList

  })

}