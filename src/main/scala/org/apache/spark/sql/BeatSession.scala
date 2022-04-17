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

import ch.qos.logback.classic.Level
import com.intel.analytics.zoo.common.NNContext
import org.apache.spark.SparkConf
import org.slf4j.LoggerFactory

/**
 * This session integrates the plain Apache Spark SQL Session
 * and the `NNContext` from Analytics Zoo
 */
object BeatSession {

  private var session:Option[SparkSession] = None

  def initialize(): Unit = {
    /*
     * Set log levels for Apache Spark console output programmatically.
     * Steps taken prior to the following ones
     *
     * - Exclude log4j support from Apache Spark (see pom.xml)
     * - Redirect log4j to slf4j
     * - Set logback
     */
    val entries = Seq(
      "com.intel.analytics.bigdl",
      "io.netty",
      "org.apache.hadoop",
      "org.apache.spark",
      "org.spark_project")

    entries.foreach(entry => {
      val logger = LoggerFactory
        .getLogger(entry).asInstanceOf[ch.qos.logback.classic.Logger]

      logger.setLevel(Level.WARN)

    })
    /*
     * STEP #1: Build the global Apache Spark Configuration
     */
    val conf = new SparkConf()
      .setAppName("SensorBeat")
      .setMaster("local[*]")
      /*
       * Driver & executor configuration; note, this should be
       * enough for `SensorBeat` requirements
       */
      .set("spark.driver.maxResultSize", "4g")
      .set("spark.driver.memory",        "12g")
      .set("spark.executor.memory",      "12g")
      /*
       * BigDL specific configuration, extracted from spark-bigdl.conf  :
       * ----------------------------
       * spark.shuffle.reduceLocality.enabled                false
       * spark.shuffle.blockTransferService                  nio
       * spark.scheduler.minRegisteredResourcesRatio         1.0
       * spark.speculation                                   false
       */
      .set("spark.shuffle.reduceLocality.enabled", "false")
      .set("spark.shuffle.blockTransferService", "nio")
      .set("spark.scheduler.minRegisteredResourcesRatio", "1.0")
      .set("spark.speculation", "false")

    /*
     * STEP #2: Leverage the configuration to create an
     * Analytics Zoo compliant Apache Spark Context
     */
    val sc = NNContext.initNNContext(conf)

    /*
     * STEP #3: Leverage the Apache Spark Context and
     * create the respective Session
     */
    val spark = new SparkSession(sc)
    spark.sparkContext.setLogLevel("ERROR")

    session = Some(spark)

  }

  def getSession: SparkSession = {
    if (session.isEmpty) initialize()
    session.get
  }

}
