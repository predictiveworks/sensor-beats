package de.kp.works.beats.sensor.netvox.enums

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

object NvTables extends Enumeration {

  type NvTable = Value

  val BatteryChange:NvTable      = Value(1, "BatteryChange")
  val Cmd:NvTable                = Value(2, "Cmd")
  val Datecode:NvTable           = Value(3, "Datecode")
  val Device:NvTable             = Value(4, "Device")
  val DisassembledAlarm:NvTable  = Value(5, "DisassembledAlarm")
  val Humi:NvTable               = Value(6, "Humi")
  val HumiChange:NvTable         = Value(7, "HumiChange")
  val HWver:NvTable              = Value(8, "HWver")
  val Illuminance:NvTable        = Value(9, "Illuminance")
  val IlluminanceChange: NvTable = Value(10, "IlluminanceChange")
  val IRDetectionTime:NvTable    = Value(11, "IRDetectionTime")
  val IRDisableTime:NvTable      = Value(12, "IRDisableTime")
  val MaxTime:NvTable            = Value(13, "MaxTime")
  val MinTime: NvTable           = Value(14, "MinTime")
  val Occupy:NvTable             = Value(15, "Occupy")
  val Status:NvTable             = Value(16, "Status")
  val SWver: NvTable             = Value(17, "SWver")
  val Temp: NvTable              = Value(18, "Temp")
  val TempChange:NvTable         = Value(19, "TempChange")
  val Volt:NvTable               = Value(20, "Volt")

}
