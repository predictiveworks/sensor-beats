package de.kp.works.beats.sensor.helium

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

case class HeliumHotspot(
  /* In MHz, the frequency which the packet was received upon */
  frequency:Double,
  /*
   * A base58 encoding of the hotspot's public key; this may
   * serve as a concise unique identifier
   */
  id:	String,
  /*
   * A human-friendly three-word encoding of the hotspot's
   * public key; link to JavaScript and Erlang implementations
   */
  name:	String,
  /* Timestamp in milliseconds */
  reported_at: Long,
  /*
   * Received Signal Strength Indicator is reported by the hotspot
   * and indicates how strong the signal device's radio signal was;
   * the larger the number (closer to 0), the better the signal
   *
   * [Int]
   */
  rssi:	Int,
  /*
   * In dB, Signal to Noise Ratio is reported by the hotspot to
   * indicate how clear the signal was relative to environmental
   * noise; this generally ranges between -20 and +10 and the larger
   * the number (closer to 10 dB) the better
   */
  snr: Double,
  /*
   * LoRa Spreading Factor and Bandwidth used for the radio transmission.
   * In the US, spreading factor ranges from 7 to 10 and bandwidth is always
   * 125 kHz.
   *
   * For example, "SF7BW125" means a Spreading Factor of 7 was used and a
   * channel width of 125 kHz.
   */
  spreading: String,
  status:String
)

/*
 * HINT: No documentation available on the Helium documentation page
 */
case class HeliumLabel(
  id: String,
  name: String,
  organization_id: String)

case class HeliumMetadata(
  labels: Seq[HeliumLabel]
)

case class HeliumUplink(
  /*
   * LoRaWAN 64-bit Application Identifier (AppEUI) in MSB hex; conventionally
   * this is used to identify a type of sensor or actuator, e.g. "temperature
   * sensor" or "valve controller"
   */
  app_eui: String,
  /*
   * LoRaWAN 64-bit Device Identifier (DevEUI) in MSB hex; conventionally this
   * is used to identify a unique device within a specific application (AppEUI)
   * or even within an entire organization
   */
  dev_eui: String,
  /*
   * Upon a successful Join, the device is allocated a LoRaWAN 64-bit DevAddr
   * which is unique across the entire Helium Network as long the session is
   * active; the data is represented here in MSB hex
   */
  devaddr: String,
  /*
   * LoRaWAN FCnt for uplink packets; this increments with every uplink packet
   * and can be useful for surmising whether an uplink was missed
   *
   * [Int]
   */
  fcnt:	Number,
  /*
   * Array of hotspots which received the same packet
   */
  hotspots: Seq[HeliumHotspot],
  /*
   * UUID used to identify the device within Helium Console; this is assigned
   * during device creation and maps to a unique set of AppEui, DevEui, and AppKey
   */
  id: String,
  /*
   * A nested JSON object with a single field, "labels", which is an array of
   * label Objects
   */
  metadata: HeliumMetadata,
  /*
   * Name assigned to the device by Console user
   */
  name: String,
  /*
   * Data transmitted by your device as a base64 encoded String.
   * The decoder of the base64 decoded payload is the same as for TTN.
   */
  payload: String,
  /*
   * Size of the payload in bytes; you can use this to verify parsing output
   *
   * [Int]
   */
  payload_size: Number,
  /*
   * LoRaWAN FPort on which the data was transmitted; this can be useful for
   * routing data within the application
   *
   * [Int]
   */
  port: Number,
  /*
   * Timestamp in milliseconds
   */
  reported_at: Long)
