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

import ch.qos.logback.classic.Logger
import com.google.gson.{JsonArray, JsonElement, JsonNull, JsonObject}
import de.kp.works.beats.sensor.BeatAttrs.{TIME, VALUE}
import de.kp.works.beats.sensor.ta4j.TATrend
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.analysis.{UnresolvedAttribute, UnresolvedRelation}
import org.apache.spark.sql.catalyst.expressions.{And, EqualTo, Expression, GreaterThan, LessThan, Literal, Or}
import org.apache.spark.sql.catalyst.plans.logical.Filter

import scala.collection.JavaConversions.{asScalaSet, iterableAsScalaIterable}

trait SingleFilter[T] {

  def filter(item: T, expression: JsonObject): Boolean =
    expressionFilter(item, expression)

  private def expressionFilter(item: T, expression: JsonObject): Boolean = {

    val operation = expression.keySet().head
    val filter = expression.get(operation).getAsJsonObject

    operation match {
      /*
       * Basic operation that holds a single
       * filter condition for the `value`
       */
      case "EqualTo" =>
        equalToFilter(item, filter)

      case "GreaterThan" =>
        greaterThanFilter(item, filter)

      case "LessThan" =>
        lessThanFilter(item, filter)
      /*
       * Complex operation that holds at least 2 filter
       * conditions for the `value` column
       */
      case "AND" =>
        andFilter(item, filter)

      case "OR" =>
        orFilter(item, filter)

      case _ =>
        throw new Exception("SQL query contains unknown operation.")
    }

  }

  private def andFilter(item: T, expression: JsonObject): Boolean = {
    /*
     * Extract `left` condition and transform
     * into a filter condition
     */
    val left = expression.get("left").getAsJsonObject
    val leftFilter = expressionFilter(item, left)
    /*
     * Extract `right` condition and transform
     * into a filter condition
     */
    val right = expression.get("right").getAsJsonObject
    val rightFilter = expressionFilter(item, right)
    /*
     * Combine left & right filter conditions
     */
    leftFilter & rightFilter

  }

  private def orFilter(item: T, expression: JsonObject): Boolean = {
    /*
     * Extract `left` condition and transform
     * into a filter condition
     */
    val left = expression.get("left").getAsJsonObject
    val leftFilter = expressionFilter(item, left)
    /*
     * Extract `right` condition and transform
     * into a filter condition
     */
    val right = expression.get("right").getAsJsonObject
    val rightFilter = expressionFilter(item, right)
    /*
     * Combine left & right filter conditions
     */
    leftFilter | rightFilter

  }

  private def equalToFilter(item: T, expression: JsonObject): Boolean = {
    /*
     * The `value` format is [Double], therefore
     * the respective literal is casted automatically
     */
    item match {
      case d: Double =>
        val literal = expression.get("value").getAsString.toDouble
        d == literal

      case l: Long =>
        val literal = expression.get("value").getAsString.toLong
        l == literal

      case _ =>
        throw new Exception("Data type must be Double or Long")
    }

  }

  private def greaterThanFilter(item: T, expression: JsonObject): Boolean = {
    /*
     * The `value` format is [Double], therefore
     * the respective literal is casted automatically
     */
    item match {
      case d: Double =>
        val literal = expression.get("value").getAsString.toDouble
        d > literal

      case l: Long =>
        val literal = expression.get("value").getAsString.toLong
        l > literal

      case _ =>
        throw new Exception("Data type must be Double or Long")
    }
  }

  private def lessThanFilter(item: T, expression: JsonObject): Boolean = {
    /*
     * The `value` format is [Double], therefore
     * the respective literal is casted automatically
     */
    item match {
      case d: Double =>
        val literal = expression.get("value").getAsString.toDouble
        d < literal

      case l: Long =>
        val literal = expression.get("value").getAsString.toLong
        l < literal

      case _ =>
        throw new Exception("Data type must be Double or Long")
    }
  }

}

object TimeFilter extends SingleFilter[Long]

object ValueFilter extends SingleFilter[Double]

object TimeValueFilter {

  def filter(time: Long, value: Double, expression: JsonObject): Boolean =
    expressionFilter(time, value, expression)

  private def expressionFilter(time: Long, value: Double, expression: JsonObject): Boolean = {

    val operation = expression.keySet().head
    val filter = expression.get(operation).getAsJsonObject

    operation match {
      /*
       * Basic operation that holds a single
       * filter condition for the `time` and
       * `value` column
       */
      case "EqualTo" =>
        equalToFilter(time, value, filter)

      case "GreaterThan" =>
        greaterThanFilter(time, value, filter)

      case "LessThan" =>
        lessThanFilter(time, value, filter)

      /*
       * Complex operation that holds at least 2 filter
       * conditions for the `time` and `value` column
       */
      case "AND" =>
        andFilter(time, value, filter)

      case "OR" =>
        orFilter(time, value, filter)

      case _ =>
        throw new Exception("SQL query contains unknown operation.")
    }

  }

