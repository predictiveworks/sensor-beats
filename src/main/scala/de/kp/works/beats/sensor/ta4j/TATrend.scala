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

import com.google.gson.{JsonArray, JsonObject}
import com.typesafe.config.{Config, ConfigFactory, ConfigObject}
import de.kp.works.beats.sensor.BeatDot
import org.ta4j.core.BarSeries

import scala.collection.mutable

abstract class TATrend extends TACompute with IndicatorBuilder {
  /**
   * The following indicators compute moving averages
   * and can be used to smooth the sensor timeseries'.
   *
   * All these indicators need just two parameters,
   * the `timeframe` and the respective value
   */
  val indicators = Seq(
    "DoubleEMAIndicator",
    "EMAIndicator",
    "HMAIndicator",
    "LWMAIndicator",
    "MMAIndicator",
    "SMAIndicator",
    "TripleEMAIndicator",
    "WMAIndicator",
    "ZLEMAIndicator")

  def analyze(values:Seq[BeatDot], indicator:String, timeframe:Int=5):List[BeatDot] = {
    /*
     * STEP #1: Validate whether the provided indicator
     * is supported in the `SensorBeat` context
     */
    if (!indicators.contains(indicator))
      throw new Exception(s"The indicator `$indicator` is not supported.")
    /*
     * STEP #2: Transform the sensor beat
     * timeseries into a TA4J [BarSeries]
     */
    val taSeries = TATransform.toSeries(values)
    /*
     * STEP #3: Build indicator model from
     * provided indicator name
     */
    val modelConf = buildModel(indicator, timeframe).getConfig("model")
    val (_, technicals) = config2Indicators(modelConf, taSeries)
    /*
     * STEP #4: Apply the provided indicator
     * and build a smoothed timeseries
     */
    val technical = technicals.head
    /*
     * IMPORTANT: The computation of the [BarSeries]
     * must skip the latest value of the time series.
     *
     * Therefore, the series [init] must be used here
     */
    val analyzed = values.init.indices.map(i => {

      val value = values(i)
      val taValue = getValue(technical, i)

      value.copy(value = taValue)

    })

    analyzed.toList

  }

  private def buildModel(indicator:String, timeframe:Int):Config = {

    val jsonIndicator = new JsonObject()
    jsonIndicator.addProperty("name", indicator)
    /*
     * The set of supported technical indicators
     * leverage single timeframe (= barCount) for
     * timeseries smoothing
     */
    val jsonParams = new JsonObject
    jsonParams.addProperty("barCount", timeframe)

    jsonIndicator.add("params", jsonParams)

    val src_col:String = "value"
    val dst_col:String = "analysis"

    jsonIndicator.addProperty("src_col", src_col)
    jsonIndicator.addProperty("dst_col", dst_col)
    /*
     * The current implementation supports multiple
     * indicators; for smoothing sensor timeseries
     * data, however, a single indicator is used
     */
    val jsonIndicators = new JsonArray()
    jsonIndicators.add(jsonIndicator)
    /*
     * Add `indicators` to JSON model
     */
    val jsonModel = new JsonObject()
    jsonModel.add("indicators", jsonIndicators)
    /*
     * Finally transform JSON configuration
     * into respective [Config]
     */
    val jsonConfig = new JsonObject()
    jsonConfig.add("model", jsonModel)

    val spec = jsonConfig.toString
    ConfigFactory.parseString(spec)

  }

  private def config2Indicators(modelConf:Config, series:BarSeries):(Array[String], Array[Any]) = {

    val columns    = mutable.ArrayBuffer.empty[String]
    val technicals = mutable.ArrayBuffer.empty[Any]

    val indicators = try {
      modelConf.getList("indicators")

    } catch {
      case _:Throwable =>
        val message = s"The model indicators are not specified."
        throw new Exception(message)
    }

    val size = indicators.size
    for (i <- 0 until size) {

      val cval = indicators.get(i)
      cval match {
        case configObject: ConfigObject =>

          val indicatorConf = configObject.toConfig
          val (column, technical) = config2Indicator(indicatorConf, series)

          columns += column
          technicals += technical

        case _ =>
          throw new Exception(s"Indicator $i is not specified as configuration object.")
      }

    }

    (columns.toArray, technicals.toArray)

  }

}
