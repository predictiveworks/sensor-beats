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

object EntsoeDefs {

  val ENTSOE_ENDPOINT = "https://web-api.tp.entsoe.eu/api"
  /**
   * The parameter description defined below specifies the
   * allowed values for the query parameter `psrType`.
   */
  val ENTSOE_PARAMETER_DESC = Map(
    "B01" -> "Biomass",
    "B02" -> "Fossil Brown coal/Lignite",
    "B03" -> "Fossil Coal-derived gas",
    "B04" -> "Fossil Gas",
    "B05" -> "Fossil Hard coal",
    "B06" -> "Fossil Oil",
    "B07" -> "Fossil Oil shale",
    "B08" -> "Fossil Peat",
    "B09" -> "Geothermal",
    "B10" -> "Hydro Pumped Storage",
    "B11" -> "Hydro Run-of-river and poundage",
    "B12" -> "Hydro Water Reservoir",
    "B13" -> "Marine",
    "B14" -> "Nuclear",
    "B15" -> "Other renewable",
    "B16" -> "Solar",
    "B17" -> "Waste",
    "B18" -> "Wind Offshore",
    "B19" -> "Wind Onshore",
    "B20" -> "Other")

  /**
   * In Europe, wholesale electricity markets are structured
   * in bidding zones, featuring EQUAL PRICES within them.
   *
   * Within each zone, any consumer is allowed to contract
   * power with any generator without limitations and hence
   * disregarding the physical reality of the transmission
   * network.
   *
   * A bidding zone is the largest geographical area within
   * which market participants are able to exchange energy
   * without capacity allocation.
   *
   * The map below assigns a certain bidding zone (or country)
   * to the domain query parameter
   */
  val ENTSOE_DOMAIN_MAPPINGS = Map(
    "AL"        -> "10YAL-KESH-----5",
    "AT"        -> "10YAT-APG------L",
    "AZ"        -> "10Y1001A1001B05V",
    "BA"        -> "10YBA-JPCC-----D",
    "BE"        -> "10YBE----------2",
    "BG"        -> "10YCA-BULGARIA-R",
    "BY"        -> "10Y1001A1001A51S",
    "CH"        -> "10YCH-SWISSGRIDZ",
    "CZ"        -> "10YCZ-CEPS-----N",
    "DE"        -> "10Y1001A1001A83F",
    "DE-LU"     -> "10Y1001A1001A82H",
    "DK"        -> "10Y1001A1001A65H",
    "DK-DK1"    -> "10YDK-1--------W",
    "DK-DK2"    -> "10YDK-2--------M",
    "EE"        -> "10Y1001A1001A39I",
    "ES"        -> "10YES-REE------0",
    "FI"        -> "10YFI-1--------U",
    "FR"        -> "10YFR-RTE------C",
    "GB"        -> "10YGB----------A",
    "GB-NIR"    -> "10Y1001A1001A016",
    "GE"        -> "10Y1001A1001B012",
    "GR"        -> "10YGR-HTSO-----Y",
    "HR"        -> "10YHR-HEP------M",
    "HU"        -> "10YHU-MAVIR----U",
    "IE"        -> "10YIE-1001A00010",
    "IE(SEM)"   -> "10Y1001A1001A59C",
    "IT"        -> "10YIT-GRTN-----B",
    "IT-BR"     -> "10Y1001A1001A699",
    "IT-CA"     -> "10Y1001C--00096J",
    "IT-CNO"    -> "10Y1001A1001A70O",
    "IT-CSO"    -> "10Y1001A1001A71M",
    "IT-FO"     -> "10Y1001A1001A72K",
    "IT-NO"     -> "10Y1001A1001A73I",
    "IT-PR"     -> "10Y1001A1001A76C",
    "IT-SACOAC" -> "10Y1001A1001A885",
    "IT-SACODC" -> "10Y1001A1001A893",
    "IT-SAR"    -> "10Y1001A1001A74G",
    "IT-SIC"    -> "10Y1001A1001A75E",
    "IT-SO"     -> "10Y1001A1001A788",
    "LT"        -> "10YLT-1001A0008Q",
    "LU"        -> "10YLU-CEGEDEL-NQ",
    "LV"        -> "10YLV-1001A00074",
    "ME"        -> "10YCS-CG-TSO---S",
    "MK"        -> "10YMK-MEPSO----8",
    "MT"        -> "10Y1001A1001A93C",
    "NL"        -> "10YNL----------L",
    "NO"        -> "10YNO-0--------C",
    "NO-NO1"    -> "10YNO-1--------2",
    "NO-NO2"    -> "10YNO-2--------T",
    "NO-NO3"    -> "10YNO-3--------J",
    "NO-NO4"    -> "10YNO-4--------9",
    "NO-NO5"    -> "10Y1001A1001A48H",
    "PL"        -> "10YPL-AREA-----S",
    "PT"        -> "10YPT-REN------W",
    "RO"        -> "10YRO-TEL------P",
    "RS"        -> "10YCS-SERBIATSOV",
    "RU"        -> "10Y1001A1001A49F",
    "RU-KGD"    -> "10Y1001A1001A50U",
    "SE"        -> "10YSE-1--------K",
    "SE-SE1"    -> "10Y1001A1001A44P",
    "SE-SE2"    -> "10Y1001A1001A45N",
    "SE-SE3"    -> "10Y1001A1001A46L",
    "SE-SE4"    -> "10Y1001A1001A47J",
    "SI"        -> "10YSI-ELES-----O",
    "SK"        -> "10YSK-SEPS-----K",
    "TR"        -> "10YTR-TEIAS----W",
    "UA"        -> "10YUA-WEPS-----0",
    "XK"        -> "10Y1001C--00100H")

