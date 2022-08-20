package de.kp.works.beats.sensor.entsoe

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

import de.kp.works.beats.sensor.http.HttpConnect

import java.time.{Instant, ZoneId, ZonedDateTime}
import java.util.Calendar

object EntsoeSession {

  /**
   * This method transforms the request parameters
   * into a unique identifier to enable timeseries
   * retrieval from the [EntsoeStore]
   */
  def buildSessionKey(params:Map[String,String]):String = {

    val token = params.map{case(k,v) => s"$k=$v"}.mkString("&")
    val sessionKey = java.util.UUID.fromString(token).toString

    sessionKey

  }

  def registerSeries(sessionKey:String, timeSeries:EntsoeSeries):Unit = {
    EntsoeStore.register(sessionKey, timeSeries)
  }

}

/**
 * The ENTSOE API ships with a limit of requests.
 * From their website:
 *
 * Maximum of 400 requests are accepted per user
 * per minute.
 *
 * Count of requests is performed based on per IP
 * address and per security token. Reaching of 400
 * query/minute limit through an unique IP address
 * or security token will result in a temporary ban
 * of 10 minutes.
 *
 * When a user reaches the limit, requests coming from
 * the user will be redirected to the different port 81
 * (8443) which returns "HTTP Status 429 - TOO MANY REQUESTS"
 * with message text "Max allowed requests per minute from
 * each unique IP is max up to 400 only".
 */
object EntsoeClient extends HttpConnect {

  private val SECURITY_TOKEN = ""
  /**
   * The ENTSOE API leverages UTC time
   */
  private val ZONE_ID = ZoneId.of( "UTC")

  def main(args:Array[String]):Unit = {

    val domain = "10Y1001A1001A83F" // DE
    println(dayAheadLoadRequest(domain))

    System.exit(0)
  }
  /**
   * This is the main method to retrieve a certain
   * time series from the ENTSOE API
   */
  def getRequest(params:Map[String,String]):EntsoeSeries = {

    val endpoint = EntsoeDefs.ENTSOE_ENDPOINT + "?" + params.map{case(k,v) => s"$k=$v"}.mkString("&")

    val source = get(endpoint)
    val document = extractTextBody(source)

    val timeSeries = EntsoeXML.getSeries(document)
    /*
     * Build session key and register the
     * retrieved time series
     */
    val sessionKey = EntsoeSession.buildSessionKey(params)
    EntsoeSession.registerSeries(sessionKey, timeSeries)

    timeSeries

  }
  /**
   * Public method to retrieve the actual aggregated
   * electricity
   */
  def productionRequest(domain:String, productionUnit:Option[String]):Option[EntsoeSeries] = {

    try {
      /*
       * For most of the data items in the generation domain,
       * the PsrType parameter is optional. When this parameter
       * is not used, the API returns all available data for each
       * production type for the queried interval and area.
       *
       * If the parameter is used, data will be returned only for
       * the specific production type requested.
       */
      val (periodStart, periodEnd) = buildPeriod(mode="realized")

      val params = if (productionUnit.isEmpty) {
        /*
         * Aggregated generation for each type = A75
         * Referring to the implementation guideline,
         * the `processType` is fixed = A16
         */
        val documentType = "A75"
        val processType  = "A16"

        Map(
          "documentType"  -> documentType,
          "processType"   -> processType,
          "in_Domain"     -> domain,
          "PeriodStart"   -> periodStart,
          "PeriodEnd"     -> periodEnd,
          "securityToken" -> SECURITY_TOKEN)

      }
      else {
        /*
         * Aggregated generation for each type = A73
         * Referring to the implementation guideline,
         * the `processType` is fixed = A16
         */
        val documentType = "A73"
        val processType  = "A16"

        Map(
          "documentType"  -> documentType,
          "processType"   -> processType,
          "in_Domain"     -> domain,
          "psrType"       -> productionUnit.get,
          "PeriodStart"   -> periodStart,
          "PeriodEnd"     -> periodEnd,
          "securityToken" -> SECURITY_TOKEN)

      }

      val timeSeries = getRequest(params)
      Some(timeSeries)

    } catch {
      case _: Throwable => None
    }

  }

  def dayAheadProductionRequest(domain:String):Option[EntsoeSeries] = {

    try {
      /*
       * Generation forecast = A71. Referring to the
       * implementation guideline, the `processType`
       * is fixed = A01.
       *
       * Production units are not supported
       */
      val documentType = "A71"
      val processType  = "A01"

      val (periodStart, periodEnd) = buildPeriod(mode="dayahead")

      val params = Map(
        "documentType"  -> documentType,
        "processType"   -> processType,
        "in_Domain"     -> domain,
        "PeriodStart"   -> periodStart,
        "PeriodEnd"     -> periodEnd,
        "securityToken" -> SECURITY_TOKEN)

      val timeSeries = getRequest(params)
      Some(timeSeries)

    } catch {
      case _: Throwable => None
    }

  }

