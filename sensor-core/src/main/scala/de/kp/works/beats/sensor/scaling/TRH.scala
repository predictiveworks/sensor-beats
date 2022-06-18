package de.kp.works.beats.sensor.scaling

object TRH {
  /*
   * Temperature and relative humidity is, first and foremost, an issue of comfort
   * for the building’s occupants. Aside from this, high temperatures and humidity
   * have been found to increase the concentrations of certain pollutants.
   *
   * In addition, regulating temperature and humidity levels minimize the risk of
   * mould growth indoors, thus preventing illnesses like Sick Building Syndrome.
   */
  val INDOOR:String =
    """
      |[
      | {
      | "rating": "excellent",
      | "index": 1,
      | "values":
      | {
      |  "10": []
      |  "20": [],
      |  "30": [],
      |  "40": [],
      |  "50": [21,22,23],
      |  "60": [20,21,22],
      |  "70": [20,21,22],
      |  "80": [],
      |  "90": []
      | }
      | },
      | {
      | "rating": "fine",
      | "index": 2,
      | "values":
      | {
      |  "10": []
      |  "20": [],
      |  "30": [],
      |  "40": [23,24],
      |  "50": [20,24],
      |  "60": [19,23,24],
      |  "70": [19,23],
      |  "80": [18,19,20,21,22],
      |  "90": []
      | }
      | },
      | {
      | "rating": "moderate",
      | "index": 3,
      | "values":
      | {
      |  "10": []
      |  "20": [],
      |  "30": [],
      |  "40": [20,21,22,25],
      |  "50": [19,25],
      |  "60": [18,25],
      |  "70": [18,24],
      |  "80": [23],
      |  "90": [19,20,21,22]
      | }
      | },
      | {
      | "rating": "poor",
      | "index": 4,
      | "values":
      | {
      |  "10": []
      |  "20": [],
      |  "30": [19,20,21,22,23,24,25,26],
      |  "40": [19,26],
      |  "50": [18,26],
      |  "60": [17,26],
      |  "70": [17,25],
      |  "80": [17,24],
      |  "90": [18,23]
      | }
      | },
      | {
      | "rating": "very poor",
      | "index": 5,
      | "values":
      | {
      |  "10": []
      |  "20": [17,18,19,20,21,22,23,24,25,26,27],
      |  "30": [17,18,27],
      |  "40": [17,18,27],
      |  "50": [16,17,27],
      |  "60": [16,27],
      |  "70": [16,26,27],
      |  "80": [16,25,26,27],
      |  "90": [17,24,25]
      | }
      | },
      | {
      | "rating": "severe",
      | "index": 6,
      | "values":
      | {
      |  "10": [16,27,18,19,20,21,22,23,24,25,25,27,28]
      |  "20": [16,28],
      |  "30": [16,28],
      |  "40": [16,28],
      |  "50": [28],
      |  "60": [28],
      |  "70": [28],
      |  "80": [28],
      |  "90": [16,26,27,28]
      | }
      | }
      |]
      |""".stripMargin
}
