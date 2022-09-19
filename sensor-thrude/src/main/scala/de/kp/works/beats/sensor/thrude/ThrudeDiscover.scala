package de.kp.works.beats.sensor.thrude

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

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col, udf}

object ThrudeDiscover {

  /** AIR POLLUTION **/

  def airPollutionByCity(date:Int=2020):DataFrame = {
    /*
     * Unit: kg/year => ton/year
     *
     * Total (2020) = 892 cities
     */
    val pollution = airPollution(date)
      .withColumn("total_release", col("total_release") / 1000)
      .withColumnRenamed("ort", "city")
      .groupBy(col("city")).sum("total_release")
      .sort(col("city"))

    pollution

  }

  def airPollutionByFirm(date:Int=2020):DataFrame = {
    /*
     * Unit: kg/year => ton/year
     *
     * Total (2020) = 1.268 firms
     */
    val pollution = airPollution(date)
      .withColumn("total_release", col("total_release") / 1000)
      .withColumnRenamed("betriebsname", "name")
      .groupBy(col("name")).sum("total_release")
      .sort(col("name"))

    pollution

  }

  def airPollutionByPostal(date:Int=2020):DataFrame = {
    /*
     * Unit: kg/year => ton/year
     *
     * Total (2020) = 949 postal codes
     */
    val pollution = airPollution(date)
      .withColumn("total_release", col("total_release") / 1000)
      .withColumnRenamed("plz", "postal_code")
      .groupBy(col("postal_code")).sum("total_release")
      .sort(col("postal_code"))

    pollution

  }

  def airPollutionByState(date:Int=2020):DataFrame = {
    /*
     * Unit: kg/year => ton/year
     */
    val pollution = airPollution(date)
      .withColumn("total_release", col("total_release") / 1000)
      .withColumnRenamed("bundesland", "state")
      .groupBy(col("state")).sum("total_release")
      .sort(col("state"))

    pollution

  }

  /**
   * This method restricts the pollution dataset
   * to air pollutions of a certain year and
   * aggregates releases and accidental releases
   */
  def airPollution(date:Int):DataFrame = {

    val toDouble = udf((value:String) =>
      value.replace(",", ".").toDouble)

    /*
     * Unit: kg/year => ton/year
     */
    val pollution = ThrudeClient.readPollution()
      .filter(col("jahr") === date)
      .filter(col("releases_to") === "Air")
      .withColumn("release", toDouble(col("jahresfracht_freisetzung")))
      .withColumn("acc_release", toDouble(col("versehentliche_freisetzung")))
      .withColumn("total_release", col("release") + col("acc_release"))

    pollution

  }
}
