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

object Measurements {
  /**
   * https://sensecap-docs.seeed.cc/sensor_types_list.html
   */
  val mappings = Map(
    4097 -> "AirTemperature",
    4098 -> "AirHumidity",
    4099 -> "LightIntensity",
    4100 -> "CO2",
    4101 -> "BarometricPressure",
    4102 -> "SoilTemperature",
    4103 -> "SoilMoisture",
    4104 -> "WindDirection",
    4105 -> "WindSpeed",
    4106 -> "pH",
    4107 -> "LightQuantum",
    4108 -> "ElectricalConductivity",
    4109 -> "DissolvedOxygen",
    4110 -> "SoilVolumetricWaterContent",
    4113 -> "RainfallHourly",
    4115 -> "Distance",
    4116 -> "WaterLeak",
    4117 -> "LiquidLevel",
    4118 -> "NH3",
    4119 -> "H2S",
    4120 -> "FlowRate",
    4121 -> "TotalFlow",
    4122 -> "OxygenConcentration",
    4123 -> "WaterElectricalConductivity",
    4124 -> "WaterTemperature",
    4125 -> "SoilHeatFlux",
    4126 -> "SunshineDuration",
    4127 -> "TotalSolarRadiation",
    4128 -> "WaterSurfaceEvaporation",
    4129 -> "PhotosyntheticallyActiveRadiation",
    4133 -> "SoilTension",
    4134 -> "Salinity",
    4135 -> "TDS",
    4136 -> "LeafTemperature",
    4137 -> "LeafWetness",
    4138 -> "SoilMoisture_10cm",
    4139 -> "SoilMoisture_20cm",
    4140 -> "SoilMoisture_30cm",
    4141 -> "SoilMoisture_40cm",
    4142 -> "SoilTemperature_10cm",
    4143 -> "SoilTemperature_20cm",
    4144 -> "SoilTemperature_30cm",
    4145 -> "SoilTemperature_40cm",
    4146 -> "PM2_5",
    4147 -> "PM10",
    4148 -> "Noise",
    4175 -> "AI_Detection_01",
    4176 -> "AI_Detection_02",
    4177 -> "AI_Detection_03",
    4178 -> "AI_Detection_04",
    4179 -> "AI_Detection_05",
    4180 -> "AI_Detection_06",
    4181 -> "AI_Detection_07",
    4182 -> "AI_Detection_08",
    4183 -> "AI_Detection_09",
    4184 -> "AI_Detection_10",
    4190 -> "UVIndex"

  )
}
