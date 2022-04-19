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

import com.typesafe.config.Config

import org.ta4j.core._

import org.ta4j.core.indicators._
import org.ta4j.core.indicators.adx._
import org.ta4j.core.indicators.bollinger._
import org.ta4j.core.indicators.candles._
import org.ta4j.core.indicators.helpers._
import org.ta4j.core.indicators.ichimoku._
import org.ta4j.core.indicators.keltner._
import org.ta4j.core.indicators.pivotpoints._
import org.ta4j.core.indicators.statistics._
import org.ta4j.core.indicators.volume._

import org.ta4j.core.num._

trait IndicatorBuilder extends SpecBuilder {

  /**
   * This is a helper method to build a certain technical
   * indicator from a provided configuration.
   */
  def config2Indicator(indicatorConf:Config, series:BarSeries):(String, Any) = {

    val dst_col = indicatorConf.getString("dst_col")
    val params = indicatorConf.getConfig("params")

    val name = indicatorConf.getString("name")
    val technical = name match {

      case "AccumulationDistributionIndicator" =>
        /*
         * AD Indicator
         */
        new AccumulationDistributionIndicator(series)

      case "ADXIndicator" =>
        /*
         * ADX Indicator.
         */
        val barCount = params.getInt("barCount")
        new ADXIndicator(series, barCount)

      case "AroonOscillatorIndicator" =>
        /*
         * Aroon Oscillator.
         */
        val barCount = params.getInt("barCount")
        new AroonOscillatorIndicator(series, barCount)

      case "AroonDownIndicator" =>
        /*
         * Arron Low Price Indicator
         */
        val barCount = params.getInt("barCount")
        new AroonDownIndicator(series, barCount)

      case "AroonUpIndicator" =>
        /*
         * Arron High Price Indicator
         */
        val barCount = params.getInt("barCount")
        new AroonUpIndicator(series, barCount)

      case "ATRIndicator" =>
        /*
         * Average True Range indicator. This indicator depends
         * on the [[TRIndicator]], which leverages `high` and
         * `low` price
         */
        val barCount = params.getInt("barCount")
        new ATRIndicator(series, barCount)

      case "AwesomeOscillatorIndicator" =>
        /*
         * Awesome oscillator indicator. This indicator depends
         * on [[MedianPriceIndicator]], which uses `high`
         * and `low` price.
         */
        val barCountSma1 = getAsInt(params, "barCountSma1", 5)
        val barCountSma2 = getAsInt(params, "barCountSma2", 34)

        val medianPriceIndicator = new MedianPriceIndicator(series)
        new AwesomeOscillatorIndicator(medianPriceIndicator, barCountSma1, barCountSma2)

      case "AccelerationDecelerationIndicator" =>
        /*
         * Acceleration-Deceleration indicator. This indicator
         * depends on [[MedianPriceIndicator]], which uses `high`
         * and `low` price.
         */
        val barCountSma1 = getAsInt(params, "barCountSma1", 5)
        val barCountSma2 = getAsInt(params, "barCountSma2", 34)

        new AccelerationDecelerationIndicator(series, barCountSma1, barCountSma2)

      case "BearishEngulfingIndicator" =>
        /*
         * Bearish Engulfing pattern indicator.
         */
        new BearishEngulfingIndicator(series)

      case "BearishHaramiIndicator" =>
        /*
         * Bearish Harami pattern indicator.
         */
        new BearishHaramiIndicator(series)

      case "BollingerBandWidthIndicator" =>
        /*
         * Bollinger BandWidth indicator.
         *
         * The source dataset for the technical indicator, depends
         * on `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        val smaIndicator = new SMAIndicator(indicators, barCount)
        /*
         * The middle band indicator is typically built from
         * an SMA indicator
         */
        val middleBandIndicator = new BollingerBandsMiddleIndicator(smaIndicator)
        /*
         * The deviation above and below the middle, factored by k.
         * Typically a StandardDeviationIndicator is used.
         */
        val deviationIndicator = new StandardDeviationIndicator(indicators, barCount)

        val k = getAsDouble(params, "k", 2.0)

        val lowerBandIndicator = new BollingerBandsLowerIndicator(middleBandIndicator, deviationIndicator, DoubleNum.valueOf(k))
        val upperBandIndicator = new BollingerBandsUpperIndicator(middleBandIndicator, deviationIndicator, DoubleNum.valueOf(k))

        new BollingerBandWidthIndicator(upperBandIndicator, middleBandIndicator, lowerBandIndicator)

      case "BollingerBandsLowerIndicator" =>
        /*
         * The Bollinger Bands Lower indicator.
         *
         * The source dataset for the technical indicator, depends
         * on `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        val smaIndicator = new SMAIndicator(indicators, barCount)
        /*
         * The middle band indicator is typically built from
         * an SMA indicator
         */
        val middleBandIndicator = new BollingerBandsMiddleIndicator(smaIndicator)
        /*
         * The deviation above and below the middle, factored by k.
         * Typically a StandardDeviationIndicator is used.
         */
        val deviationIndicator = new StandardDeviationIndicator(indicators, barCount)

        val k = getAsDouble(params, "k", 2.0)
        new BollingerBandsLowerIndicator(middleBandIndicator, deviationIndicator, DoubleNum.valueOf(k))

      case "BollingerBandsMiddleIndicator" =>
        /*
         *
         * Bollinger Bands Middle Indicator.
         *
         * Buy - Occurs when the price line crosses from below
         * to above the Lower Bollinger Band.
         *
         * Sell - Occurs when the price line crosses from above
         * to below the Upper Bollinger Band.
         *
         * The source dataset for the technical indicator, depends
         * on `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        val smaIndicator = new SMAIndicator(indicators, barCount)
        /*
         * The middle band indicator is typically built from
         * an SMA indicator
         */
        new BollingerBandsMiddleIndicator(smaIndicator)

      case "BollingerBandsUpperIndicator" =>
        /*
         * The Bollinger Bands Upper indicator.
         *
         * The source dataset for the technical indicator, depends
         * on `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        val smaIndicator = new SMAIndicator(indicators, barCount)
        /*
         * The middle band indicator is typically built from
         * an SMA indicator
         */
        val middleBandIndicator = new BollingerBandsMiddleIndicator(smaIndicator)
        /*
         * The deviation above and below the middle, factored by k.
         * Typically a StandardDeviationIndicator is used.
         */
        val deviationIndicator = new StandardDeviationIndicator(indicators, barCount)

        val k = getAsDouble(params, "k", 2.0)
        new BollingerBandsUpperIndicator(middleBandIndicator, deviationIndicator, DoubleNum.valueOf(k))

      case "BullishEngulfingIndicator" =>
        /*
         * Bullish Engulfing pattern indicator.
         */
        new BullishEngulfingIndicator(series)

      case "BullishHaramiIndicator" =>
        /*
         * Bullish Harami pattern indicator.
         */
        new BullishHaramiIndicator(series)

      case "CCIIndicator" =>
        /*
         * Commodity Channel Index (CCI) indicator.
         */
        val barCount = getAsInt(params, "barCount", 20)
        new CCIIndicator(series, barCount)

      case "ChaikinMoneyFlowIndicator" =>
        /*
         * Chaikin Money Flow (CMF) indicator.
         */
        val barCount = params.getInt("barCount")
        new ChaikinMoneyFlowIndicator(series, barCount)

      case "ChaikinOscillatorIndicator" =>
        /*
         * Chaikin Oscillator.
         */
        val shortBarCount = getAsInt(params, "shortBarCount", 3)
        val longBarCount = getAsInt(params, "longBarCount", 10)

        new ChaikinOscillatorIndicator(series, shortBarCount, longBarCount)

      case "ChandelierExitLongIndicator" =>
        /*
         * The Chandelier Exit (long) Indicator.
         */
        val barCount  = getAsInt(params, "barCount",  22)
        /* K multiplier for ATR (usually 3.0) */
        val k = getAsDouble(params, "k", 3.0)
        new ChandelierExitLongIndicator(series, barCount, k)

      case "ChandelierExitShortIndicator" =>
        /*
         * The Chandelier Exit (short) Indicator.
         */
        val barCount  = getAsInt(params, "barCount",  22)
        /* K multiplier for ATR (usually 3.0) */
        val k = getAsDouble(params, "k", 3.0)
        new ChandelierExitShortIndicator(series, barCount, k)

      case "ChopIndicator" =>
        /*
         * Choppiness indicator
         */
        val ciTimeFrame = params.getInt("ciTimeFrame")
        val scaleTo = getAsInt(params, "scaleTo", 100)

        new ChopIndicator(series, ciTimeFrame, scaleTo)

      case "CMOIndicator" =>
        /*
         * Chande Momentum Oscillator indicator. The source
         * dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new CMOIndicator(indicators, barCount)

      case "CoppockCurveIndicator" =>
        /*
         * Coppock Curve indicator.
         *
         * The source dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val longRoCBarCount  = getAsInt(params, "longRoCBarCount",  14)
        val shortRoCBarCount = getAsInt(params, "shortRoCBarCount", 11)

        val wmaBarCount = getAsInt(params, "wmaBarCount", 10)
        new CoppockCurveIndicator(indicators, longRoCBarCount, shortRoCBarCount, wmaBarCount)

      case "DeMarkPivotPointIndicator" =>
        /*
         * DeMark Pivot Point indicator.
         */
        val timeLevel = params.getString("timeLevel")
        new DeMarkPivotPointIndicator(series, toTimeLevel(timeLevel))

      case "DeMarkReversalIndicator" =>
        /*
         * DeMarkReversal indicator.
         */
        val timeLevel = params.getString("timeLevel")
        val pivotLevel = params.getString("deMarkPivotLevel")

        val pivotPointIndicator = new DeMarkPivotPointIndicator(series, toTimeLevel(timeLevel))
        new DeMarkReversalIndicator(pivotPointIndicator, toDeMarkPivotLevel(pivotLevel))

      case "DojiIndicator" =>
        /*
         * Doji indicator.
         *
         * A candle/bar is considered Doji if its body height is lower than
         * the average multiplied by a factor.
         */
        val barCount  = params.getInt("barCount")
        val bodyFactor = params.getDouble("bodyFactor")

        new DojiIndicator(series, barCount, bodyFactor)

      case "DoubleEMAIndicator" =>
        /*
         * Double exponential moving average indicator.
         *
         * The source dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        /*
         * Advanced parameters alpha, beta, gamma, delta
         * are currently not supported
         */
        val barCount = params.getInt("barCount")
        new DoubleEMAIndicator(indicators, barCount)

      case "DPOIndicator" =>
        /*
         * The Detrended Price Oscillator (DPO) indicator.
         *
         * The Detrended Price Oscillator (DPO) is an indicator designed
         * to remove trend from price and make it easier to identify cycles.
         * DPO does not extend to the last date because it is based on a
         * displaced moving average.
         *
         * However, alignment with the most recent is not an issue because
         * DPO is not a momentum oscillator. Instead, DPO is used to identify
         * cycles highs/lows and estimate cycle length
         *
         * In short, DPO(20) equals price 11 days ago less the 20-day SMA.
         */
        val barCount = params.getInt("barCount")
        new DPOIndicator(series, barCount)

      case "EMAIndicator" =>
        /*
         * Exponential moving average indicator.
         *
         * The source dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        /*
         * Advanced parameters alpha, beta, gamma, delta
         * are currently not supported
         */
        val barCount = params.getInt("barCount")
        new EMAIndicator(indicators, barCount)

      case "FibonacciReversalIndicator" =>
        /*
         * Fibonacci Reversal Indicator.
         */
        val timeLevel = params.getString("timeLevel")
        val pivotPointIndicator = new PivotPointIndicator(series, toTimeLevel(timeLevel))

        val fibonacciFactor = params.getString("fibonacciFactor")
        val fibReversalTyp = params.getString("fibReversalTyp")

        new FibonacciReversalIndicator(pivotPointIndicator, fibonacciFactor.toDouble, toFibonacciReversalType(fibReversalTyp))

      case "FisherIndicator" =>
        /*
         * The Fisher Indicator.
         *
         * The source dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        /*
         * Advanced parameters alpha, beta, gamma, delta
         * are currently not supported
         */
        val barCount = getAsInt(params, "barCount", 10)
        new FisherIndicator(indicators, barCount)

      case "HMAIndicator" =>
        /*
         * Hull moving average (HMA) indicator.
         *
         * The source dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new HMAIndicator(indicators, barCount)

      case "IchimokuChikouSpanIndicator" =>
        /*
         * Ichimoku clouds: Chikou Span indicator
         */
        val timeDelay = getAsInt(params, "timeDelay", 26)
        new IchimokuChikouSpanIndicator(series, timeDelay)

      case "IchimokuKijunSenIndicator" =>
        /*
         * Ichimoku clouds: Kijun-sen (Base line) indicator
         */
        val barCount = getAsInt(params, "barCount", 26)
        new IchimokuChikouSpanIndicator(series, barCount)

      case "IchimokuTenkanSenIndicator" =>
        /*
         * Ichimoku clouds: Tenkan-sen (Conversion line) indicator
         */
        val barCount = getAsInt(params, "barCount", 9)
        new IchimokuTenkanSenIndicator(series, barCount)

      case "IchimokuSenkouSpanAIndicator" =>
        /*
         * Ichimoku clouds: Senkou Span A (Leading Span A) indicator
         */
        val barCountConversionLine = getAsInt(params, "barCountConversionLine", 9)
        val barCountBaseLine = getAsInt(params, "barCountBaseLine", 26)

        new IchimokuSenkouSpanAIndicator(series, barCountConversionLine, barCountBaseLine)

      case "IchimokuSenkouSpanBIndicator" =>
        /*
         * Ichimoku clouds: Senkou Span B (Leading Span B) indicator
         */
        val barCount = getAsInt(params, "barCount", 52)
        val offset = getAsInt(params, "offset", 26)

        new IchimokuSenkouSpanBIndicator(series, barCount, offset)

      case "IIIIndicator" =>
        /* Intraday Intensity Index */
        new IIIIndicator(series)

      case "KAMAIndicator" =>
        /*
         * The Kaufman's Adaptive Moving Average (KAMA) Indicator.
         * The source dataset for the technical indicator, depends
         * on `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        /* The time frame of the effective ratio (usually 10) */
        val barCountEffectiveRatio = getAsInt(params, "barCountEffectiveRatio", 10)

        /* The time frame fast (usually 2) */
        val barCountFast = getAsInt(params, "barCountFast", 2)

        /* The time frame slow (usually 30) */
        val barCountSlow = getAsInt(params, "barCountSlow", 30)

        new KAMAIndicator(indicators, barCountEffectiveRatio, barCountFast, barCountSlow)

      case "KeltnerChannelLowerIndicator" =>
        /*
         * This indicator depends on [[ATRIndicator]] and also
         * [[KeltnerChannelMiddleIndicator]]. Interface params
         * are `barCount` for the ATR part and `ratio` for the
         * MIDDLE part
         *
         * The source dataset for the technical indicator,
         * depends on `src_col` and is `open`, `high`, `low`,
         * or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCountATR = params.getInt("barCountATR")
        val barCountEMA = params.getInt("barCountEMA")

        val ratio = params.getDouble("ratio")

        /* Build [[KeltnerChannelMiddleIndicator]] */
        val middleIndicator = new KeltnerChannelMiddleIndicator(indicators, barCountEMA)
        new KeltnerChannelLowerIndicator(middleIndicator, ratio, barCountATR)

      case "KeltnerChannelMiddleIndicator" =>
        /*
         * Keltner Channel (middle line) indicator
         */
        val barCountEMA = params.getInt("barCountEMA")
        new KeltnerChannelMiddleIndicator(series, barCountEMA)

      case "KeltnerChannelUpperIndicator" =>
        /*
         * This indicator depends on [[ATRIndicator]] and also
         * [[KeltnerChannelMiddleIndicator]]. Interface params
         * are `barCount` for the ATR part and `ratio` for the
         * MIDDLE part
         *
         * The source dataset for the technical indicator,
         * depends on `src_col` and is `open`, `high`, `low`,
         * or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCountATR = params.getInt("barCountATR")
        val barCountEMA = params.getInt("barCountEMA")

        val ratio = params.getDouble("ratio")

        /* Build [[KeltnerChannelMiddleIndicator]] */
        val middleIndicator = new KeltnerChannelMiddleIndicator(indicators, barCountEMA)
        new KeltnerChannelUpperIndicator(middleIndicator, ratio, barCountATR)

      case "KSTIndicator" =>
        /*
         * The Know Sure Thing Indicator.
         *
         * The source dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        new KSTIndicator(indicators)

      case "LowerShadowIndicator" =>
        /*
         * Lower shadow height indicator.
         *
         * Provides the (absolute) difference between the low price
         * and the lowest price of the candle body. I.e.:
         *
         * low price - min(open price, close price)
         */
        new LowerShadowIndicator(series)

      case "LWMAIndicator" =>
        /*
         * Linearly Weighted Moving Average (LWMA).
         *
         * The source dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new LWMAIndicator(indicators, barCount)

      case "MACDIndicator" =>
        /*
         * Moving average convergence divergence (MACDIndicator)
         * indicator. Also known as MACD Absolute Price Oscillator (APO).
         *
         * The source dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val shortBarCount = getAsInt(params, "shortBarCount", 12)
        val longBarCount = getAsInt(params, "longBarCount", 26)
        /*
         * This indicator depends [[EMAIndicator]] and computes
         * their difference.
         */
        new MACDIndicator(indicators, shortBarCount, longBarCount)

      case "MassIndexIndicator" =>

        /* The time frame for EMAs (usually 9) */
        val barCountEMA = getAsInt(params, "barCountEMA", 9)
        val barCount = params.getInt("barCount")

        new MassIndexIndicator(series, barCountEMA, barCount)

      case "MeanDeviationIndicator" =>
        /*
         * Mean deviation indicator.
         *
         * The source dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new MeanDeviationIndicator(indicators, barCount)

      case "MMAIndicator" =>
        /*
         * Modified moving average indicator.
         *
         * It is similar to exponential moving average but smooths
         * more slowly. Used in Welles Wilder's indicators like ADX,
         * RSI.
         *
         * The source dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new MMAIndicator(indicators, barCount)

      case "MVWAPIndicator" =>
        /*
         * The Moving volume weighted average price (MVWAP) Indicator.
         * Depends on [[VWAPIndicator]]
         *
         * The current implementation leverages the same timeframe
         * (barCount) for the internal and external indicator.
         */
        val barCount = params.getInt("barCount")

        val vwapIndicator = new VWAPIndicator(series, barCount)
        new MVWAPIndicator(vwapIndicator, barCount)

      case "NVIIndicator" =>
        /* Negative Volume Index (NVI) indicator. */
        new NVIIndicator(series)

      case "OnBalanceVolumeIndicator" =>
        /* On-balance volume indicator. */
        new OnBalanceVolumeIndicator(series)

      case "ParabolicSarIndicator" =>
        /*
         * Parabolic SAR (Stop And Reverse) indicator.
         *
         * The most simple interface is used (i.e. with any params)
         */
        new ParabolicSarIndicator(series)

      case "PercentBIndicator" =>
        /*
         * % B Indicator
         *
         * The source dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        val k = getAsDouble(params, "k", 2.0)

        new PercentBIndicator(indicators, barCount, k)

      case "PeriodicalGrowthRateIndicator" =>
        /*
         * Periodical Growth Rate indicator.
         *
         * In general the 'Growth Rate' is useful for comparing the
         * average returns of investments in stocks or funds and can
         * be used to compare the performance e.g. comparing the historical
         * returns of stocks with bonds.
         *
         * This indicator has the following characteristics: - the calculation
         * is timeframe dependent. The timeframe corresponds to the number
         * of trading events in a period, e. g. the timeframe for a US trading
         * year for end of day bars would be '251' trading days - the result is
         * a step function with a constant value within a timeframe - NaN values
         * while index is smaller than timeframe, e.g. timeframe is year, than no
         * values are calculated before a full year is reached - NaN values for
         * incomplete timeframes, e.g. timeframe is a year and your timeseries
         * contains data for 11,3 years, than no values are calculated for the
         * remaining 0,3 years - the method 'getTotalReturn' calculates the total
         * return over all returns of the corresponding timeframes
         *
         * The source dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new PeriodicalGrowthRateIndicator(indicators, barCount)

      case "PivotPointIndicator" =>
        /*
         * Pivot Point indicator.
         */
        val timeLevel = params.getString("timeLevel")
        new PivotPointIndicator(series, toTimeLevel(timeLevel))

      case "PPOIndicator" =>
        /*
         * Percentage price oscillator (PPO) indicator. Also known
         * as MACD Percentage Price Oscillator (MACD-PPO).
         *
         * The source dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val shortBarCount = getAsInt(params, "shortBarCount", 12)
        val longBarCount = getAsInt(params, "longBarCount", 26)
        /*
         * This indicator depends [[EMAIndicator]] and computes
         * their difference.
         */
        new PPOIndicator(indicators, shortBarCount, longBarCount)

      case "PVIIndicator" =>
        /* Positive Volume Index (PVI) indicator. */
        new PVIIndicator(series)

      case "PVOIndicator" =>
        /*
         * Percentage Volume Oscillator (PVO).
         *
         * Sample:  ((12-day EMA of Volume - 26-day EMA of Volume) / 26-day EMA of Volume) x 100
         *
         */
        val shortBarCount = getAsInt(params, "shortBarCount", 12)
        val longBarCount = getAsInt(params, "longBarCount", 26)

        new PVOIndicator(series, shortBarCount, longBarCount)

      case "RAVIIndicator" =>
        /*
         * Chande's Range Action Verification Index (RAVI) indicator.
         * To preserve trend direction, default calculation does not
         * use absolute value.
         *
         * The source dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val shortSmaBarCount = getAsInt(params, "shortSmaBarCount", 7)
        val longSmaBarCount = getAsInt(params, "longSmaBarCount", 65)
        /*
         * This indicator depends [[EMAIndicator]] and computes
         * their difference.
         */
        new RAVIIndicator(indicators, shortSmaBarCount, longSmaBarCount)

      case "RealBodyIndicator" =>
        /*
         * Real (candle) body height indicator.
         *
         * Provides the (relative) difference between the open price
         * and the close price of a bar. I.e.: close price - open price
         */
        new RealBodyIndicator(series)

      case "ROCIndicator" =>
        /*
         * Rate of Change (Momentum) indicator.
         *
         * The source dataset for the technical indicator, depends
         * on `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new ROCIndicator(indicators, barCount)

      case "ROCVIndicator" =>
        /*
         * Rate of change of volume (ROCVIndicator) indicator.
         * Aka. Momentum of Volume
         *
         * The ROCVIndicator calculation compares the current
         * volume with the volume "n" periods ago.
         */
        val barCount = params.getInt("barCount")
        new ROCVIndicator(series, barCount)

      case "RSIIndicator" =>
        /*
         * Relative strength index indicator.
         *
         * Computed using original Welles Wilder formula.
         *
         * The source dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new RSIIndicator(indicators, barCount)

      case "RWIHighIndicator" =>
        /*
         * The Random Walk Index High Indicator.
         */
        val barCount = params.getInt("barCount")
        new RWIHighIndicator(series, barCount)

      case "RWILowIndicator" =>
        /*
         * The Random Walk Index Low Indicator.
         */
        val barCount = params.getInt("barCount")
        new RWILowIndicator(series, barCount)

      case "SigmaIndicator" =>
        /*
         * Sigma-Indicator (also called, "z-score" or
         * "standard score").
         *
         * The source dataset for the technical indicator,
         * depends on `src_col` and is `open`, `high`, `low`,
         * or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new SigmaIndicator(indicators, barCount)

      case "SimpleLinearRegressionIndicator" =>
        /*
         * Simple linear regression indicator. The source
         * dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new SimpleLinearRegressionIndicator(indicators, barCount)

      case "SMAIndicator" =>
        /*
         * Simple moving average (SMA) indicator. The source
         * dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new SMAIndicator(indicators, barCount)

      case "StandardDeviationIndicator" =>
        /*
         * Standard deviation indicator. The source
         * dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new StandardDeviationIndicator(indicators, barCount)

      case "StandardErrorIndicator" =>
        /*
         * Standard error indicator. The source
         * dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new StandardErrorIndicator(indicators, barCount)

      case "StandardReversalIndicator" =>
        /*
         * Standard Reversal indicator.
         */
        val timeLevel = params.getString("timeLevel")
        val pivotLevel = params.getString("pivotLevel")

        val pivotPointIndicator = new PivotPointIndicator(series, toTimeLevel(timeLevel))
        new StandardReversalIndicator(pivotPointIndicator, toPivotLevel(pivotLevel))

      case "StochasticOscillatorDIndicator" =>
        /*
         * The Stochastic Oscillator D Indicator.
        */
        val barCount = params.getInt("barCount")

        val k = new StochasticOscillatorKIndicator(series, barCount)
        new StochasticOscillatorDIndicator(k)

      case "StochasticOscillatorKIndicator" =>
        /*
         * The Stochastic Oscillator K Indicator.
        */
        val barCount = params.getInt("barCount")
        /*
         * This indicator depends [[EMAIndicator]] and computes
         * their difference.
         */
        new StochasticOscillatorKIndicator(series, barCount)

      case "StochasticRSIIndicator" =>
        /*
         * The Stochastic RSI Indicator.
         *
         * Stoch RSI = (RSI - MinimumRSIn) / (MaximumRSIn - MinimumRSIn)
         */
        val barCount = params.getInt("barCount")
        /*
         * This indicator depends [[EMAIndicator]] and computes
         * their difference.
         */
        new StochasticRSIIndicator(series, barCount)

      case "ThreeBlackCrowsIndicator" =>
        /*
         * Three Black Crows indicator.
         */
        val barCount = params.getInt("barCount")
        val factor = params.getDouble("factor")

        new ThreeBlackCrowsIndicator(series, barCount, factor)

      case "ThreeWhiteSoldiersIndicator" =>
        /*
         * Three White Soldiers indicator.
         */
        val barCount = params.getInt("barCount")
        val factor = params.getDouble("factor")

        new ThreeWhiteSoldiersIndicator(series, barCount, DoubleNum.valueOf(factor))

      case "TripleEMAIndicator" =>
        /*
         * Triple Exponential Moving Average (TRIX) indicator
         *
         * The source dataset for the technical indicator, depends
         * on `src_col` and is `open`, `high`, `low`, or `close`.
         *
         * TEMA needs "3 * period - 2" of data to start producing
         * values in contrast to the period samples needed by a
         * regular EMA.
         */
        val barCount = params.getInt("barCount")

        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        new TripleEMAIndicator(indicators, barCount)

      case "UlcerIndexIndicator" =>
        /*
         * The source dataset for the technical indicator, depends
         * on `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new UlcerIndexIndicator(indicators, barCount)

      case "UpperShadowIndicator" =>
        /*
         * Upper shadow height indicator.
         *
         * Provides the (absolute) difference between the high price
         * and the highest price of the candle body. I.e.:
         *
         * high price - max(open price, close price)
         */
        new UpperShadowIndicator(series)

      case "VarianceIndicator" =>
        /*
         * Variance indicator
         *
         * The source dataset for the technical indicator, depends
         * on `src_col` and is `open`, `high`, `low`, or `close`.
         *
         * TEMA needs "3 * period - 2" of data to start producing
         * values in contrast to the period samples needed by a
         * regular EMA.
         */
        val barCount = params.getInt("barCount")

        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        new VarianceIndicator(indicators, barCount)

      case "VWAPIndicator" =>
        /*
         * The volume-weighted average price (SWAP) Indicator.
         */
        val barCount = params.getInt("barCount")
        new VWAPIndicator(series, barCount)

      case "WilliamsRIndicator" =>
        /*
         * William's R indicator. This indicator depends on
         * `high`, `low` and `close` price.
         */
        val barCount = params.getInt("barCount")
        new WilliamsRIndicator(series, barCount)

      case "WMAIndicator" =>
        /*
         * Weighted moving average (WMA) indicator. The source
         * dataset for the technical indicator, depends on
         * `src_col` and is `open`, `high`, `low`, or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new WMAIndicator(indicators, barCount)

      case "ZLEMAIndicator" =>
        /*
         * Zero-lag exponential moving average indicator.
         * The source dataset for the technical indicator,
         * depends on `src_col` and is `open`, `high`, `low`,
         * or `close`.
         */
        val src_col = indicatorConf.getString("src_col")
        val indicators = OHLCHelper(series, src_col)

        val barCount = params.getInt("barCount")
        new ZLEMAIndicator(indicators, barCount)

      case _ => throw new Exception(s"Indicator `$name` is not supported.")
    }

    (dst_col, technical)

  }