  def dayAheadWindSolarProductionRequest(domain:String):Option[EntsoeSeries] = {

    try {
      /*
       * Generation forecast = A69. Referring to the
       * implementation guideline, the `processType`
       * is fixed = A01.
       *
       * Production units are not supported
       */
      val documentType = "A69"
      val processType  = "A01"

      val (periodStart, periodEnd) = buildPeriod(mode="dayahead")

      val params = Map(
        "documentType"  -> documentType,
        "processType"   -> processType,
        "in_Domain"     -> domain,
        "PeriodStart"   -> periodStart,
        "PeriodEnd"     -> periodEnd,
        "securityToken" -> SECURITY_TOKEN)

      val timeSeries = getRequest(params)
      Some(timeSeries)

    } catch {
      case _: Throwable => None
    }

  }

  /**
   * Public method to retrieve load or consumption
   * data from the ENTSOE API
   */
  def loadRequest(domain: String):Option[EntsoeSeries] = {

    try {

      val documentType = "A65"
      val processType = "A16"

      val (periodStart, periodEnd) = buildPeriod(mode = "realized")

      val params = Map(
        "documentType"          -> documentType,
        "processType"           -> processType,
        "outBiddingZone_Domain" -> domain,
        "PeriodStart"           -> periodStart,
        "PeriodEnd"             -> periodEnd,
        "securityToken"         -> SECURITY_TOKEN)

      val timeSeries = getRequest(params)
      Some(timeSeries)

    } catch {
      case _: Throwable => None
    }

  }

  def dayAheadLoadRequest(domain:String):Option[EntsoeSeries] = {

    try {

      val documentType = "A65"
      val processType  = "A01"

      val (periodStart, periodEnd) = buildPeriod(mode="dayahead")

      val params = Map(
        "documentType"          -> documentType,
        "processType"           -> processType,
        "outBiddingZone_Domain" -> domain,
        "PeriodStart"           -> periodStart,
        "PeriodEnd"             -> periodEnd,
        "securityToken"         -> SECURITY_TOKEN)

      val timeSeries = getRequest(params)
      Some(timeSeries)

    } catch {
      case _:Throwable => None
    }

  }

  /**
   * Public method to retrieve the physical flow between
   * two countries
   */
  def exchangeRequest(inDomain:String, outDomain:String):Option[EntsoeSeries] = {

    try {
      /*
       * Physical flows have a fixed document type = A11,
       * and no process type is used
       */
      val documentType = "A11"

      val (periodStart, periodEnd) = buildPeriod(mode="realized")

      val params = Map(
        "documentType"  -> documentType,
        "in_Domain"     -> inDomain,
        "out_Domain"    -> outDomain,
        "PeriodStart"   -> periodStart,
        "PeriodEnd"     -> periodEnd,
        "securityToken" -> SECURITY_TOKEN)

      val timeSeries = getRequest(params)
      Some(timeSeries)

    } catch {
      case _:Throwable => None
    }

  }

  def dayAheadExchangeRequest(inDomain:String, outDomain:String):Option[EntsoeSeries] = {

    try {
      /*
        * Day ahead commercial exchange has a fixed
        * document type = A11, and no process type
        * is used
        */
      val documentType = "A09"

      val (periodStart, periodEnd) = buildPeriod(mode="dayahead")

      val params = Map(
        "documentType"  -> documentType,
        "in_Domain"     -> inDomain,
        "out_Domain"    -> outDomain,
        "PeriodStart"   -> periodStart,
        "PeriodEnd"     -> periodEnd,
        "securityToken" -> SECURITY_TOKEN)

      val timeSeries = getRequest(params)
      Some(timeSeries)

    } catch {
      case _:Throwable => None
    }

  }

