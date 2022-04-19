package de.kp.works.beats.sensor.fiware

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

/*
 * https://fiware-orion.readthedocs.io/en/master/user/walkthrough_apiv2/index.html#context-management
 */
class Producer(options:Options) {
  /**
   * The address of the Fiware Context Broker
   */
  private val brokerUrl = options.getBrokerUrl
  /**
   * The broker endpoint to create & update
   * entities (sensors)
   */
  private val entityCreateUrl = "/v2/entities"
  private val entityUpdateUrl = "/v2/entities/{id}/attrs"
  /**
   * A public method to create a certain sensor
   * entity. The expected HTTP response code of
   * this POST request = 201 Created.
   */
  def createSensor():Unit = {

  }
  /**
   * A public method to update the attributes of
   * a certain sensor entity. The expected HTTP
   * response code of this PATCH request = 204
   * No Content.
   */
  def updateSensor():Unit = {

  }
}
