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

import com.google.gson.{JsonArray, JsonObject}
import org.apache.spark.sql.DataFrame

import scala.collection.JavaConversions.iterableAsScalaIterable

/**
 * Aggregated Gas Storage Inventory: https://agsi.gie.eu/
 *
 * Documentation:
 *
 * https://www.gie.eu/transparency-platform/GIE_API_documentation_v006.pdf
 *
 * This API service is focused on gas storage operators
 * and facilities, and supports the following information
 * elements:
 *
 * - Datetime
 * - Geographic Area (EU, Non-EU)
 * - Country
 * - Company
 * - Facility (Storage)
 * - Storage Volume
 * - Current Storage
 * - Storage Trend
 * - Injection
 * - Withdrawal
 * - Consumption (reference)
 */
object AgsiClient extends BaseClient {

  private val folder = ""

  def main(args:Array[String]):Unit = {

//    val from = "2021-09-12"
//    val to   = "2022-09-13"
//
//    val ds = getCountry("EU", from,to)
//    ds.show

    val ds = getSnapshot(reload = true)
    ds.show

    println(ds.count)
    println(ds.schema)
    //getCountry("EU")
    System.exit(0)
  }

  def getNews(reload:Boolean):DataFrame = {

    val path = s"${folder}asgi.news.json"
    val stored = readDF(path)

    val request = stored.isEmpty || reload
    if (request) {
      /*
       * Determine the current request delay to not
       * exceed the account's request limit
       */
      val delay = GIELimiter.getDelay
      Thread.sleep(delay)

      val endpoint = s"${AgsiDefs.API_URL}/news"

      val bytes = get(endpoint, AgsiDefs.AUTH_HEADER)
      val json = extractJsonBody(bytes)

      val dots = extractDots(json, Some("data"))
      writeDF(dots, path)

    } else stored

  }

  /**
   * This method retrieves a snapshot of EU
   * and Non-EU gas storages.
   *
   * This is provided as 2 nested datasets,
   * with the following sub structure
   *
   * - total (e.g., EU)
   * -- country
   * --- company
   * ---- facilities
   */
  def getSnapshot(reload:Boolean, date:Option[String]=None):DataFrame = {

    val base = s"${folder}asgi.snapshot"
    val stored = readSnapshot(base, date)

    val request = stored.isEmpty || reload
    if (request) {
      /*
       * Determine the current request delay to not
       * exceed the account's request limit
       */
      val delay = GIELimiter.getDelay
      Thread.sleep(delay)

      val endpoint = s"${AgsiDefs.API_URL}"

      val bytes = get(endpoint, AgsiDefs.AUTH_HEADER)
      val json = extractJsonBody(bytes)
      /*
       * The snapshot dataset is a nested dataset
       * and must be flattened before it can be
       * transformed into a dataframe
       */
      val flattened = new JsonArray
      json.getAsJsonObject.get("data")
        .getAsJsonArray.foreach(area => {
        /*
         * The area is either `EU` or `Non-EU`
         */
        val areaJson = area.getAsJsonObject
        areaJson.addProperty("type", "area")
        areaJson.addProperty("parent", "")

        val countries = areaJson
          .remove("children")
          .getAsJsonArray

        flattened.add(areaJson)

        countries.foreach(country => {

          val countryJson = country.getAsJsonObject
          countryJson.addProperty("type", "country")
          countryJson.addProperty("parent", areaJson.get("code").getAsString)

          val companies = countryJson
            .remove("children")
            .getAsJsonArray

          flattened.add(countryJson)

          companies.foreach(company => {

            val companyJson = company.getAsJsonObject
            companyJson.addProperty("type", "company")
            companyJson.addProperty("parent", countryJson.get("code").getAsString)

            val facilities = companyJson
              .remove("children")
              .getAsJsonArray

            flattened.add(companyJson)

            facilities.foreach(facility => {

              val facilityJson = facility.getAsJsonObject
              facilityJson.addProperty("type", "facility")
              facilityJson.addProperty("parent", companyJson.get("code").getAsString)

              flattened.add(facilityJson)

            })
          })

        })
      })
      /*
       * consumption & consumptionFull fields are
       * not provided below country level
       *
       * Note: Apache Spark harmonizes missing
       * fields (e.g., consumption) and adds
       * `null`values
       */
      val dots = extractDots(flattened)
      writeDF(dots, s"$base.json")

    } else stored

  }
  /**
   * This method retrieves all registered companies
   * and their associated storage facilities
   */
  def getCompanies(reload:Boolean):DataFrame = {

    val path = s"${folder}asgi.companies.json"
    val stored = readDF(path)

    val request = stored.isEmpty || reload
    if (request) {
      /*
       * Determine the current request delay to not
       * exceed the account's request limit
       */
      val delay = GIELimiter.getDelay
      Thread.sleep(delay)

      val endpoint = s"${AgsiDefs.API_URL}/about?show=listing"

      val bytes = get(endpoint, AgsiDefs.AUTH_HEADER)
      val json = extractJsonBody(bytes)
      /*
       * Fields:
       *
       * - country
       * - eic
       * - facilities
       * - name
       * - short_name
       * - type
       * - url
       */
      val dots = extractDots(json)
      writeDF(dots, path)

    } else stored

  }

