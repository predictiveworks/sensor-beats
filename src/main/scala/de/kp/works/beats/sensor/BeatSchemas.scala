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

import org.apache.spark.sql.types.{DoubleType, LongType, StructField, StructType}

object BeatSchemas {
  /**
   *  Public method to define a `battery` table
   */
  def battery:(String, StructType) = {

    val table = "battery"
    val schema = baseSchema

    (table, schema)

  }
  /**
   *  Public method to define a `carbon_dioxide` table
   */
  def carbon_dioxide:(String, StructType) = {

    val table = "carbon_dioxide"
    val schema = baseSchema

    (table, schema)

  }
  /**
   *  Public method to define a `carbon_monoxide` table
   */
  def carbon_monoxide:(String, StructType) = {

    val table = "carbon_monoxide"
    val schema = baseSchema

    (table, schema)

  }
  /**
   *  Public method to define a `humidity` table
   */
  def humidity:(String, StructType) = {

    val table = "humidity"
    val schema = baseSchema

    (table, schema)

  }
  /**
   *  Public method to define a `temperature` table
   */
  def temperature:(String, StructType) = {

    val table = "humidity"
    val schema = baseSchema

    (table, schema)

  }
  /**
   * Private method to define the base schema for
   * a certain physical sensor property
   */
  private def baseSchema:StructType = {
    StructType(Array(
      StructField("time",  LongType, nullable = false),
      StructField("value", DoubleType, nullable = false)

    ))
  }
}
