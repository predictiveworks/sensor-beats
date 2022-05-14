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

import org.ta4j.core._
import org.ta4j.core.num._

class OHLCIndicator(val series:BarSeries, label:String) extends Indicator[Num] {
  /**
   * This indicator supports OHLC and NON-OHLC compliant
   * datasets; the current implementation maps prices
   * (labels) that are unknown to the close price
   */
  override def getValue(index:Int):Num = {

    val bar = series.getBar(index)
    label match {
      case "open"  => bar.getOpenPrice
      case "high"  => bar.getHighPrice
      case "low"   => bar.getLowPrice
      case "close" => bar.getClosePrice
      case _ => bar.getClosePrice
    }

  }

  def getBarSeries: BarSeries = series

  def numOf(number:Number):Num = {
    throw new Exception("Not implemented")
  }

}

object OHLCHelper {

  def apply(series:BarSeries, label:String): OHLCIndicator = {
    new OHLCIndicator(series, label)
  }

}