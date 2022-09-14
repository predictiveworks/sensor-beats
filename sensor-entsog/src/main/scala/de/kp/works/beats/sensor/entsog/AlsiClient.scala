package de.kp.works.beats.sensor.entsog

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

import com.google.gson.JsonObject
import org.apache.spark.sql.DataFrame

/**
 * Aggregated LNG Storage Inventory
 *
 * Data definitions:
 *
 * LNG INVENTORY:
 * Aggregated amount of LNG in the LNG tanks at the end of the
 * previous gas day. Reported in volume (103 m3 LNG) - m3 being
 * expressed at 0 degrees Celsius and 1 013 mbar)
 *
 * SEND-OUT:
 * The aggregated gas flow out of the LNG facility within the gas
 * day. Reported in energy units (GWh/d)
 *
 * DTMI:
 * Declared Total Maximum Inventory. Reported in volume (103 m3 LNG) - m3
 * being expressed at 0 degrees Celsius and 1 013 mbar)
 *
 * DTRS:
 * Declared Total Reference Send Out. Reported in energy units (GWh/d)
 */
object AlsiClient extends BaseClient {

  private val folder = ""

  val COMPANY_LISTING = s"${AlsiDefs.API_URL}/countries/type/LSO/companies"

  def getCompany(country:String, company:String, from:String,to:String):DataFrame = {

    val basePath = s"${folder}alsi.company.${country.toLowerCase}.$company"
    val oldMeta = getMetadata(s"$basePath.metadata")

    val (request, reqFrom, reqTo) = computeInterval(from, to, oldMeta)
    if (!request) {
      session.read.json(s"$basePath.json")
    }
    else {
      /*
       * Sample: https://alsi.gie.eu/api/data/21X000000001006T/be
       */
      val endpoint = s"${AlsiDefs.API_URL}/data/$company/${country.toLowerCase}?from=$reqFrom&to=$reqTo"
      /*
       * Determine the current request delay to not
       * exceed the account's request limit
       */
      val delay = GIELimiter.getDelay
      Thread.sleep(delay)

      val bytes = get(endpoint, AgsiDefs.AUTH_HEADER)
      val json = extractJsonBody(bytes)

      val dots = extractDots(json)

      if (dots.isEmpty)
        return session.emptyDataFrame
      /*
       * {
       *     "status": "C",
       *     "gasDayStartedOn": "2022-09-12",
       *     "lngInventory": "339.10",
       *     "sendOut": "4.4",
       *     "dtmi": "617.20",
       *     "dtrs": "541.0",
       *     "info": []
       * }
       */
      val output = writeDF(dots, s"$basePath.json")
      /*
       * Build and persist metadata
       */
      val metadata = new JsonObject
      metadata.addProperty("country_code", country)
      metadata.addProperty("company_code", company)

      metadata.addProperty("from", reqFrom)
      metadata.addProperty("to", reqTo)

      writeMetadata(metadata, s"$basePath.metadata")

      output

    }
  }
  /**
   * Timeseries of LNG data for a specific country
   */
  def getCountry(country_code:String, from:String, to:String):DataFrame = {

    val basePath = s"${folder}alsi.country.${country_code.toLowerCase}"
    val oldMeta = getMetadata(s"$basePath.metadata")

    val (request, reqFrom, reqTo) = computeInterval(from, to, oldMeta)

    if (!request) {
      session.read.json(s"$basePath.json")
    }
    else {
      /*
       * Determine the current request delay to not
       * exceed the account's request limit
       */
      val delay = GIELimiter.getDelay
      Thread.sleep(delay)

      val endpoint = s"${AlsiDefs.API_URL}/data/${country_code.toLowerCase()}?from=$reqFrom&to=$reqTo"

      val bytes = get(endpoint, AgsiDefs.AUTH_HEADER)
      val json = extractJsonBody(bytes)
      /*
       * Fields:
       *
       * - name
       * - short_name
       * - type
       * - eic
       * - country
       * - facilities
       */
      val dots = extractDots(json)
      val output = writeDF(dots, s"$basePath.json")
      /*
       * Build and persist metadata
       */
      val metadata = new JsonObject
      metadata.addProperty("country_code", country_code)

      metadata.addProperty("from", reqFrom)
      metadata.addProperty("to", reqTo)

      writeMetadata(metadata, s"$basePath.metadata")

      output

    }

  }
  /**
   * Method to get list of EIC units, i.e., companies
   * and their operated facilities
   */
  def getCompanies(reload:Boolean):DataFrame = {

    val path = s"${folder}alsi.companies.json"
    val stored = readDF(path)

    val request = stored.isEmpty || reload
    if (request) {
      /*
       * Determine the current request delay to not
       * exceed the account's request limit
       */
      val delay = GIELimiter.getDelay
      Thread.sleep(delay)

      val endpoint = s"${AlsiDefs.API_URL}/eic-listing/LSO/view"

      val bytes = get(endpoint, AgsiDefs.AUTH_HEADER)
      val json = extractJsonBody(bytes)
      /*
       * Fields:
       *
       * - name
       * - short_name
       * - type
       * - eic
       * - country
       * - facilities
       */
      val dots = extractDots(json)
      writeDF(dots, path)

    } else stored

  }

}