  def dayAheadPriceRequest(domain: String): Option[EntsoeSeries] = {

    try {
      /*
       * Day ahead price request has a fixed
       * document type = A44, and no process type
       * is used
       */
      val documentType = "A44"
      /*
       * For day ahead requests, `periodStart` and
       * `periodEnd` must be provided. However, the
       * result is independent of this input and
       * describes the entire UTC (current) day.
       *
       * The interpretation of the result is:
       *
       * At this time interval of today, the forecasted
       * value for the same interval tomorrow is ...
       */
      val (periodStart, periodEnd) = buildPeriod(mode = "dayahead")

      val params = Map(
        "documentType"  -> documentType,
        "in_Domain"     -> domain,
        "out_Domain"    -> domain,
        "PeriodStart"   -> periodStart,
        "PeriodEnd"     -> periodEnd,
        "securityToken" -> SECURITY_TOKEN)

      val timeSeries = getRequest(params)
      Some(timeSeries)

    } catch {
      case _: Throwable => None
    }

  }
  /**
   * This method builds a time period from
   * current timestamp to the begin of the
   * day in UTC time zone.
   */
  private def buildPeriod(mode:String):(String,String) = {
    /*
     * The ENTSOE API uses UTC time zone, i.e., the day of July 5 2022 in CET
     * is during summer time and using UTC this day is considered to start at
     * 2022-07-04 at 22:00 and end at 2022-07-05 at 22:00.
     */
    val now = Instant.now().atZone(ZONE_ID)
    /*
     * The format of the periodStart = YYYYMMDDHH00
     */
    val (periodStart, periodEnd) = mode match {
      case "dayahead"   =>

        val startTime = now.toInstant.toEpochMilli

        val cal = Calendar.getInstance()
        cal.setTimeInMillis(startTime)

        cal.add(Calendar.HOUR, 24)
        val endTime = Instant.ofEpochMilli(cal.getTimeInMillis).atZone(ZONE_ID)

        val start = formatTime(now)
        val end   = formatTime(endTime)

        (start, end)

      case "realized" =>

        val year  = now.getYear
        val month = {
          val value = now.getMonthValue
          if (value < 10) s"0$value" else s"$value"
        }

        val day  = {
          val value = now.getDayOfMonth
          if (value < 10) s"0$value" else s"$value"
        }

        val hour = {
          val value = now.getHour
          if (value < 10) s"0$value" else s"$value"
        }

        val start = s"$year$month${day}0000"
        val end   = s"$year$month$day${hour}00"

        (start, end)

      case "weekahead"  =>
        throw new Exception("No supported yet")
      case "monthahead" =>
        throw new Exception("No supported yet")
      case "yearahead"  =>
        throw new Exception("No supported yet")

      case _ =>
        throw new Exception(s"Load request mode `$mode` is not supported.")

    }

    (periodStart, periodEnd)

  }

