package de.kp.works.beats.sensor.sensecap.enums

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

object ScTables extends Enumeration {
  type ScTable = Value

  val AI_Detection_01:ScTable                    = Value(1, "AI_Detection_01")
  val AI_Detection_02:ScTable                    = Value(2, "AI_Detection_02")
  val AI_Detection_03:ScTable                    = Value(3, "AI_Detection_03")
  val AI_Detection_04:ScTable                    = Value(4, "AI_Detection_04")
  val AI_Detection_05:ScTable                    = Value(5, "AI_Detection_05")
  val AI_Detection_06:ScTable                    = Value(6, "AI_Detection_06")
  val AI_Detection_07:ScTable                    = Value(7, "AI_Detection_07")
  val AI_Detection_08:ScTable                    = Value(8, "AI_Detection_08")
  val AI_Detection_09:ScTable                    = Value(9, "AI_Detection_09")
  val AI_Detection_10:ScTable                    = Value(10, "AI_Detection_10")
  val AirHumidity:ScTable                        = Value(11, "AirHumidity")
  val AirTemperature:ScTable                     = Value(12, "AirTemperature")
  val BarometricPressure:ScTable                 = Value(13, "BarometricPressure")
  val CO2:ScTable                                = Value(14, "CO2")
  val DissolvedOxygen:ScTable                    = Value(15, "DissolvedOxygen")
  val Distance:ScTable                           = Value(16, "Distance")
  val ElectricalConductivity:ScTable             = Value(17, "ElectricalConductivity")
  val FlowRate:ScTable                           = Value(18, "FlowRate")
  val H2S:ScTable                                = Value(19, "H2S")
  val LeafTemperature:ScTable                    = Value(20, "LeafTemperature")
  val LeafWetness:ScTable                        = Value(21, "LeafWetness")
  val LightIntensity:ScTable                     = Value(22, "LightIntensity")
  val LightQuantum:ScTable                       = Value(23, "LightQuantum")
  val LiquidLevel:ScTable                        = Value(24, "LiquidLevel")
  val NH3:ScTable                                = Value(25, "NH3")
  val Noise:ScTable                              = Value(26, "Noise")
  val OxygenConcentration:ScTable                = Value(27, "OxygenConcentration")
  val pH:ScTable                                 = Value(28, "pH")
  val PhotosyntheticallyActiveRadiation:ScTable  = Value(29, "PhotosyntheticallyActiveRadiation")
  val PM2_5:ScTable                              = Value(30, "PM2_5")
  val PM10:ScTable                               = Value(31, "PM10")
  val RainfallHourly:ScTable                     = Value(32, "RainfallHourly")
  val Salinity:ScTable                           = Value(33, "Salinity")
  val SoilHeatFlux:ScTable                       = Value(34, "SoilHeatFlux")
  val SoilMoisture:ScTable                       = Value(35, "SoilMoisture")
  val SoilMoisture_10cm:ScTable                  = Value(36, "SoilMoisture_10cm")
  val SoilMoisture_20cm:ScTable                  = Value(37, "SoilMoisture_20cm")
  val SoilMoisture_30cm:ScTable                  = Value(38, "SoilMoisture_30cm")
  val SoilMoisture_40cm:ScTable                  = Value(39, "SoilMoisture_40cm")
  val SoilTension:ScTable                        = Value(40, "SoilTension")
  val SoilTemperature:ScTable                    = Value(41, "SoilTemperature")
  val SoilTemperature_10cm:ScTable               = Value(42, "SoilTemperature_10cm")
  val SoilTemperature_20cm:ScTable               = Value(43, "SoilTemperature_20cm")
  val SoilTemperature_30cm:ScTable               = Value(44, "SoilTemperature_30cm")
  val SoilTemperature_40cm:ScTable               = Value(45, "SoilTemperature_40cm")
  val SoilVolumetricWaterContent:ScTable         = Value(46, "SoilVolumetricWaterContent")
  val SunshineDuration:ScTable                   = Value(47, "SunshineDuration")
  val TDS:ScTable                                = Value(48, "TDS")
  val TotalFlow:ScTable                          = Value(49, "TotalFlow")
  val TotalSolarRadiation:ScTable                = Value(50, "TotalSolarRadiation")
  val UVIndex:ScTable                            = Value(51, "UVIndex")
  val WaterElectricalConductivity:ScTable        = Value(52, "WaterElectricalConductivity")
  val WaterLeak:ScTable                          = Value(53, "WaterLeak")
  val WaterSurfaceEvaporation:ScTable            = Value(54, "WaterSurfaceEvaporation")
  val WaterTemperature:ScTable                   = Value(55, "WaterTemperature")
  val WindDirection:ScTable                      = Value(56, "WindDirection")
  val WindSpeed:ScTable                          = Value(57, "WindSpeed")

}
