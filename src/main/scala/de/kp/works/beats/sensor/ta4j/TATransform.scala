package de.kp.works.beats.sensor.ta4j

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

import de.kp.works.beats.sensor.BeatDot
import org.ta4j.core.num.DoubleNum
import org.ta4j.core.{BarSeries, BaseBar, BaseBarSeriesBuilder}

import java.time.{Duration, Instant, ZoneId, ZonedDateTime}

object TATransform extends TACompute {

  private val ZONE_ID = ZoneId.systemDefault()
  /**
   * This is an adapted OHLC [BarSeries] generation, where every
   * open, low, high, close price is set equal and to the provided
   * one
   */
  def toSeries(input:List[BeatDot]):BarSeries = {

    val series:BarSeries = new BaseBarSeriesBuilder()
      .withName("TimeSeries")
      .withNumTypeOf(classOf[DoubleNum])
      .build

    input.zip(input.tail).foreach{case(curr, next) =>

      val duration = Duration.ofMillis(next.time - curr.time)
      val endtime = {
        val starttime = millis2ZonedDateTime(curr.time)
        starttime.plus(duration)
      }

      val open   = DoubleNum.valueOf(curr.value)
      val close  = DoubleNum.valueOf(curr.value)
      val high   = DoubleNum.valueOf(curr.value)
      val low    = DoubleNum.valueOf(curr.value)
      val volume = DoubleNum.valueOf(curr.value)

      val bar = BaseBar.builder()
        .endTime(endtime)
        .timePeriod(duration)
        .openPrice(open)
        .closePrice(close)
        .highPrice(high)
        .lowPrice(low)
        .volume(volume)
        .build

      series.addBar(bar)

    }

    series

  }
  /**
   * A helper method to transform a timestamp that
   * defines epoch milliseconds into a zoned date
   * time
   */
  private def millis2ZonedDateTime(millis:Long):ZonedDateTime = {
    val instant = Instant.ofEpochMilli(millis)
    instant.atZone(ZONE_ID)
  }

}