  private def andFilter(time: Long, value: Double, expression: JsonObject): Boolean = {
    /*
     * Extract `left` condition and transform
     * into a filter condition
     */
    val left = expression.get("left").getAsJsonObject
    val leftFilter = expressionFilter(time, value, left)
    /*
     * Extract `right` condition and transform
     * into a filter condition
     */
    val right = expression.get("right").getAsJsonObject
    val rightFilter = expressionFilter(time, value, right)
    /*
     * Combine left & right filter conditions
     */
    leftFilter & rightFilter

  }

  private def orFilter(time: Long, value: Double, expression: JsonObject): Boolean = {
    /*
     * Extract `left` condition and transform
     * into a filter condition
     */
    val left = expression.get("left").getAsJsonObject
    val leftFilter = expressionFilter(time, value, left)
    /*
     * Extract `right` condition and transform
     * into a filter condition
     */
    val right = expression.get("right").getAsJsonObject
    val rightFilter = expressionFilter(time, value, right)
    /*
     * Combine left & right filter conditions
     */
    leftFilter | rightFilter


  }

  private def equalToFilter(time: Long, value: Double, expression: JsonObject): Boolean = {

    val attrName = expression.get("name").getAsString
    BeatAttrs.withName(attrName) match {
      case TIME =>
        val literal = expression.get("value").getAsString.toLong
        time == literal

      case VALUE =>
        val literal = expression.get("value").getAsString.toDouble
        value == literal

      case _ =>
        throw new Exception("Attribute name is unknown.")

    }

  }

  private def greaterThanFilter(time: Long, value: Double, expression: JsonObject): Boolean = {

    val attrName = expression.get("name").getAsString
    BeatAttrs.withName(attrName) match {
      case TIME =>
        val literal = expression.get("value").getAsString.toLong
        time > literal

      case VALUE =>
        val literal = expression.get("value").getAsString.toDouble
        value > literal

      case _ =>
        throw new Exception("Attribute name is unknown.")

    }

  }

  private def lessThanFilter(time: Long, value: Double, expression: JsonObject): Boolean = {

    val attrName = expression.get("name").getAsString
    BeatAttrs.withName(attrName) match {
      case TIME =>
        val literal = expression.get("value").getAsString.toLong
        time < literal

      case VALUE =>
        val literal = expression.get("value").getAsString.toDouble
        value < literal

      case _ =>
        throw new Exception("Attribute name is unknown.")

    }

  }

}
/**
 * Validating the SQL statement is performed
 * using the Apache Spark SqlParser, which is
 * invoked via a Spark session
 */
class BeatSql(session:SparkSession, logger:Logger) extends TATrend {

  private var table:String = _
  private var output:Seq[String] = _
  /**
   * The columns that are part of the
   * SQL where clause (condition)
   */
  private var condition:JsonObject = _
  private var columns:Seq[String] = _

  private val emptyResponse = new JsonArray
  /**
   * At this stage, the RocksDB must be initialized
   * already, therefore no `options` are provided
   */
  private val rocksApi = BeatRocksApi.getInstance

  def toArray: JsonArray = {

    val response = new JsonArray
    /*
     * Return the full range of available dots
     * from the specified `table`
     */
    rocksApi.scan(table).foreach { case (time, value) =>

      val dot = new JsonObject
      dot.addProperty("time", time)
      dot.addProperty("value", value.toDouble)

      response.add(dot)

    }

    response

  }

  def toDots:Seq[BeatDot] = {
    rocksApi.scan(table)
      .map { case (time, value) => BeatDot(time, value.toDouble) }
  }

  def toJson:JsonElement = {
    /*
     * Extract table
     */
    if (table == null) return JsonNull.INSTANCE

    val obj = new JsonObject
    obj.addProperty("table", table)
    /*
     * Extract output columns
     */
    if (output == null) return JsonNull.INSTANCE

    val jOutput = new JsonArray
    output.foreach(jOutput.add)

    obj.add("output", jOutput)
    /*
     * Extract condition & condition
     * columns
     */
    if (condition != null) {
      /*
       * Subsequent processing leverages the
       * number of distinct columns to make
       * decisions about row filtering
       */
      val jColumns = new JsonArray
      columns.distinct.foreach(jColumns.add)

      obj.add("columns", jColumns)
      obj.add("condition", condition)
    }
    else {
      obj.add("columns", JsonNull.INSTANCE)
      obj.add("condition", JsonNull.INSTANCE)
    }

    obj

  }

