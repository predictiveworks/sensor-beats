package de.kp.works.beats.sensor.weather.sandia

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

import com.google.gson.{JsonElement, JsonNull, JsonObject}
import de.kp.works.beats.sensor.http.HttpConnect
import de.kp.works.beats.sensor.weather.WeLogging
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types._

import java.io.File
import java.nio.file.{Files, Paths}
/**
 * This object loads the latest CEC modules and inverters
 * from NREL/SAM github account and exposes them as SQL
 * tables (in-memory)
 */
object SAMRegistry extends HttpConnect with WeLogging {

  private val downloadCfg = config.getDownloadCfg
  private val folder = downloadCfg.getString("folder")
  /**
   * Latest CEC inverters
   */
  private val CEC_INVERTERS = "sandia_cec_inverters.csv"
  private val CEC_INVERTERS_URL = "https://raw.githubusercontent.com/NREL/SAM/develop/deploy/libraries/CEC%20Inverters.csv"
  /**
   * Latest CEC modules
   */
  private val CEC_MODULES = "sandia_cec_modules.csv"
  private val CEC_MODULES_URL = "https://raw.githubusercontent.com/NREL/SAM/develop/deploy/libraries/CEC%20Modules.csv"

  def searchModules(session:SparkSession, term:String):DataFrame = {

    val sql = s"select * from global_temp.CEC_MODULES where Name like '%$term%' or Manufacturer like '%$term%'"
    session.sql(sql)

  }

  def getInverters(session:SparkSession):DataFrame = {
    val sql = s"select * from global_temp.CEC_INVERTERS"
    getInvertersBySql(session,sql)
  }

  def getInvertersBySql(session:SparkSession, sql:String):DataFrame = {
    session.sql(sql)
  }

  /**
   * Public method to retrieve inverter parameter
   * specification as JSON object that is required
   * by `pvlib`.
   */
  def getInverter(session:SparkSession, name:String):JsonElement = {

    val sql = s"select * from global_temp.CEC_INVERTERS where Name like '%$name%'"
    val inverter = session.sql(sql)

    if (inverter.isEmpty)
      JsonNull.INSTANCE

    else
      inverters2Json(inverter)

  }
  /**
   * Public method to retrieve the inverter parameters of
   * a certain PV invert registered in SAM's CEC Inverters
   */
  def getInverterParams(session:SparkSession, name:String):JsonElement = {

    val json = SAMRegistry.getInverter(session, name)
    if (json.isJsonNull) return json

    val removables = Seq("Name")
    val params = json.getAsJsonObject

    removables.foreach(key => params.remove(key))
    params

  }

  def getModules(session:SparkSession):DataFrame = {

    val sql = s"select * from global_temp.CEC_MODULES"
    getModulesBySql(session, sql)

  }

  def getModulesBySql(session:SparkSession, sql:String):DataFrame = {
    session.sql(sql)
  }

  /**
   * Public method to retrieve module parameter
   * specification as JSON object that is required
   * by `pvlib`.
   */
  def getModule(session:SparkSession, manufacturer:String, name:String):JsonElement = {

    val sql = s"select * from global_temp.CEC_MODULES where Name like '%$name%' and Manufacturer like '%$manufacturer%'"
    val module = session.sql(sql)

    if (module.isEmpty)
      JsonNull.INSTANCE

    else
      module2Json(module)

  }

  /**
   * Public method to retrieve the module parameters
   * of a certain PV module (system) registered in
   * SAM's CEC Modules
   */
  def getModuleParams(session:SparkSession, manufacturer:String, name:String):JsonElement = {

    val json = SAMRegistry.getModule(session, manufacturer, name)
    if (json.isJsonNull) return json

    val removables = Seq("Date", "Manufacturer", "Name", "Version")
    val params = json.getAsJsonObject

    removables.foreach(key => params.remove(key))
    params

  }

