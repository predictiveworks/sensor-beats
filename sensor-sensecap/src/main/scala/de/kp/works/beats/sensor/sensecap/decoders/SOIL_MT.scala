package de.kp.works.beats.sensor.sensecap.decoders

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
import de.kp.works.beats.sensor.sensecap.enums.ScFields.{SoilTemperature, SoilVolumetricWaterContent}

object SOIL_MT extends CommonDecoder {
  /*
   * SenseCAP Wireless Soil Moisture & Temperature Sensor measures
   * soil volumetric water content (VWC) and soil temperature at the
   * range of 0 ~ 100% (m³/m³) and -30 ~ 70° Celsius respectively.
   *
   * With the high-quality soil moisture and temperature probe,
   * this sensor features high precision and sensitivity regardless
   * of soil variability, making it widely applicable in industrial
   * IoT (IIoT) scenarios such as water-saving irrigation, outdoor
   * fields, greenhouses, and more.
   */
  override def fields: Seq[String] =
    Seq(
      SoilVolumetricWaterContent,
      SoilTemperature
    )
}