  def toRows:Seq[(Long,Double)] = {
    rocksApi.scan(table)
      .map { case (time, value) => (time, value.toDouble) }
  }

  def toSqlDots:Seq[BeatDot] = {
    toSqlRows
      .map { case (time, value) => BeatDot(time, value) }
  }

  def toSqlRows:Seq[(Long,Double)] = {
    /*
     * Reminder: The (filter) condition is a JsonObject
     * and is defined as a tree with left & right nodes
     */
    val expression = condition
    /*
     * Check whether one or more than one columns
     * are referenced
     */
    val rows = toRows
    val sqlRows = if (columns.size == 1) {

      BeatAttrs.withName(columns.head) match {
        case TIME =>
          rows.filter { case (time, _) =>
            TimeFilter.filter(time, expression)
          }

        case VALUE =>
          rows.filter { case (_, value) =>
            ValueFilter.filter(value, expression)
          }

        case _ => rows

      }

    } else {
      rows.filter { case (time, value) =>
        TimeValueFilter.filter(time, value, expression)
      }

    }

    sqlRows

  }

  def read(sql:String):String = {

    clear()
    /*
     * STEP #1: Validate & extract provided SQL
     * statement; [BeatSql] throws an invalid
     * argument exception if something went wrong
     */
    val json = parse(sql)
    if (!isValid(json))
      return emptyResponse.toString
    /*
     * STEP #2: Map SQL statement onto RockDB
     * commands
     */
    var response = new JsonArray
    if (condition.isJsonNull) {

      response = toArray
      response.toString

    } else {

      val response = new JsonArray
      toSqlRows.foreach { case (time, value) =>
        val jo = new JsonObject
        jo.addProperty("time", time)
        jo.addProperty("value", value)

        response.add(jo)
      }

      response.toString

    }

  }

  def parse(sql:String):JsonElement = {

    try {

      val plan = session.sessionState.sqlParser.parsePlan(sql)
      if (plan == null)
        throw new Exception("Could not build a valid computation plan from SQL statement.")

      /*
       * STEP #1: Retrieve table name
       */
      val tables = plan.collectLeaves()
        .map {
          case relation: UnresolvedRelation =>
            relation.tableName
          case _ => ""
        }
        .filter(table => table.nonEmpty)

      if (tables.size != 1) {
        val message = s"Please configure a SQL statement with a single table name. Found: ${tables.size}"
        throw new IllegalArgumentException(message)
      }
      table = tables.head
      /*
       * STEP #2: Retrieve column names
       */
      try {
        output = plan.output.map(attr => attr.name)

      } catch {
        case _:Throwable => output = Seq("*")
      }
      /*
       * STEP #3: Retrieve where clause; note, the
       * current implementation supports a restricted
       * filter condition
       */
      plan.children.foreach {
        case filter: Filter =>
          condition = buildExpression(filter.condition)
        case _ =>
      }

    } catch {
      case t:Throwable =>
        val message = s"[BeatSql] The provided SQL statement '$sql' cannot be resolved: ${t.getLocalizedMessage}"
        logger.error(message)

        throw new IllegalArgumentException(message)
    }

    toJson

  }

  def trend(sql:String, indicator:String, timeframe:Int=5):String = {

    clear()
    /*
     * STEP #1: Validate & extract provided SQL
     * statement; [BeatSql] throws an invalid
     * argument exception if something went wrong
     */
    val json = parse(sql)
    if (!isValid(json))
      return emptyResponse.toString
    /*
     * STEP #2: Map SQL statement onto RockDB
     * commands and compute dots
     */
    val dots =
      if (condition.isJsonNull) toDots else toSqlDots
    /*
     * STEP #3: Compute trend with provided technical
     * indicator
     */
    val trend = analyze(dots, indicator, timeframe)

    val response = new JsonArray
    trend.foreach { dot =>
      val jo = new JsonObject
      jo.addProperty("time", dot.time)
      jo.addProperty("value", dot.value)

      response.add(jo)
    }

    response.toString

  }

  def isValid(json:JsonElement):Boolean = {

    if (json.isJsonNull) {
      val message = s"Provided SQL statement is not complete."
      logger.warn(message)

      return false
    }
    /*
     * Check whether the extracted table, columns and
     * (optional) conditions refer to an existing database
     * specification
     */
    val obj = json.getAsJsonObject

    val sqlTable  = obj.get("table").getAsString
    checkTable(sqlTable)

    table = sqlTable

    val sqlOutput = obj.get("output").getAsJsonArray
      .map(_.getAsString).toSeq

    checkOutput(sqlOutput)

    output = sqlOutput
    condition = obj.get("condition").getAsJsonObject

    if (!obj.get("columns").isJsonNull)
      columns = obj.get("columns").getAsJsonArray
        .map(_.getAsString).toSeq

    true

  }

