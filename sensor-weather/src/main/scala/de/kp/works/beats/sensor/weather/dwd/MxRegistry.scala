package de.kp.works.beats.sensor.weather.dwd

import de.kp.works.beats.sensor.weather.h3.H3

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

object MxRegistry {

  private val H3_RESOLUTION = 7
  /**
   * Retrieve the list of all active MOSMIX stations
   */
  private val mxStations = MxStations.getStations
  /**
   * Retrieve the matching MOSMIX stations by providing
   * a certain geospatial coordinate. The method is based
   * on Uber's H3 geospatial indexing system
   */
  def getByLatLon(lat:Double, lon:Double):Option[MxStation] = {
    getByLatLon(lat, lon, H3_RESOLUTION)
  }

  def getByLatLon(lat:Double, lon:Double, resolution:Int):Option[MxStation] = {
    /*
     * STEP #1: Try to retrieve a matching MOSMIX
     * station that is within the H3 hexagon
     */
    val h3index = H3.instance
      .geoToH3(lat.toDouble, lon.toDouble, resolution)

    val matching = mxStations
      .filter(s => s.h3index == h3index)
    /*
     * STEP #2: In cases where no MOSMIX station
     * is found, the nearest station is retrieved
     */
    val station =
      if (matching.isEmpty) {
        mxStations
          .map(s => {
            val d = distance(lat, lon, s.lat, s.lon)
            (d, s)
          }).minBy { case (d, _) => d }._2

      }
      else
        matching.head

    Some(station)

  }
  /**
   * Helper method to determine the distance
   * of two geospatial points in Km
   */
  private def distance(lat1:Double, lon1:Double, lat2:Double, lon2:Double):Double = {

    val theta = lon1 - lon2
    var dist =
      Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) +
      Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta))

    dist = Math.acos(dist)
    dist = rad2deg(dist)

    dist = dist * 60 * 1.1515
    /* Kilometres */
    dist = dist * 1.609344

    dist

  }

  private def deg2rad(deg:Double):Double = {
    deg * Math.PI / 180.0
  }

  private def rad2deg(rad:Double):Double = {
    rad * 180.0 / Math.PI
  }
}