  /**
   * Cross border or bidding zone transmission is specified
   * by providing an in (import) domain and an out (export)
   * domain.
   *
   * Some of these exchanges need specific overrides
   */
  val ENTSOE_EXCHANGE_DOMAIN_OVERRIDE = Map(
    "AT->IT-NO"         -> List(ENTSOE_DOMAIN_MAPPINGS("AT"), ENTSOE_DOMAIN_MAPPINGS("IT")),
    "BY->UA"            -> List(ENTSOE_DOMAIN_MAPPINGS("BY"), "10Y1001C--00003F"),
    "DE->DK-DK1"        -> List(ENTSOE_DOMAIN_MAPPINGS("DE-LU"), ENTSOE_DOMAIN_MAPPINGS("DK-DK1")),
    "DE->DK-DK2"        -> List(ENTSOE_DOMAIN_MAPPINGS("DE-LU"), ENTSOE_DOMAIN_MAPPINGS("DK-DK2")),
    "DE->NO-NO2"        -> List(ENTSOE_DOMAIN_MAPPINGS("DE-LU"), ENTSOE_DOMAIN_MAPPINGS("NO-NO2")),
    "DE->SE-SE4"        -> List(ENTSOE_DOMAIN_MAPPINGS("DE-LU"), ENTSOE_DOMAIN_MAPPINGS("SE-SE4")),
    "FR-COR->IT-CNO"    -> List(ENTSOE_DOMAIN_MAPPINGS("IT-SACODC"),ENTSOE_DOMAIN_MAPPINGS("IT-CNO")),
    "GE->RU-1"          -> List(ENTSOE_DOMAIN_MAPPINGS("GE"), ENTSOE_DOMAIN_MAPPINGS("RU")),
    "GR->IT-SO"         -> List(ENTSOE_DOMAIN_MAPPINGS("GR"), ENTSOE_DOMAIN_MAPPINGS("IT-SO")),
    "IT-CSO->ME"        -> List(ENTSOE_DOMAIN_MAPPINGS("IT"), ENTSOE_DOMAIN_MAPPINGS("ME")),
    "PL->UA"            -> List(ENTSOE_DOMAIN_MAPPINGS("PL"), "10Y1001A1001A869"),
    "IT-SIC->IT-SO"     -> List(ENTSOE_DOMAIN_MAPPINGS("IT-SIC"), ENTSOE_DOMAIN_MAPPINGS("IT-CA")),
    "FR-COR-AC->IT-SAR" -> List(ENTSOE_DOMAIN_MAPPINGS("IT-SACOAC"), ENTSOE_DOMAIN_MAPPINGS("IT-SAR")),
    "FR-COR-DC->IT-SAR" -> List(ENTSOE_DOMAIN_MAPPINGS("IT-SACODC"), ENTSOE_DOMAIN_MAPPINGS("IT-SAR")))
}