  /**
   * This private method transforms the first row
   * of the provided dataset into a JSON object
   */
  private def module2Json(dataset:DataFrame):JsonElement = {

    val doubles = Seq(
      "STC", "PTC", "A_c", "Length", "Width", "I_sc_ref", "V_oc_ref", "I_mp_ref",
      "V_mp_ref",  "alpha_sc",  "beta_oc", "T_NOCT", "a_ref", "I_L_ref", "I_o_ref",
      "R_s", "R_sh_ref", "Adjust", "gamma_r")

    val integers = Seq("Bifacial", "N_s")

    val schema = dataset.schema
    val fields = schema.fields

    val json = new JsonObject
    val row = dataset.collect().head

    fields.foreach(field => {

      val fname = field.name
      val fidex = schema.fieldIndex(fname)

      val ftype = field.dataType
      ftype match {

        case BooleanType =>
          json.addProperty(fname, row.getBoolean(fidex))

        case DoubleType =>
          json.addProperty(fname, row.getDouble(fidex))

        case FloatType =>
          json.addProperty(fname, row.getFloat(fidex))

        case IntegerType =>
          json.addProperty(fname, row.getInt(fidex))

        case LongType =>
          json.addProperty(fname, row.getLong(fidex))

        case ShortType =>
          json.addProperty(fname, row.getShort(fidex))

        case StringType =>
          val str = row.getString(fidex)

          if (str == null) json.add(fname, null)
          else {
            if (doubles.contains(fname))
              json.addProperty(fname, str.toDouble)

            else if (integers.contains(fname))
              json.addProperty(fname, str.toInt)

            else
              json.addProperty(fname, str)

          }

        case _ =>
          throw new Exception(s"Data type `${ftype.simpleString}` not supported")
      }

    })

    json

  }
  /**
   * This private method transforms the first row
   * of the provided dataset into a JSON object
   */
  private def inverters2Json(dataset:DataFrame):JsonElement = {

    val doubles = Seq(
      "Pso", "Pdco", "C0", "C1", "C2", "C3", "Pnt", "Idcmax"
    )

    val integers = Seq(
      "Vac", "Paco", "Vdco", "Vdcmax", "Mppt_low"
    )

    val schema = dataset.schema
    val fields = schema.fields

    val json = new JsonObject
    val row = dataset.collect().head

    fields.foreach(field => {

      val fname = field.name
      val fidex = schema.fieldIndex(fname)

      val ftype = field.dataType
      ftype match {

        case BooleanType =>
          json.addProperty(fname, row.getBoolean(fidex))

        case DoubleType =>
          json.addProperty(fname, row.getDouble(fidex))

        case FloatType =>
          json.addProperty(fname, row.getFloat(fidex))

        case IntegerType =>
          json.addProperty(fname, row.getInt(fidex))

        case LongType =>
          json.addProperty(fname, row.getLong(fidex))

        case ShortType =>
          json.addProperty(fname, row.getShort(fidex))

        case StringType =>
          val str = row.getString(fidex)

          if (str == null) json.add(fname, null)
          else {
            if (doubles.contains(fname))
              json.addProperty(fname, str.toDouble)

            else if (integers.contains(fname))
              json.addProperty(fname, str.toInt)

            else
              json.addProperty(fname, str)

          }

        case _ =>
          throw new Exception(s"Data type `${ftype.simpleString}` not supported")
      }

    })

    json

  }

  /**
   * Public method to support the bootstrap phase
   * of this (Weather) Beat:
   *
   * it loads Clean Energy Council (CEC) approved
   * photovoltaic (PV) modules and inverters and
   * exposes them as Apache Spark tables
   */
  def load(session:SparkSession, replace:Boolean):Unit = {

    loadInverters(session, replace)
    loadModules(session, replace)

  }

  def loadInverters(session:SparkSession, replace:Boolean):Unit = {

    val files = new File(folder).list()
    val filtered = files.filter(fname => fname.contains(CEC_INVERTERS))

    val file = s"$folder/$CEC_INVERTERS"
    if (filtered.isEmpty) {
      invertersAsView(session, file, replace = true)

    }
    else {
      invertersAsView(session, file, replace)

    }

  }

  def loadModules(session:SparkSession, replace:Boolean):Unit = {

    val files = new File(folder).list()
    val filtered = files.filter(fname => fname.contains(CEC_MODULES))

    val file = s"$folder/$CEC_MODULES"
    if (filtered.isEmpty) {
      modulesAsView(session, file, replace = true)

    }
    else {
      modulesAsView(session, file, replace)

    }
  }

  private def invertersAsView(session:SparkSession, file:String, replace:Boolean):Unit = {

    if (replace) {

      Files.delete(Paths.get(file))

      val result = download(CEC_INVERTERS_URL, file)
      if (!result.status.isSuccess) {
        error(s"Downloading CEC inverters failed.")
      }
      else {
        /*
         * Read *.csv file as [DataFrame] and remove SAM
         * specification rows
         */
        val dataframe =
          session.read
            .option("header", value = true)
            .csv(file)
            .filter(col("Name") =!= "Units" && col("Name") =!= "[0]")
        /*
         * Register dataframe as global table
         */
        dataframe.createOrReplaceGlobalTempView("CEC_INVERTERS")

      }

    }
    else {
      /*
       * Read *.csv file as [DataFrame] and remove SAM
       * specification rows
       */
      val dataframe =
        session.read
          .option("header", value = true)
          .csv(file)
          .filter(col("Name") =!= "Units" && col("Name") =!= "[0]")
      /*
       * Register dataframe as global table
       */
      dataframe.createOrReplaceGlobalTempView("CEC_INVERTERS")

    }

  }

  private def modulesAsView(session:SparkSession, file:String, replace:Boolean):Unit = {

    if (replace) {

      Files.delete(Paths.get(file))

      val result = download(CEC_MODULES_URL, file)
      if (!result.status.isSuccess) {
        error(s"Downloading CEC modules failed.")
      }
      else {
        /*
         * Read *.csv file as [DataFrame] and remove SAM
         * specification rows
         */
        val dataframe =
          session.read
            .option("header", value = true)
            .csv(file)
            .filter(col("Name") =!= "Units" && col("Name") =!= "[0]")
        /*
         * Register dataframe as global table
         */
        dataframe.createOrReplaceGlobalTempView("CEC_MODULES")

      }

    }
    else {
      /*
       * Read *.csv file as [DataFrame] and remove SAM
       * specification rows
       */
      val dataframe =
        session.read
          .option("header", value = true)
          .csv(file)
          .filter(col("Name") =!= "Units" && col("Name") =!= "[0]")
      /*
       * Register dataframe as global table
       */
      dataframe.createOrReplaceGlobalTempView("CEC_MODULES")

    }

  }
}
