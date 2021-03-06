package de.kp.works.beats.sensor.weather

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

/**
 * [WeBeat] is an Akka-based micro service to continually
 * retrieve weather data from a certain weather station,
 * identified by its geospatial coordinates.
 */
object WeBeat extends WeLogging {

  val programName: String = "WeBeat"
  val programDesc: String = "Digital twin of an OpenWeather station."

  private val line = s"------------------------------------------------"

  def main(args:Array[String]):Unit = {

    try {

      info(line)

      val service = new WeService(config)
      service.start()

      info(s"$programName service started.")
      info(line)

    } catch {
      case t: Throwable =>

        error(s"$programName cannot be started: " + t.getMessage)

        Thread.sleep(10000)
        sys.exit(1)

    }

  }

}

