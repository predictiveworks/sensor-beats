package de.kp.works.sensor.weather.dwd

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

object MxParams extends Enumeration {
  type MxParam = Value

  /* https://opendata.dwd.de/weather/lib/MetElementDefinition.xml */

  /** TEMPERATURE **/

  /* Temperature 2m above surface: K */
  val TTT: MxParam = Value(1, "TTT")

  /* Absolute error temperature 2m above surface: K */
  val E_TTT: MxParam = Value(2, "E_TTT")

  /* Dewpoint 2m above surface: K */
  val Td: MxParam = Value(3, "Td")

  /* Absolute error dew point 2m above surface: K */
  val E_Td: MxParam = Value(4, "E_Td")

  /* Maximum temperature - within the last 12 hours: K */
  val TX: MxParam = Value(5, "TX")

  /* Minimum temperature - within the last 12 hours: K */
  val TN: MxParam = Value(6, "TN")

  /* Temperature 5cm above surface: K */
  val T5cm: MxParam = Value(7, "T5cm")

  /* Minimum surface temperature at 5cm within the last 12 hours: K */
  val TG: MxParam = Value(8, "TG")

  /* Mean temperature during the last 24 hours: K */
  val TM: MxParam = Value(9, "TM")

  /** WIND **/

  /* Wind direction: 0°..360° */
  val DD: MxParam = Value(10, "DD")

  /* Absolute error wind direction: 0°..360° */
  val E_DD: MxParam = Value(11, "E_DD")

  /* Wind speed: m/s */
  val FF: MxParam = Value(12, "FF")

  /* Absolute error wind speed 10m above surface: m/s */
  val E_FF: MxParam = Value(13, "E_FF")

  /* Maximum wind gust within the last hour: m/s */
  val FX1: MxParam = Value(14, "FX1")

  /* Maximum wind gust within the last 3 hours: m/s */
  val FX3: MxParam = Value(15, "FX3")

  /* Maximum wind gust within the last 12 hours: m/s */
  val FXh: MxParam = Value(16, "FXh")

  /* Probability of wind gusts >= 25kn within the last 12 hours: % (0..100) */
  val FXh25: MxParam = Value(17, "FXh25")

  /* Probability of wind gusts >= 40kn within the last 12 hours: % (0..100) */
  val FXh40: MxParam = Value(18, "FXh40")

  /* Probability of wind gusts >= 55kn within the last 12 hours: % (0..100) */
  val FXh55: MxParam = Value(19, "FXh55")

  /** PRECIPITATION **/

  /* Total precipitation during the last hour consistent with significant weather: kg/m2 */
  val RR1c: MxParam = Value(20, "RR1c")

  /* Total precipitation during the last hour: kg/m2 */
  val RR1: MxParam = Value(21, "RR1")

  /* Total precipitation during the last 3 hours consistent with significant weather: kg/m2 */
  val RR3c: MxParam = Value(22, "RR3c")

  /* Total precipitation during the last 3 hours: kg/m2 */
  val RR3: MxParam = Value(23, "RR3")

  /* Probability of precipitation > 0.0mm during the last 12 hours: % (0..100) */
  val Rh00: MxParam = Value(24, "Rh00")

  /* Probability of precipitation > 0.2mm during the last 6 hours: % (0..100) */
  val R602: MxParam = Value(25, "R602")

  /* Probability of precipitation > 0.2mm during the last 12 hours: % (0..100) */
  val Rh02: MxParam = Value(26, "Rh02")

  /* Probability of precipitation > 0.2mm during the last 24 hours: % (0..100) */
  val Rd02: MxParam = Value(27, "Rd02")

  /* Probability of precipitation > 1.0mm during the last 12 hours: % (0..100) */
  val Rh10: MxParam = Value(28, "Rh10")

  /* Probability of precipitation > 5.0mm during the last 6 hours: % (0..100) */
  val R650: MxParam = Value(29, "R650")

