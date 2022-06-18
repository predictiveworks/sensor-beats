package de.kp.works.beats.sensor.scaling

import com.google.gson.{JsonArray, JsonParser}

object VOC {
  /*
   * Volatile organic compounds refer to a large group of carbon-containing substances
   * including hydrocarbons, alcohols, aldehydes, and organic acids.
   *
   * They are particularly concentrated indoors due to internal sources from interior
   * products and building materials such as furniture, plastics, carpets, wallpapers,
   * cleaning materials, copy machines, lacquers, solvents, synthetic fragrances, insecticides
   * and third-hand tobacco smoke.
   *
   * The indoor impact of VOCs has greater health implications since people spend time predominantly
   * in buildings. This makes them an important air pollutant to monitor as they have toxic, mutagenic,
   * carcinogenic, genotoxic, and teratogenic effects on humans.
   *
   * Milder symptoms are characterized by headaches, fatigue, loss of productivity, and sleep disorders,
   * which are categorized as “Sick Building Syndrome”. More serious health effects include damage to the
   * liver, kidney, and central nervous system, and cancers like leukemia and lymphoma.
   *
   * Indoor organic pollutants have been categorized as volatile organic compounds (VOCs), very volatile
   * organic compounds (VVOCs), and semi-volatile organic compounds (SVOCs).
   *
   *  The sum of all the VOC concentration types combined becomes the total volatile organic compounds
   * (TVOCs) value.
   */
  val INDOOR: String =
    """
      |[
      | {
      | "rating": "excellent",
      | "index": 1,
      | "lower": 0,
      | "upper": 50
      | },
      | {
      | "rating": "fine",
      | "index": 2,
      | "lower": 51,
      | "upper": 100
      | },
      | {
      | "rating": "moderate",
      | "index": 3,
      | "lower": 101,
      | "upper": 150
      | },
      | {
      | "rating": "poor",
      | "index": 4,
      | "lower": 151,
      | "upper": 200
      | },
      | {
      | "rating": "very poor",
      | "index": 5,
      | "lower": 201,
      | "upper": 300
      | },
      | {
      | "rating": "severe",
      | "index": 6,
      | "lower": 301,
      | "upper": 600
      | }
      |]
      |""".stripMargin

  def getIndoor:JsonArray = {
    JsonParser
      .parseString(INDOOR)
      .getAsJsonArray
  }

}
