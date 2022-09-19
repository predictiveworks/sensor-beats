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

import org.apache.spark.sql._

/**
 * https://thru.de/thrude/downloads/
 *
 * - Pollution (air & water)
 * - Waste
 * - Waste water
 */
object ThrudeClient {

  private val folder = "/Volumes/iomega/data/thrude/CSV_PRTR-Export_GERMANY_2022-04-29"

  val session: SparkSession = BeatSession.getSession
  import session.implicits._

  /**
   * Total = 63.474
   */
  def readPollution(): DataFrame = {
    session
      .read
      .parquet(s"$folder/2022-05-09_PRTR-Deutschland_Freisetzungen.parquet")
  }

  def extractLoadPollution(): Unit = {

    val path = s"$folder/2022-05-09_PRTR-Deutschland_Freisetzungen.csv"
    val dropcols = Seq(
      "taet_nr",
      "taetigkeit",
      "haupttaetigkeit",
      "branche",
      "nace_wirtschaftszweig",
      "stoffgruppe",
      "schadstoff",
      "umweltkompartiment",
      "einheit",
      "bestimmungsmethode",
      "schutzgrund_fracht",
      "schutzgrund_betrieb")

    extractLoad(path,dropcols)

  }

  def readWasteRisk():DataFrame = {
    session
      .read
      .parquet(s"$folder/2022-05-09_PRTR-Deutschland_Abfall_gefaehrlich.parquet")

  }

  def extractLoadWasteRisk(): Unit = {

    val path = s"$folder/2022-05-09_PRTR-Deutschland_Abfall_gefaehrlich.csv"
    val dropcols = Seq(
      "taet_nr",
      "taetigkeit",
      "haupttaetigkeit",
      "branche",
      "nace_wirtschaftszweig",
      "abfallart",
      "einheit",
      "bestimmungsmethode",
      "verw_o_beseit",
      "ausland_inland",
      "verwertungs_beseitungs_land",
      "schutzgrund_fracht",
      "schutzgrund_betrieb")

    extractLoad(path,dropcols)

  }

  def readWaste():DataFrame = {
    session
      .read
      .parquet(s"$folder/2022-05-09_PRTR-Deutschland_Abfall_nichtgefaehrlich.parquet")

  }

  def extractLoadWaste(): Unit = {

    val path = s"$folder/2022-05-09_PRTR-Deutschland_Abfall_nichtgefaehrlich.csv"
    val dropcols = Seq(
      "taet_nr",
      "taetigkeit",
      "haupttaetigkeit",
      "branche",
      "nace_wirtschaftszweig",
      "abfallart",
      "einheit",
      "bestimmungsmethode",
      "verw_o_beseit",
      "ausland_inland",
      "verwertungs_beseitungs_land",
      "schutzgrund_fracht",
      "schutzgrund_betrieb")

    extractLoad(path,dropcols)

  }

  def readWasteWater():DataFrame = {
    session
      .read
      .parquet(s"$folder/2022-05-09_PRTR-Deutschland_Abwasser.parquet")

  }

  def extractLoadWasteWater(): Unit = {

    val path = s"$folder/2022-05-09_PRTR-Deutschland_Abwasser.csv"
    val dropcols = Seq(
      "t_nr",
      "taetigkeit",
      "haupttaetigkeit",
      "branche",
      "nace_wirtschaftszweig",
      "abfallart",
      "einheit",
      "umweltkompartiment",
      "bestimmungsmethode",
      "schutzgrund_fracht",
      "schutzgrund_betrieb")

    extractLoad(path,dropcols)

  }

  def extractLoad(path:String, dropcols:Seq[String]):Unit = {
    /*
     * NOTE: The original file is ISO-8859-1 encoding
     * and was re-encoded to UTF-8 before opening.
     */
    val ds: Dataset[String] = session
      .read
      .text(path)
      .as[String]

    val df = session.read
      .option("header", "true")
      .option("delimiter", ";")
      .csv(ds)
      .drop(dropcols: _*)

    df
      .distinct
      .coalesce(1)
      .write.mode(SaveMode.Overwrite)
      .parquet(path.replace(".csv", ".parquet"))

  }
}