  private def formatTime(time:ZonedDateTime):String = {

    val year  = time.getYear
    val month = {
      val value = time.getMonthValue
      if (value < 10) s"0$value" else s"$value"
    }

    val day  = {
      val value = time.getDayOfMonth
      if (value < 10) s"0$value" else s"$value"
    }

    val hour = {
      val value = time.getHour
      if (value < 10) s"0$value" else s"$value"
    }

    s"$year$month$day${hour}00"

  }
}
/*
"""
Parser that uses the ENTSOE API to return the following data types.
Consumption
Production
Exchanges
Exchange Forecast
Day-ahead Price
Generation Forecast
Consumption Forecast
"""
import itertools
import re
from collections import defaultdict
from datetime import datetime, timedelta
from logging import Logger, getLogger
from typing import Any, Dict, List, Optional, Union

import arrow
import numpy as np
import pandas as pd
from bs4 import BeautifulSoup
from requests import Session

from parsers.lib.config import refetch_frequency

from .lib.utils import get_token, sum_production_dicts
from .lib.validation import validate

ENTSOE_PARAMETER_BY_DESC = {v: k for k, v in ENTSOE_PARAMETER_DESC.items()}
ENTSOE_PARAMETER_GROUPS = {
    "production": {
        "biomass": ["B01", "B17"],
        "coal": ["B02", "B05", "B07", "B08"],
        "gas": ["B03", "B04"],
        "geothermal": ["B09"],
        "hydro": ["B11", "B12"],
        "nuclear": ["B14"],
        "oil": ["B06"],
        "solar": ["B16"],
        "wind": ["B18", "B19"],
        "unknown": ["B20", "B13", "B15"],
    },
    "storage": {"hydro storage": ["B10"]},
}
ENTSOE_PARAMETER_BY_GROUP = {
    v: k for k, g in ENTSOE_PARAMETER_GROUPS.items() for v in g
}
# Get all the individual storage parameters in one list
ENTSOE_STORAGE_PARAMETERS = list(
    itertools.chain.from_iterable(ENTSOE_PARAMETER_GROUPS["storage"].values())
)


# Generation per unit can only be obtained at EIC (Control Area) level
ENTSOE_EIC_MAPPING: Dict[str, str] = {
    "DK-DK1": "10Y1001A1001A796",
    "DK-DK2": "10Y1001A1001A796",
    "FI": "10YFI-1--------U",
    "PL": "10YPL-AREA-----S",
    "SE-SE1": "10YSE-1--------K",
    "SE-SE2": "10YSE-1--------K",
    "SE-SE3": "10YSE-1--------K",
    "SE-SE4": "10YSE-1--------K",
    # TODO: ADD DE
}

# Define zone_keys to an array of zone_keys for aggregated production data
ZONE_KEY_AGGREGATES: Dict[str, List[str]] = {
    "IT-SO": ["IT-CA", "IT-SO"],
    "SE": ["SE-SE1", "SE-SE2", "SE-SE3", "SE-SE4"],
}

# Some zone_keys are part of bidding zone domains for price data
ENTSOE_PRICE_DOMAIN_OVERRIDE: Dict[str, str] = {
    "AX": ENTSOE_DOMAIN_MAPPINGS["SE-SE3"],
    "DK-BHM": ENTSOE_DOMAIN_MAPPINGS["DK-DK2"],
    "DE": ENTSOE_DOMAIN_MAPPINGS["DE-LU"],
    "IE": ENTSOE_DOMAIN_MAPPINGS["IE(SEM)"],
    "LU": ENTSOE_DOMAIN_MAPPINGS["DE-LU"],
}

ENTSOE_UNITS_TO_ZONE: Dict[str, str] = {
    # DK-DK1
    "Anholt": "DK-DK1",
    "Esbjergvaerket 3": "DK-DK1",
    "Fynsvaerket 7": "DK-DK1",
    "Horns Rev A": "DK-DK1",
    "Horns Rev B": "DK-DK1",
    "Nordjyllandsvaerket 3": "DK-DK1",
    "Silkeborgvaerket": "DK-DK1",
    "Skaerbaekvaerket 3": "DK-DK1",
    "Studstrupvaerket 3": "DK-DK1",
    "Studstrupvaerket 4": "DK-DK1",
    # DK-DK2
    "Amagervaerket 3": "DK-DK2",
    "Asnaesvaerket 2": "DK-DK2",
    "Asnaesvaerket 5": "DK-DK2",
    "Avedoerevaerket 1": "DK-DK2",
    "Avedoerevaerket 2": "DK-DK2",
    "Kyndbyvaerket 21": "DK-DK2",
    "Kyndbyvaerket 22": "DK-DK2",
    "Roedsand 1": "DK-DK2",
    "Roedsand 2": "DK-DK2",
    # FI
    "Alholmens B2": "FI",
    "Haapavesi B1": "FI",
    "Kaukaan Voima G10": "FI",
    "Keljonlahti B1": "FI",
    "Loviisa 1 G11": "FI",
    "Loviisa 1 G12": "FI",
    "Loviisa 2 G21": "FI",
    "Loviisa 2 G22": "FI",
    "Olkiluoto 1 B1": "FI",
    "Olkiluoto 2 B2": "FI",
    "Toppila B2": "FI",
    # SE-SE1
    "Bastusel G1": "SE-SE1",
    "Gallejaur G1": "SE-SE1",
    "Gallejaur G2": "SE-SE1",
    "Harsprånget G1": "SE-SE1",
    "Harsprånget G2": "SE-SE1",
    "Harsprånget G4": "SE-SE1",
    "Harsprånget G5": "SE-SE1",
    "Letsi G1": "SE-SE1",
    "Letsi G2": "SE-SE1",
    "Letsi G3": "SE-SE1",
    "Ligga G3": "SE-SE1",
    "Messaure G1": "SE-SE1",
    "Messaure G2": "SE-SE1",
    "Messaure G3": "SE-SE1",
    "Porjus G11": "SE-SE1",
    "Porjus G12": "SE-SE1",
    "Porsi G3": "SE-SE1",
    "Ritsem G1": "SE-SE1",
    "Seitevare G1": "SE-SE1",
    "Vietas G1": "SE-SE1",
    "Vietas G2": "SE-SE1",
    # SE-SE2
    "Stalon G1": "SE-SE2",
    "Stornorrfors G1": "SE-SE2",
    "Stornorrfors G2": "SE-SE2",
    "Stornorrfors G3": "SE-SE2",
    "Stornorrfors G4": "SE-SE2",
    # SE-SE3
    "Forsmark block 1 G11": "SE-SE3",
    "Forsmark block 1 G12": "SE-SE3",
    "Forsmark block 2 G21": "SE-SE3",
    "Forsmark block 2 G22": "SE-SE3",
    "Forsmark block 3 G31": "SE-SE3",
    "KVV Västerås G3": "SE-SE3",
    "KVV1 Värtaverket": "SE-SE3",
    "KVV6 Värtaverket": "SE-SE3",
    "KVV8 Värtaverket": "SE-SE3",
    "Oskarshamn G3": "SE-SE3",
    "Oskarshamn G1Ö+G1V": "SE-SE3",
    "Ringhals block 1 G11": "SE-SE3",
    "Ringhals block 1 G12": "SE-SE3",
    "Ringhals block 2 G21": "SE-SE3",
    "Ringhals block 2 G22": "SE-SE3",
    "Ringhals block 3 G31": "SE-SE3",
    "Ringhals block 3 G32": "SE-SE3",
    "Ringhals block 4 G41": "SE-SE3",
    "Ringhals block 4 G42": "SE-SE3",
    "Rya KVV": "SE-SE3",
    "Stenungsund B3": "SE-SE3",
    "Stenungsund B4": "SE-SE3",
    "Trängslet G1": "SE-SE3",
    "Trängslet G2": "SE-SE3",
    "Trängslet G3": "SE-SE3",
    "Uppsala KVV": "SE-SE3",
    "Åbyverket Örebro": "SE-SE3",
    # SE-SE4
    "Gasturbiner Halmstad G12": "SE-SE4",
    "Karlshamn G1": "SE-SE4",
    "Karlshamn G2": "SE-SE4",
    "Karlshamn G3": "SE-SE4",
}

VALIDATIONS: Dict[str, Dict[str, Any]] = {
    # This is a list of criteria to ensure validity of data,
    # used in validate_production()
    # Note that "required" means data is present in ENTSOE.
    # It will still work if data is present but 0.
    # "expected_range" and "floor" only count production and storage
    # - not exchanges!
    "AT": {
        "required": ["hydro"],
    },
    "BE": {
        "required": ["gas", "nuclear"],
        "expected_range": (3000, 25000),
    },
    "BG": {
        "required": ["coal", "nuclear", "hydro"],
        "expected_range": (2000, 20000),
    },
    "CH": {
        "required": ["hydro", "nuclear"],
        "expected_range": (2000, 25000),
    },
    "CZ": {
        # usual load is in 7-12 GW range
        "required": ["coal", "nuclear"],
        "expected_range": (3000, 25000),
    },
    "DE": {
        # Germany sometimes has problems with categories of generation missing from ENTSOE.
        # Normally there is constant production of a few GW from hydro and biomass
        # and when those are missing this can indicate that others are missing as well.
        # We have also never seen unknown being 0.
        # Usual load is in 30 to 80 GW range.
        "required": [
            "coal",
            "gas",
            "nuclear",
            "wind",
            "biomass",
            "hydro",
            "unknown",
            "solar",
        ],
        "expected_range": (20000, 100000),
    },
    "EE": {
        "required": ["coal"],
    },
    "ES": {
        "required": ["coal", "nuclear"],
        "expected_range": (10000, 80000),
    },
    "FI": {
        "required": ["coal", "nuclear", "hydro", "biomass"],
        "expected_range": (2000, 20000),
    },
    "GB": {
        # usual load is in 15 to 50 GW range
        "required": ["coal", "gas", "nuclear"],
        "expected_range": (10000, 80000),
    },
    "GR": {
        "required": ["coal", "gas"],
        "expected_range": (2000, 20000),
    },
    "HU": {
        "required": ["coal", "nuclear"],
    },
    "IE": {
        "required": ["coal"],
        "expected_range": (1000, 15000),
    },
    "IT": {
        "required": ["coal"],
        "expected_range": (5000, 50000),
    },
    "PL": {
        # usual load is in 10-20 GW range and coal is always present
        "required": ["coal"],
        "expected_range": (5000, 35000),
    },
    "PT": {
        "required": ["coal", "gas"],
        "expected_range": (1000, 20000),
    },
    "RO": {
        "required": ["coal", "nuclear", "hydro"],
        "expected_range": (2000, 25000),
    },
    "RS": {
        "required": ["coal"],
    },
    "SE-SE1": {
        "required": ["hydro", "wind", "unknown", "solar"],
    },
    "SE-SE2": {
        "required": ["gas", "hydro", "wind", "unknown", "solar"],
    },
    "SE-SE3": {
        "required": ["gas", "hydro", "nuclear", "wind", "unknown", "solar"],
    },
    "SE-SE4": {
        "required": ["gas", "hydro", "wind", "unknown", "solar"],
    },
    "SI": {
        # own total generation capacity is around 4 GW
        "required": ["nuclear"],
        "expected_range": (800, 5000),
    },
    "SK": {"required": ["nuclear"]},
}

 */