  /* Probability of precipitation > 5.0mm during the last 12 hours: % (0..100) */
  val Rh50: MxParam = Value(30, "Rh50")

  /* Probability of precipitation > 5.0mm during the last 24 hours: % (0..100) */
  val Rd50: MxParam = Value(31, "Rd50")

  /* Duration of precipitation within the last hour: s */
  val DRR1: MxParam = Value(32, "DRR1")

  /* Probability: Occurrence of stratiform precipitation within the last hour: % (0..100) */
  val wwD: MxParam = Value(33, "wwD")

  /* Probability: Occurrence of convective precipitation within the last hour: % (0..100) */
  val wwC: MxParam = Value(34, "wwC")

  /* Probability: Occurrence of liquid precipitation within the last hour: % (0..100) */
  val wwL: MxParam = Value(35, "wwL")

  /* Probability: Occurrence of solid precipitation within the last hour: % (0..100) */
  val wwS: MxParam = Value(36, "wwS")

  /* Probability: Occurrence of precipitation within the last hour: % (0..100) */
  val wwP: MxParam = Value(37, "wwP")

  /* Total precipitation during the last 6 hours: kg / m2 */
  val RR6: MxParam = Value(38, "RR6")

  /* Total precipitation during the last 6 hours consistent with significant weather: kg / m2 */
  val RR6c: MxParam = Value(39, "RR6c")

  /* Probability of precipitation > 0.0mm during the last 6 hours: % (0..100) */
  val R600: MxParam = Value(40, "R600")

  /* Probability of precipitation > 0.1 mm during the last hour: % (0..100) */
  val R101: MxParam = Value(41, "R101")

  /* Probability of precipitation > 0.2 mm during the last hour: % (0..100) */
  val R102: MxParam = Value(42, "R102")

  /* Probability of precipitation > 0.3 mm during the last hour: % (0..100) */
  val R103: MxParam = Value(43, "R103")

  /* Probability of precipitation > 0.5 mm during the last hour: % (0..100) */
  val R105: MxParam = Value(44, "R105")

  /* Probability of precipitation > 0.7 mm during the last hour: % (0..100) */
  val R107: MxParam = Value(45, "R107")

  /* Probability of precipitation > 1.0 mm during the last hour: % (0..100) */
  val R110: MxParam = Value(46, "R110")

  /* Probability of precipitation > 2.0 mm during the last hour: % (0..100) */
  val R120: MxParam = Value(47, "R120")

  /* Probability of precipitation > 3.0 mm during the last hour: % (0..100) */
  val R130: MxParam = Value(48, "R130")

  /* Probability of precipitation > 5.0 mm during the last hour: % (0..100) */
  val R150: MxParam = Value(49, "R150")

  /* Probability of precipitation > 10 mm during the last hour: % (0..100) */
  val RR1o1: MxParam = Value(50, "RR1o1")

  /* Probability of precipitation > 15 mm during the last hour: % (0..100) */
  val RR1w1: MxParam = Value(51, "RR1w1")

  /* Probability of precipitation > 25 mm during the last hour: % (0..100) */
  val RR1u1: MxParam = Value(52, "RR1u1")

  /* Probability: Occurrence of stratiform precipitation within the last 6 hours: % (0..100) */
  val wwD6: MxParam = Value(53, "wwD6")

  /* Probability: Occurrence of convective precipitation within the last 6 hours: % (0..100) */
  val wwC6: MxParam = Value(54, "wwC6")

  /* Probability: Occurrence of precipitation within the last 6 hours: % (0..100) */
  val wwP6: MxParam = Value(55, "wwP6")

  /* Probability: Occurrence of liquid precipitation within the last 6 hours: % (0..100) */
  val wwL6: MxParam = Value(56, "wwL6")

  /* Probability: Occurrence of solid precipitation within the last 6 hours: % (0..100) */
  val wwS6: MxParam = Value(57, "wwS6")

