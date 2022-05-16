package de.kp.works.beats.sensor.loriot

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

/*
 * An "Uplink Data Message" is a LoRaWAN message received from a registered device
 * on the Network Server which is sent to the defined Application Output.
 *
 * Every received message will be sent the defined endpoint in the configured verbosity
 * in a JSON format.
 *
 * Every uplink JSON message corresponds to one frame produced by your device, It contains
 * not only the raw application payload, but also additional meta-data.
 *
 * If APPSKEY is assigned to a device, the data field will be populated with decrypted data,
 * and encdata will be omitted.
 *
 * If APPSKEY is missing, the encdata field will be populated with encrypted payload, and data
 * will be omitted.
 *
 * Basic Verbosity:
 *
 * {
 * cmd      : 'rx';    // identifies type of message, rx = uplink message
 * EUI      : string;  // device EUI, 16 hex digits (without dashes)
 * ts       : number;  // server timestamp as number (milliseconds from Linux epoch)
 * ack      : boolean; // acknowledgement flag as set by device
 * bat      : number;  // device battery status, response to the DevStatusReq LoRaWAN MAC Command
 * fcnt     : number;  // frame counter, a 32-bit number
 * port     : number;  // port as sent by the end device
 *
 * encdata? : string;  // data payload (APPSKEY encrypted hex string)
 *                     // only present if APPSKEY is not assigned to device
 *
 * data?    : string;  // data payload (decrypted, plaintext hex string)
 *                     // only present if APPSKEY is assigned to device
 * }
 *
 * Extended Verbosity:
 *
 * The LORIOT Network Server can deliver extended radio information to your application.
 * The extended radio information is reported as seen by the first gateway that received
 * the frame.
 *
 * freq     : number;  // radio frequency at which the frame was received, in Hz
 * dr       : string;  // radio data rate - spreading factor, bandwidth and coding rate
 *                     // e.g. SF12 BW125 4/5
 *                     // UPDATED: previously the field was called 'sf'
 * rssi     : number;  // frame rssi, in dBm, as integer number
 * snr      : number;  // frame snr, in dB, one decimal place
 */
case class LoriotUplink(
  /*
   * BASIC VERBOSITY
   */
  cmd: String = "rx",
  EUI: String,
  /* [Long] */
  ts: Number,
  ack :Boolean,
  /* [Int] */
  bat: Number,
  /* [Int] */
  fcnt: Number,
  /* [Int] */
  port:Number,
  encdata: Option[String] = None,
  data: Option[String] = None,
  /*
   * EXTENDED VERBOSITY
   */
  /* [Int] */
  freq:Option[Number] = None,
  dr: Option[String] = None,
  /* [Int] */
  rssi: Option[Number] = None,
  /* [Double] */
  snr: Option[Number] = None)