package de.kp.works.beats.sensor.milesight

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

object MsBeat extends MsLogging {

  val programName: String = "MsBeat"
  val programDesc: String = "Digital twin of a Milesight EM-300 sensor."

  private val line = s"------------------------------------------------"

  def main(args:Array[String]):Unit = {

    try {

      info(line)

      val service = new MsService()
      service.start()

      info(s"$programName service started.")
      info(line)

    } catch {
      case t: Throwable =>

        error(s"$programName cannot be started: " + t.getMessage)
        /*
         * Sleep for 10 seconds so that one may see error messages
         * in Yarn clusters where logs are not stored.
         */
        Thread.sleep(10000)
        sys.exit(1)

    }

  }

}