  /**
   * Get the aggregated historical data export for a specific
   * country.
   *
   * country_code Two digit country code. Ex: NL, DE, DK, SE,
   * FI etc.
   */
  def getCountry(country_code:String,from:String,to:String):DataFrame = {

    val basePath = s"${folder}asgi.country.${country_code.toLowerCase}"
    val oldMeta = getMetadata(s"$basePath.metadata")

    val (request, reqFrom, reqTo) = computeInterval(from, to, oldMeta)

    if (!request) {
      session.read.json(s"$basePath.json")
    }
    else {
      /*
       * Note: The oldest available data point is from
       * 2012-09-12 (10 years)
       *
       * The total amount of data points is 300; therefore,
       * to retrieve more data, a sequence of requests must
       * be initiated
       *
       * Any API account that exceeds 60 API calls per minute
       * will automatically be put in a waiting queue and get
       * a 'too many requests' error message for a duration of
       * 60 seconds, each time.
       *
       * We will also monitor the accounts in this queue and if
       * data calls are not adjusted properly and timely, we will
       * affect a permanent ban on these accounts, by placing these
       * accounts on our blacklist as 'DDOS attack' users.
       *
       * Do to this rate limit, this request cannot be offered as
       * part of a data product without leveraging a large amount
       * of accounts.
       */
      val endpoint = s"${AgsiDefs.API_URL}/data/$country_code?from=$reqFrom&to=$reqTo&size=300"
      val dots = getDots(endpoint)

      if (dots.isEmpty)
        return session.emptyDataFrame

      /*
       * Sample row:
       *
       * {
       *  "name":"EU",
       *  "code":"eu",
       *  "url":"eu",
       *  "gasDayStart":"2022-09-10",
       *
       *  --  storage info --
       *
       *  "gasInStorage":"931.2735",      [TWh]
       *  "workingGasVolume":"1113.6025", [TWh] (maximum)
       *
       *  "full":"83.63",            [%]
       *  "trend":"0.35",            [%]
       *
       *  --  injection info --
       *
       *  "injection":"4251.15",          [GWh/d]
       *  "injectionCapacity":"11752.33", [GWh/d]
       *
       *  -- withdrawal info --
       *
       *  "withdrawal":"396.6",           [GWh/d]
       *  "withdrawalCapacity":"19893.44",[GWh/d]
       *
       *  --  consumption info --
       *
       *  "consumption":"4151.8372",      [TWh] (for 2020)
       *  "consumptionFull":"22.43",      [%] (Storage level compared to consumption)
       *  "status":"E",
       *  "info":[]
       *  }
       */
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
   * This method retrieves a timeseries (per day)
   * for a certain storage company.
   *
   * Note: The company must be specified by its
   * EIC code
   */
  def getCompany(country:String, company:String, from:String,to:String):DataFrame = {

    val basePath = s"${folder}asgi.company.${country.toLowerCase}.$company"
    val oldMeta = getMetadata(s"$basePath.metadata")

    val (request, reqFrom, reqTo) = computeInterval(from, to, oldMeta)
    if (!request) {
      session.read.json(s"$basePath.json")
    }
    else {
      /*
       * The endpoint is taken from the ASGI+ website;
       * an alternative endpoint is:
       *
       * s"${AgsiDefs.API_URL}/data/$company/$country"
       */
      val endpoint = s"${AgsiDefs.API_URL}?company=$company&country=$country" +
          s"&from=$reqFrom&to=$reqTo&size=3000"

      val dots = getDots(endpoint)

      if (dots.isEmpty)
        return session.emptyDataFrame

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
   * This method retrieves a timeseries (per day)
   * for a certain storage facility
   */
  def getFacility(country:String, company:String, facility:String,from:String,to:String):DataFrame = {

    val basePath = s"${folder}asgi.facility.${country.toLowerCase}.$company.$facility"
    val oldMeta = getMetadata(s"$basePath.metadata")

    val (request, reqFrom, reqTo) = computeInterval(from, to, oldMeta)
    if (!request) {
      session.read.json(s"$basePath.json")
    }
    else {

      val endpoint = s"${AgsiDefs.API_URL}?facility=$facility&company=$company&country=$country" +
        s"&from=$reqFrom&to=$reqTo&size=3000"

      val dots = getDots(endpoint)

      if (dots.isEmpty)
        return session.emptyDataFrame

      val output = writeDF(dots, s"$basePath.json")
      /*
       * Build and persist metadata
       */
      val metadata = new JsonObject
      metadata.addProperty("country_code",  country)
      metadata.addProperty("company_code",  company)
      metadata.addProperty("facility_code", facility)

      metadata.addProperty("from", reqFrom)
      metadata.addProperty("to", reqTo)

      writeMetadata(metadata, s"$basePath.metadata")

      output

    }
  }
  /**
   * The unavailability reports are a repeating group of entries
   * for the planned or proposed outage of a facility by Storage
   * System Operators, detailing the unavailability type and the
   * period and quantity of unavailability.
   *
   * Each Storage system operator identifies the dates and time on
   * which the planned or unplanned outages of the Storage facility
   * occur and the capacity which is affected.
   */
  def getUnavailability(from:String,to:String):DataFrame = {

    val basePath = s"${folder}asgi.unavailability"
    val oldMeta = getMetadata(s"$basePath.metadata")

    val (request, reqFrom, reqTo) = computeInterval(from, to, oldMeta)
    if (!request) {
      session.read.json(s"$basePath.json")
    }
    else {

      val endpoint = s"${AgsiDefs.API_URL}/unavailability?from=$reqFrom&to=$reqTo&size=3000"
      val dots = getDots(endpoint)

      if (dots.isEmpty)
        return session.emptyDataFrame

      val output = writeDF(dots, s"$basePath.json")
      /*
       * Build and persist metadata
       */
      val metadata = new JsonObject

      metadata.addProperty("from", reqFrom)
      metadata.addProperty("to", reqTo)

      writeMetadata(metadata, s"$basePath.metadata")

      output

    }

  }
  /**
   * This method retrieves the aggregated gas storage
   * data for Europe in a certain time period.
   *
   * Note, the same information can also be retrieved
   * via method `getCountry`, even if the API endpoint
   * is different.
   */
  def getEU(from:String,to:String):DataFrame = {

    val basePath = s"${folder}asgi.country.eu"
    val oldMeta = getMetadata(s"$basePath.metadata")

    val (request, reqFrom, reqTo) = computeInterval(from, to, oldMeta)
    if (!request) {
      session.read.json(s"$basePath.json")
    }
    else {
      /*
       * Note: The oldest available data point is from
       * 2012-09-12 (10 years)
       */
      val endpoint = s"${AgsiDefs.API_URL}?continent=EU&from=$reqFrom&to=$reqTo&size=3000"
      val dots = getDots(endpoint)

      if (dots.isEmpty)
        return session.emptyDataFrame

      /*
       * Sample row:
       *
       * {
       *  "name":"EU",
       *  "code":"eu",
       *  "url":"eu",
       *  "gasDayStart":"2022-09-10",
       *
       *  --  storage info --
       *
       *  "gasInStorage":"931.2735",      [TWh]
       *  "workingGasVolume":"1113.6025", [TWh] (maximum)
       *
       *  "full":"83.63",            [%]
       *  "trend":"0.35",            [%]
       *
       *  --  injection info --
       *
       *  "injection":"4251.15",          [GWh/d]
       *  "injectionCapacity":"11752.33", [GWh/d]
       *
       *  -- withdrawal info --
       *
       *  "withdrawal":"396.6",           [GWh/d]
       *  "withdrawalCapacity":"19893.44",[GWh/d]
       *
       *  --  consumption info --
       *
       *  "consumption":"4151.8372",      [TWh] (for 2020)
       *  "consumptionFull":"22.43",      [%] (Storage level compared to consumption)
       *  "status":"E",
       *  "info":[]
       *  }
       */
      val output = writeDF(dots, s"$basePath.json")
      /*
       * Build and persist metadata
       */
      val metadata = new JsonObject
      metadata.addProperty("country_code", "EU")

      metadata.addProperty("from", reqFrom)
      metadata.addProperty("to", reqTo)

      writeMetadata(metadata, s"$basePath.metadata")

      output

    }

  }
  /**
   * A helper method to retrieve data from
   * the ASGI+ API in a paginated approach.
   *
   * This can be a longer-running method as
   * the inter request delays depend on the
   * current request rate.
   */
  private def getDots(endpoint:String):List[String] = {
    /*
     * First request
     */
    var page = 1
    /*
     * Determine the current request delay to not
     * exceed the account's request limit
     */
    var delay = GIELimiter.getDelay
    Thread.sleep(delay)

    var bytes = get(endpoint + s"&page=$page", AgsiDefs.AUTH_HEADER)
    var json = extractJsonBody(bytes)
    /*
     * Retrieve data from the first request
     */
    var dots = extractDots(json, Some("data"))
    val last_page = json.getAsJsonObject.get("last_page").getAsInt

    if (last_page == 0)
    /*
     * This indicates that the request was
     * not correct; in this case return an
     * empty dataframe and shortcut the subsequent
     * handling
     */
      return List.empty[String]

    /*
     * Follow-on requests
     */
    page += 1
    while (page <= last_page) {

      delay = GIELimiter.getDelay
      Thread.sleep(delay)

      bytes = get(endpoint + s"&page=$page", AgsiDefs.AUTH_HEADER)
      json = extractJsonBody(bytes).getAsJsonObject

      dots = dots ++ extractDots(json, Some("data"))
      page += 1

    }

    dots

  }

  /**
   * A helper method to determine whether
   * snapshot information for the provided
   * data already exist
   */
  private def readSnapshot(path:String, date:Option[String]):DataFrame = {

    var stored = session.emptyDataFrame
    if (date.isEmpty) return stored

    val meta = getMetadata(s"$path.metadata")
    if (meta.size() > 0) {

      val date_old = DATE_FORMAT.parse(meta.get("date").getAsString)
      val date_new = DATE_FORMAT.parse(date.get)

      val date_cmp = date_new.compareTo(date_old)
      if (date_cmp == 0) stored = readDF(s"$path.json")

    }

    stored

  }

}