  /**
   * Public method to validate whether the
   * output (selected fields) reference
   * valid Beat attributes
   */
  def checkOutput(output:Seq[String]):Unit = {

    if (output.head == "*") {
      /* Do nothing */

    } else {
      output.foreach(checkColumn)

    }

  }
  /**
   * Helper method to check whether a certain
   * column refers to an existing Beat attribute.
   */
  private def checkColumn(column:String):Unit = {

    try {
      BeatAttrs.withName(column)

    } catch {
      case _:Throwable =>
        val message = s"Unknown column `$column` detected."
        logger.error(message)

        new IllegalArgumentException(message)
    }

  }

  private def checkTable(table:String):Unit = {

    if (!rocksApi.hasTable(table)) {
      val message = s"Unknown table `$table` detected."
      logger.error(message)

      new IllegalArgumentException(message)

    }

  }
  /**
   * Private helper method to reset the metadata
   * extracted from the SQL query.
   */
  private def clear():Unit = {

    table = null
    output = null

    columns = null
    condition = null

  }

  private def buildExpression(expression:Expression):JsonObject = {

    val obj = new JsonObject
    expression match {
      /*******************
       * BASIC OPERATIONS
       */
      case operation: EqualTo =>
        obj.add("EqualTo", buildEqualTo(operation))

      case operation: GreaterThan =>
        obj.add("GreaterThan", buildGreaterThan(operation))

      case operation: LessThan =>
        obj.add("LessThan", buildLessThan(operation))

      /*********************
       * COMPLEX OPERATIONS
       */
      case operation: And =>

        val andObj = new JsonObject
        /*
         * The left part of an `And` operation
         */
        andObj.add("left",
          buildExpression(operation.left))
        /*
         * The right part of an `And` operation
         */
        andObj.add("right",
          buildExpression(operation.right))

        obj.add("AND", andObj)

      case operation: Or =>

        val orObj = new JsonObject
        /*
         * The left part of an `Or` operation
         */
        orObj.add("left",
          buildExpression(operation.left))
        /*
         * The right part of an `And` operation
         */
        orObj.add("right",
          buildExpression(operation.right))

        obj.add("OR", orObj)

      case _ => /* Do nothing */
    }

    obj

  }

  private def buildEqualTo(operation:EqualTo):JsonObject = {

    val obj = new JsonObject
    operation.left match {
      case left: UnresolvedAttribute =>
        /*
         * Extract attribute name
         */
        val attr = left.asInstanceOf[UnresolvedAttribute]
        val attrName = attr.name

        if (columns == null) columns = Seq.empty[String]
        columns = columns ++ Seq(attrName)

        obj.addProperty("name", attrName)
      case _ =>
        throw new Exception("Unknown filter condition detected.")
    }
    operation.right match {
      case right: Literal =>
        val value = right.asInstanceOf[Literal]
        obj.addProperty("type", value.dataType.simpleString)
        obj.addProperty("value", value.value.toString)

      case _ =>
        throw new Exception("Unknown filter condition detected.")

    }

    obj

  }

  private def buildGreaterThan(operation:GreaterThan):JsonObject = {

    val obj = new JsonObject
    operation.left match {
      case left: UnresolvedAttribute =>
        /*
         * Extract attribute name
         */
        val attr = left.asInstanceOf[UnresolvedAttribute]
        val attrName = attr.name

        if (columns == null) columns = Seq.empty[String]
        columns = columns ++ Seq(attrName)

        obj.addProperty("name", attr.name)
      case _ =>
        throw new Exception("Unknown filter condition detected.")
    }
    operation.right match {
      case right: Literal =>
        val value = right.asInstanceOf[Literal]
        obj.addProperty("type", value.dataType.simpleString)
        obj.addProperty("value", value.value.toString)

      case _ =>
        throw new Exception("Unknown filter condition detected.")

    }

    obj

  }

  private def buildLessThan(operation:LessThan):JsonObject = {

    val obj = new JsonObject
    operation.left match {
      case left: UnresolvedAttribute =>
        /*
         * Extract attribute name
         */
        val attr = left.asInstanceOf[UnresolvedAttribute]
        val attrName = attr.name

        if (columns == null) columns = Seq.empty[String]
        columns = columns ++ Seq(attrName)

        obj.addProperty("name", attr.name)
      case _ =>
        throw new Exception("Unknown filter condition detected.")
    }
    operation.right match {
      case right: Literal =>
        val value = right.asInstanceOf[Literal]
        obj.addProperty("type", value.dataType.simpleString)
        obj.addProperty("value", value.value.toString)

      case _ =>
        throw new Exception("Unknown filter condition detected.")

    }

    obj

  }

}