  private def toFibonacciReversalType(value:String):FibonacciReversalIndicator.FibReversalTyp = {

    value match {
      case "RESISTANCE" => FibonacciReversalIndicator.FibReversalTyp.RESISTANCE
      case "SUPPORT"    => FibonacciReversalIndicator.FibReversalTyp.SUPPORT
      case _ => throw new Exception(s"FibonacciReversal type $value is not defined.")
    }

  }

  private def toDeMarkPivotLevel(value:String): DeMarkReversalIndicator.DeMarkPivotLevel = {

    value match {
      case "RESISTANCE" => DeMarkReversalIndicator.DeMarkPivotLevel.RESISTANCE
      case "SUPPORT"    => DeMarkReversalIndicator.DeMarkPivotLevel.SUPPORT
      case _ => throw new Exception(s"Pivot level $value is not defined.")
    }
  }

  private def toPivotLevel(value:String):PivotLevel = {
    value match {
      case "RESISTANCE_1" => PivotLevel.RESISTANCE_1
      case "RESISTANCE_2" => PivotLevel.RESISTANCE_2
      case "RESISTANCE_3" => PivotLevel.RESISTANCE_3
      case "SUPPORT_1"    => PivotLevel.SUPPORT_1
      case "SUPPORT_2"    => PivotLevel.SUPPORT_2
      case "SUPPORT_3"    => PivotLevel.SUPPORT_3
      case _ => throw new Exception(s"Pivot level $value is not defined.")
    }
  }

  private def toTimeLevel(value:String):TimeLevel = {

    value match {
      case "BARBASED" => TimeLevel.BARBASED
      case "DAY"      => TimeLevel.DAY
      case "WEEK"     => TimeLevel.WEEK
      case "MONTH"    => TimeLevel.MONTH
      case "YEAR"     => TimeLevel.YEAR
      case _ => throw new Exception(s"Time level $value is not defined.")
    }

  }
}
