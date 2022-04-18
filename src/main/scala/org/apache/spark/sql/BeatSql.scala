package org.apache.spark.sql

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
import org.apache.spark.sql.catalyst.analysis.{UnresolvedAttribute, UnresolvedRelation}
import org.apache.spark.sql.catalyst.expressions.{And, EqualTo, Expression, GreaterThan, LessThan, Literal, Or}
import org.apache.spark.sql.catalyst.plans.logical.Filter

class BeatSql(session:SparkSession, logger:Logger) {

  private var table:String = _
  private var columns:Seq[String] = _

  private var condition:JsonObject = _

  def toJson:JsonElement = {

    if (table == null) return JsonNull.INSTANCE

    val obj = new JsonObject
    obj.addProperty("table", table)

    if (columns == null) return JsonNull.INSTANCE

    val output = new JsonArray
    columns.foreach(column => output.add(column))

    obj.add("output", output)

    if (condition != null)
      obj.add("condition", condition)

    obj

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
        columns = plan.output.map(attr => attr.name)

      } catch {
        case _:Throwable => columns = Seq("*")
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
        val message = s"[BeatSql] The provided SQL statement '$sql' cannot be resolved. Validation failed with: ${t.getLocalizedMessage}"
        logger.error(message)

        throw new IllegalArgumentException(message)
    }

    toJson

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
        val attr = left.asInstanceOf[UnresolvedAttribute]
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

  private def buildGreaterThan(operation:GreaterThan):JsonObject = {

    val obj = new JsonObject
    operation.left match {
      case left: UnresolvedAttribute =>
        val attr = left.asInstanceOf[UnresolvedAttribute]
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
        val attr = left.asInstanceOf[UnresolvedAttribute]
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
