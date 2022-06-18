package de.kp.works.beats.sensor.scaling

import com.google.gson.{JsonArray, JsonParser}

object CO2 {
  /*
   * Indoor levels of carbon dioxide can have a negative impact on cognitive performance
   * as well as human health. Average indoor CO2 concentrations range from 600-1000 ppm,
   * but can exceed 2000 ppm with increased occupancies and poor ventilation.
   *
   * Exposure to >1000 ppm leads to decreased cognitive abilities, while levels >2000 ppm
   * have been linked to inflammation, kidney calcification, bone de-mineralization,
   * oxidative stress, and endothelial dysfunction.
   */
  val INDOOR: String =
    """
      |[
      | {
      | "rating": "excellent",
      | "index": 1,
      | "lower": 0,
      | "upper": 400,
      | "description": "The air inside is as fresh as the air outside."
      | },
      | {
      | "rating": "fine",
      | "index": 2,
      | "lower": 400,
      | "upper": 1000,
      | "description": "The air quality inside remains at harmless levels."
      | },
      | {
      | "rating": "moderate",
      | "index": 3,
      | "lower": 1000,
      | "upper": 1500,
      | "description": "The air quality inside has reached conspicuous levels."
      | },
      | {
      | "rating": "poor",
      | "index": 4,
      | "lower": 1500,
      | "upper": 2000,
      | "description": "The air quality inside has reached precarious levels."
      | },
      | {
      | "rating": "very poor",
      | "index": 5,
      | "lower": 2000,
      | "upper": 5000,
      | "description": "The air quality inside has reached unacceptable levels."
      | },
      | {
      | "rating": "severe",
      | "index": 6,
      | "lower": 5000,
      | "upper": 100000,
      | "description": "The air quality inside has exceeded maximum workplace concentration values."
      | }
      |]
      |""".stripMargin

  def getIndoor:JsonArray = {
    JsonParser
      .parseString(INDOOR)
      .getAsJsonArray
  }

}