  /* Probability: Occurrence of stratiform precipitation within the last 12 hours: % (0..100) */
  val wwDh: MxParam = Value(58, "wwDh")

  /* Probability: Occurrence of convective precipitation within the last 12 hours: % (0..100) */
  val wwCh: MxParam = Value(59, "wwCh")

  /* Probability: Occurrence of precipitation within the last 12 hours: % (0..100) */
  val wwPh: MxParam = Value(60, "wwPh")

  /* Probability: Occurrence of liquid precipitation within the last 12 hours: % (0..100) */
  val wwLh: MxParam = Value(61, "wwLh")

  /* Probability: Occurrence of solid precipitation within the last 12 hours: % (0..100) */
  val wwSh: MxParam = Value(62, "wwSh")

  /* Probability of precipitation > 1.0 mm during the last 6 hours: % (0..100) */
  val R610: MxParam = Value(63, "R610")

  /* Total precipitation during the last 12 hours: kg / m2 */
  val RRh: MxParam = Value(64, "RRh")

  /* Total precipitation during the last 12 hours consistent with significant weather: kg / m2 */
  val RRhc: MxParam = Value(65, "RRhc")

  /* Total liquid precipitation during the last hour consistent with significant weather: kg / m2 */
  val RRL1c: MxParam = Value(66, "RRL1c")

  /* Probability of precipitation > 0.0 mm during the last 24 hours: % (0..100) */
  val Rd00: MxParam = Value(67, "Rd00")

  /* Probability of precipitation > 1.0 mm during the last 24 hours: % (0..100) */
  val Rd10: MxParam = Value(68, "Rd10")

  /* Total precipitation during the last 24 hours: kg / m2 */
  val RRd: MxParam = Value(69, "RRd")

  /* Total precipitation during the last 24 hours consistent with significant weather: kg / m2 */
  val RRdc: MxParam = Value(70, "RRdc")

  /* Probability: Occurrence of any precipitation within the last 24 hours: % (0..100) */
  val wwPd: MxParam = Value(71, "wwPd")

  /** SNOW RAIN **/

  /* Snow-Rain-Equivalent during the last hour: kg/m2 */
  val RRS1c: MxParam = Value(72, "RRS1c")

  /* Snow-Rain-Equivalent during the last 3 hours: kg/m2 */
  val RRS3c: MxParam = Value(73, "RRS3c")

  /** CLOUD COVER */

  /* Total cloud cover: % (0..100) */
  val N: MxParam = Value(74, "N")

  /* Effective cloud cover: % (0..100) */
  val Neff: MxParam = Value(75, "Neff")

  /* Cloud cover below 500 ft.: % (0..100) */
  val N05: MxParam = Value(76, "N05")

  /* Low cloud cover (lower than 2 km): % (0..100) */
  val Nl: MxParam = Value(77, "Nl")

  /* Midlevel cloud cover (2-7 km): % (0..100) */
  val Nm: MxParam = Value(78, "Nm")

  /* High cloud cover (>7 km): % (0..100) */
  val Nh: MxParam = Value(79, "Nh")

  /* Cloud cover low and mid level clouds below 7000 m: % (0..100) */
  val Nlm: MxParam = Value(80, "Nlm")

  /** PRESSURE **/

  /* Surface pressure, reduced: Pa */
  val PPPP: MxParam = Value(81, "PPPP")

  /* Absolute error surface pressure: Pa */
  val E_PPP: MxParam = Value(82, "E_PPP")

  /* Significant Weather: - */
  val ww: MxParam = Value(83, "ww")

  /* Significant Weather of the last 3 hours: - (0..95) */
  val ww3: MxParam = Value(84, "ww3")

  /* Optional significant weather (highest priority) during the last hour: - (0..95) */
  val WPc11: MxParam = Value(85, "WPc11")

  /* Optional significant weather (highest priority) during the last 3 hours: - (0..95) */
  val WPc31: MxParam = Value(86, "WPc31")

