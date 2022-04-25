package de.kp.works.beats.sensor.dl

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

import akka.stream.scaladsl.SourceQueueWithComplete
import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.BeatFrame
import de.kp.works.beats.sensor.publish.Publisher
import org.apache.spark.sql.{DataFrame, SparkSession}

abstract class BeatWorker(
  queue:SourceQueueWithComplete[String],
  session:SparkSession,
  logger:Logger) {
  /**
   * Time management
   */
  protected var startts:Long = -1

  protected var currts:Long = -1
  protected var nextts:Long = -1
  /**
   * [DataFrame] interface of RocksDB
   */
  protected val beatFrame = new BeatFrame(session, logger)
  /**
   * SSE queue publisher
   */
  protected val publisher: Publisher = Publisher.getInstance(queue)

  protected var verbose = true

  def setVerbose(verbose:Boolean):Unit = {
    this.verbose = verbose
  }

  def execute(jid:String, table:String, start:Long, end:Long):Unit

  def loadDataset(table:String, start:Long, end:Long):DataFrame = {

    if (start == 0L || end == 0L)
      beatFrame.readAll(table)

    else
      beatFrame.readRange(table, start, end)

  }

}
