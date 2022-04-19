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

import org.ta4j.core.indicators._
import org.ta4j.core.indicators.adx._
import org.ta4j.core.indicators.bollinger._
import org.ta4j.core.indicators.candles._
import org.ta4j.core.indicators.ichimoku._
import org.ta4j.core.indicators.keltner._
import org.ta4j.core.indicators.pivotpoints._
import org.ta4j.core.indicators.statistics._
import org.ta4j.core.indicators.volume._

trait TACompute {

  def getValue(indicator:Any, i:Int):Double = {

    indicator match {

      /* Acceleration-Deceleration */
      case tech:AccelerationDecelerationIndicator =>
        tech.getValue(i).doubleValue

      /* Accumulation-distribution indicator (ADI). */
      case tech:AccumulationDistributionIndicator =>
        tech.getValue(i).doubleValue

      /* ADX Indicator. */
      case tech:ADXIndicator =>
        tech.getValue(i).doubleValue

      /* Arron Low Price Indicator */
      case tech:AroonDownIndicator =>
        tech.getValue(i).doubleValue

      /* Arron High Price Indicator */
      case tech:AroonUpIndicator =>
        tech.getValue(i).doubleValue

      /* Aroon Oscillator. */
      case tech:AroonOscillatorIndicator =>
        tech.getValue(i).doubleValue

      /* Average True Range */
      case tech:ATRIndicator =>
        tech.getValue(i).doubleValue

      /* Awesome Oscillator */
      case tech:AwesomeOscillatorIndicator =>
        tech.getValue(i).doubleValue

      /* Bearish Engulfing pattern indicator. */
      case tech:BearishEngulfingIndicator =>
        val value = tech.getValue(i)
        if (value == true) 1D else 0D

      /* Bearish Harami pattern indicator. */
      case tech:BearishHaramiIndicator =>
        val value = tech.getValue(i)
        if (value == true) 1D else 0D

      /*  Bollinger BandWidth indicator. */
      case tech:BollingerBandWidthIndicator =>
        tech.getValue(i).doubleValue

      /*  Bollinger Bands Lower indicator. */
      case tech:BollingerBandsLowerIndicator =>
        tech.getValue(i).doubleValue

      /*  Bollinger Bands Middle indicator. */
      case tech:BollingerBandsMiddleIndicator =>
        tech.getValue(i).doubleValue

      /*  Bollinger Bands Upper indicator. */
      case tech:BollingerBandsUpperIndicator =>
        tech.getValue(i).doubleValue

      /* Bullish Engulfing pattern indicator. */
      case tech:BullishEngulfingIndicator =>
        val value = tech.getValue(i)
        if (value == true) 1D else 0D

      /* Bullish Harami pattern indicator. */
      case tech:BullishHaramiIndicator =>
        val value = tech.getValue(i)
        if (value == true) 1D else 0D

      /* Chaikin Oscillator. */
      case tech:ChaikinOscillatorIndicator =>
        tech.getValue(i).doubleValue

      /* Choppiness indicator */
      case tech:ChopIndicator =>
        tech.getValue(i).doubleValue

      /* DeMark Pivot Point indicator. */
      case tech:DeMarkPivotPointIndicator =>
        tech.getValue(i).doubleValue

      /* DeMarkReversal indicator. */
      case tech:DeMarkReversalIndicator =>
        tech.getValue(i).doubleValue

      /* Doji indicator. */
      case tech:DojiIndicator =>
        val value = tech.getValue(i)
        if (value == true) 1D else 0D

      /* Commodity Channel Index (CCI) indicator. */
      case tech:CCIIndicator =>
        tech.getValue(i).doubleValue

      /* Chaikin Money Flow (CMF) indicator. */
      case tech:ChaikinMoneyFlowIndicator =>
        tech.getValue(i).doubleValue

      /* The Chandelier Exit (High Price) Indicator. */
      case tech:ChandelierExitLongIndicator =>
        tech.getValue(i).doubleValue

      /* The Chandelier Exit (Low Price) Indicator. */
      case tech:ChandelierExitShortIndicator =>
        tech.getValue(i).doubleValue

      /* Chande Momentum Oscillator indicator. */
      case tech:CMOIndicator =>
        tech.getValue(i).doubleValue

      /* Coppock Curve indicator. */
      case tech:CoppockCurveIndicator =>
        tech.getValue(i).doubleValue

      /* The Detrended Price Oscillator (DPO) indicator. */
      case tech:DPOIndicator =>
        tech.getValue(i).doubleValue

      /* The Double exponential moving average indicator. */
      case tech:DoubleEMAIndicator =>
        tech.getValue(i).doubleValue

      /* The Exponential moving average indicator. */
      case tech:EMAIndicator =>
        tech.getValue(i).doubleValue

      /* Fibonacci Reversal Indicator. */
      case tech:FibonacciReversalIndicator =>
        tech.getValue(i).doubleValue

      /* The Fisher Indicator. */
      case tech:FisherIndicator =>
        tech.getValue(i).doubleValue

      /* The Hull moving average (HMA) indicator. */
      case tech:HMAIndicator =>
        tech.getValue(i).doubleValue

      /* Ichimoku clouds: Chikou Span indicator */
      case tech:IchimokuChikouSpanIndicator =>
        tech.getValue(i).doubleValue

      /* Ichimoku clouds: Kijun-sen (Base line) indicator */
      case tech:IchimokuKijunSenIndicator =>
        tech.getValue(i).doubleValue

      /* Ichimoku clouds: Tenkan-sen (Conversion line) indicator */
      case tech:IchimokuTenkanSenIndicator =>
        tech.getValue(i).doubleValue

      /* Ichimoku clouds: Senkou Span A (Leading Span A) indicator */
      case tech:IchimokuSenkouSpanAIndicator =>
        tech.getValue(i).doubleValue

      /* Ichimoku clouds: Senkou Span B (Leading Span B) indicator */
      case tech:IchimokuSenkouSpanBIndicator =>
        tech.getValue(i).doubleValue

      /* Intraday Intensity Index */
      case tech:IIIIndicator =>
        tech.getValue(i).doubleValue

      /* The Kaufman's Adaptive Moving Average (KAMA) Indicator.  */
      case tech:KAMAIndicator =>
        tech.getValue(i).doubleValue

      /* Keltner Channel: Low */
      case tech:KeltnerChannelLowerIndicator =>
        tech.getValue(i).doubleValue

      /* Keltner Channel: Middle */
      case tech:KeltnerChannelMiddleIndicator =>
        tech.getValue(i).doubleValue

      /* Keltner Channel: Upper */
      case tech:KeltnerChannelUpperIndicator =>
        tech.getValue(i).doubleValue

      /* The Know Sure Thing Indicator. */
      case tech:KSTIndicator =>
        tech.getValue(i).doubleValue

      /* Linearly Weighted Moving Average (LWMA). */
      case tech:LWMAIndicator =>
        tech.getValue(i).doubleValue

      /* Lower shadow height indicator. */
      case tech:LowerShadowIndicator =>
        tech.getValue(i).doubleValue

      /* Moving average convergence divergence indicator. */
      case tech:MACDIndicator =>
        tech.getValue(i).doubleValue

      /* Mass Index Indicator */
      case tech:MassIndexIndicator =>
        tech.getValue(i).doubleValue

      /*Mean deviation indicator. */
      case tech:MeanDeviationIndicator =>
        tech.getValue(i).doubleValue

      /* Modified moving average indicator. */
      case tech:MMAIndicator =>
        tech.getValue(i).doubleValue

      /* The Moving volume weighted average price (MVWAP) Indicator. */
      case tech:MVWAPIndicator =>
        tech.getValue(i).doubleValue

      /* Negative Volume Index (NVI) indicator. */
      case tech:NVIIndicator =>
        tech.getValue(i).doubleValue

      /* On-balance volume indicator. */
      case tech:OnBalanceVolumeIndicator =>
        tech.getValue(i).doubleValue

      /* Parabolic SAR (Stop And Reverse) indicator. */
      case tech:ParabolicSarIndicator =>
        tech.getValue(i).doubleValue

      /* % B Indicator */
      case tech:PercentBIndicator =>
        tech.getValue(i).doubleValue

      /* Periodical Growth Rate indicator. */
      case tech:PeriodicalGrowthRateIndicator =>
        tech.getValue(i).doubleValue

      /* Pivot Point indicator. */
      case tech:PivotPointIndicator =>
        tech.getValue(i).doubleValue

      /* Percentage Volume Oscillator (PVO). */
      case tech:PVOIndicator =>
        tech.getValue(i).doubleValue

      /* Percentage price oscillator (PPO) indicator. */
      case tech:PPOIndicator =>
        tech.getValue(i).doubleValue

      /* Positive Volume Index (PVI) indicator. */
      case tech:PVIIndicator =>
        tech.getValue(i).doubleValue

      /* Real (candle) body height indicator. */
      case tech:RealBodyIndicator =>
        tech.getValue(i).doubleValue

      /* Rate of Change (Momentum) indicator. */
      case tech:ROCIndicator =>
        tech.getValue(i).doubleValue

      /* Rate of change of volume (ROCVIndicator) indicator. */
      case tech:ROCVIndicator =>
        tech.getValue(i).doubleValue

      /* Chande Range Action Verification Index (RAVI) indicator. */
      case tech:RAVIIndicator =>
        tech.getValue(i).doubleValue

      /* Relative strength index indicator. */
      case tech:RSIIndicator =>
        tech.getValue(i).doubleValue

      /* The Random Walk Index High Indicator. */
      case tech:RWIHighIndicator =>
        tech.getValue(i).doubleValue

      /* The Random Walk Index Low Indicator. */
      case tech:RWILowIndicator =>
        tech.getValue(i).doubleValue

      /* The Sigma-Indicator. */
      case tech:SigmaIndicator =>
        tech.getValue(i).doubleValue

      /* Simple linear regression indicator. */
      case tech:SimpleLinearRegressionIndicator =>
        tech.getValue(i).doubleValue

      /* Simple Moving Average */
      case tech:SMAIndicator =>
        tech.getValue(i).doubleValue

      /* Standard deviation indicator. */
      case tech:StandardDeviationIndicator =>
        tech.getValue(i).doubleValue

      /* Standard error indicator. */
      case tech:StandardErrorIndicator =>
        tech.getValue(i).doubleValue

      /* Standard Reversal indicator. */
      case tech:StandardReversalIndicator =>
        tech.getValue(i).doubleValue

      /* The Stochastic Oscillator D Indicator. */
      case tech:StochasticOscillatorDIndicator =>
        tech.getValue(i).doubleValue

      /* The Stochastic Oscillator K Indicator. */
      case tech:StochasticOscillatorKIndicator =>
        tech.getValue(i).doubleValue

      /* The Stochastic RSI Indicator */
      case tech:StochasticRSIIndicator =>
        tech.getValue(i).doubleValue

      /*  Three Black Crows indicator. */
      case tech:ThreeBlackCrowsIndicator =>
        val value = tech.getValue(i)
        if (value == true) 1D else 0D

      /* Three White Soldiers indicator. */
      case tech:ThreeWhiteSoldiersIndicator =>
        val value = tech.getValue(i)
        if (value == true) 1D else 0D

      /* Triple Exponential Moving Average (TRIX) indicator */
      case tech:TripleEMAIndicator =>
        tech.getValue(i).doubleValue

      /* UlcerIndexIndicator */
      case tech:UlcerIndexIndicator =>
        tech.getValue(i).doubleValue

      /*  Upper shadow height indicator. */
      case tech:UpperShadowIndicator =>
        tech.getValue(i).doubleValue

      /*  Variance indicator */
      case tech:VarianceIndicator =>
        tech.getValue(i).doubleValue

      /* The volume-weighted average price (VWAP) Indicator. */
      case tech:VWAPIndicator =>
        tech.getValue(i).doubleValue

      /* William's R indicator. */
      case tech:WilliamsRIndicator =>
        tech.getValue(i).doubleValue

      /* WMA indicator. */
      case tech:WMAIndicator =>
        tech.getValue(i).doubleValue

      /* Zero-lag exponential moving average indicator. */
      case tech:ZLEMAIndicator =>
        tech.getValue(i).doubleValue

      case _ =>
        throw new Exception(s"Unknown technical indicator detected.")

    }

  }

}