  /* Optional significant weather (highest priority) during the last 6 hours: - (0..95) */
  val WPc61: MxParam = Value(87, "WPc61")

  /* Optional significant weather (highest priority) during the last 12 hours: - (0..95) */
  val WPch1: MxParam = Value(88, "WPch1")

  /* Optional significant weather (highest priority) during the last 24 hours: - (0..95) */
  val WPcd1: MxParam = Value(89, "WPcd1")

  /** RADIATION **/

  /* Short wave radiation balance during the last 3 hours: kJ/m2 */
  val RadS3: MxParam = Value(90, "RadS3")

  /* Long wave radiation balance during the last 3 hours: kJ/m2 */
  val RadL3: MxParam = Value(91, "RadL3")

  /** IRRADIANCE **/

  /* Global Irradiance: kJ/m2 */
  val Rad1h: MxParam = Value(92, "Rad1h")

  /* Global irradiance within the last hour: % (0..80) */
  val RRad1: MxParam = Value(93, "RRad1")

  /** FOG **/

  /* Probability for fog within the last hour: % (0..100) */
  val wwM: MxParam = Value(94, "wwM")

  /* Probability for fog within the last 6 hours: % (0..100) */
  val wwM6: MxParam = Value(95, "wwM6")

  /* Probability for fog within the last 12 hours: % (0..100) */
  val wwMh: MxParam = Value(96, "wwMh")

  /* Probability: Occurrence of fog within the last 24 hours: % (0..100) */
  val wwMd: MxParam = Value(97, "wwMd")

  /** SUNSHINE **/

  /* Sunshine duration during the last Hour: s */
  val SunD1: MxParam = Value(98, "SunD1")

  /* Yesterdays total sunshine duration : s */
  val SunD: MxParam = Value(99, "SunD")

  /* Relative sunshine duration within the last 24 hours: % (0..100) */
  val RSunD: MxParam = Value(100, "RSunD")

  /* Probability: relative sunshine duration >  0 % within 24 hours: % (0..100) */
  val PSd00: MxParam = Value(101, "PSd00")

  /* Probability: relative sunshine duration > 30 % within 24 hours: % (0..100) */
  val PSd30: MxParam = Value(102, "PSd30")

  /* Probability: relative sunshine duration > 60 % within 24 hours: % (0..100) */
  val PSd60: MxParam = Value(103, "PSd60")

  /* Sunshine duration during the last three hours: s */
  val SunD3: MxParam = Value(104, "SunD3")

  /** RAIN **/

  /* Probability: Occurrence of freezing rain within the last hour: % (0..100) */
  val wwF: MxParam = Value(105, "wwF")

  /* Probability: Occurrence of freezing rain within the last 6 hours: % (0..100) */
  val wwF6: MxParam = Value(106, "wwF6")

  /* Probability: Occurrence of freezing rain within the last 12 hours: % (0..100) */
  val wwFh: MxParam = Value(107, "wwFh")

  /** DRIZZLE **/

  /* Probability: Occurrence of drizzle within the last hour: % (0..100) */
  val wwZ: MxParam = Value(108, "wwZ")

  /* Probability: Occurrence of drizzle within the last 6 hours: % (0..100) */
  val wwZ6: MxParam = Value(109, "wwZ6")

  /* Probability: Occurrence of drizzle within the last 12 hours: % (0..100) */
  val wwZh: MxParam = Value(110, "wwZh")

  /** THUNDERSTORMS **/

  /* Probability: Occurrence of thunderstorms within the last hour: % (0..100) */
  val wwT: MxParam = Value(111, "wwT")

  /* Probability: Occurrence of thunderstorms within the last 6 hours: % (0..100) */
  val wwT6: MxParam = Value(112, "wwT6")

  /* Probability: Occurrence of thunderstorms within the last 12 hours: % (0..100) */
  val wwTh: MxParam = Value(113, "wwTh")

  /* Probability: Occurrence of thunderstorms within the last 24 hours: % (0..100) */
  val wwTd: MxParam = Value(114, "wwTd")

  /** WIND GUST **/

  /* Probability: Occurrence of gusts >= 25kn within the last 6 hours : % (0..100) */
  val FX625: MxParam = Value(115, "FX625")

  /* Probability: Occurrence of gusts >= 40kn within the last 6 hours : % (0..100) */
  val FX640: MxParam = Value(116, "FX640")

  /* Probability: Occurrence of gusts >= 55kn within the last 6 hours : % (0..100) */
  val FX655: MxParam = Value(117, "FX655")

  /** SNOW **/

  /* Accumulated new snow amount in 3 hours: m */
  val Sa3: MxParam = Value(118, "Sa3")

  /* Accumulated new snow amount in 6 hours (amount of 3h values): m */
  val Sa6: MxParam = Value(119, "Sa6")

  /* Accumulated new snow amount in 12 hours (amount of 6h values): m */
  val Sah: MxParam = Value(120, "Sah")

  /* Accumulated new snow amount in 24 hours (amount of 12h values): m */
  val Sad: MxParam = Value(121, "Sad")

  /* Probability of > 5cm new snow amount in 6 hours: % (0..100) */
  val Sa605: MxParam = Value(122, "Sa605")

  /* Probability of > 10cm new snow amount in 6 hours: % (0..100) */
  val Sa610: MxParam = Value(123, "Sa610")

  /* Probability of > 20cm new snow amount in 6 hours: % (0..100) */
  val Sa620: MxParam = Value(124, "Sa620")

  /* Probability of > 5cm new snow amount in 12 hours: % (0..100) */
  val Sah05: MxParam = Value(125, "Sah05")

  /* Probability of > 10cm new snow amount in 12 hours: % (0..100) */
  val Sah10: MxParam = Value(126, "Sah10")

  /* Probability of > 30cm new snow amount in 12 hours: % (0..100) */
  val Sah30: MxParam = Value(127, "Sah30")

  /* Probability of > 10cm new snow amount in 24 hours: % (0..100) */
  val Sad10: MxParam = Value(128, "Sad10")

  /* Probability of > 30cm new snow amount in 24 hours: % (0..100) */
  val Sad30: MxParam = Value(129, "Sad30")

  /* Probability of > 50cm new snow amount in 24 hours: % (0..100) */
  val Sad50: MxParam = Value(130, "Sad50")

  /* Snow depth: m */
  val SnCv: MxParam = Value(131, "SnCv")

  /** VISIBILITY **/

  /* Visibility: m */
  val VV: MxParam = Value(132, "VV")

  /* Probability: Visibility below 1000m: % (0..100) */
  val VV10: MxParam = Value(133, "VV10")

  /** OTHER **/

  /* Past weather during the last 6 hours: - */
  val W1W2: MxParam = Value(134, "W1W2")

  /* Potential evapotranspiration within the last 24 hours: kg / m2 */
  val PEvap: MxParam = Value(135, "PEvap")

  /* Cloud base of convective clouds: m */
  val H_BsC: MxParam = Value(136, "H_BsC")

  /**
   * This public method provides the DWD forecast
   * data values that are relevant for PV output
   * forecasting
   */
  def getMxSolar:Seq[MxParam] = {
    Seq(
      /* Wind direction: 0°..360° */
      DD,
      /* Significant Weather: - */
      ww,
      /* Global Irradiance: kJ/m2 */
      Rad1h,
      /* Global irradiance within the last hour: % (0..80) */
      RRad1,
      /* Temperature 2m above surface: K */
      TTT,
      /* Wind speed: m/s */
      FF,
      /* Surface pressure, reduced: Pa */
      PPPP,
      /* Dewpoint 2m above surface: K */
      Td,
      /* Total cloud cover: % (0..100) */
      N)
  }
}
