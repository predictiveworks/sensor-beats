package de.kp.works.beats.sensor.loriot

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

import ch.qos.logback.classic.Logger
import de.kp.works.beats.sensor.socket.{SocketConnect, SocketHandler}
import de.kp.works.beats.sensor.{BeatConf, BeatSource}
/**
 * The LORIOT consumer is built to consume LORIOT
 * uplink messages from the LORIOT web socket server.
 */
abstract class Consumer[T <: BeatConf](options:Options[T]) extends SocketConnect with BeatSource {

  private val RESTART_WAIT_MS = 250
  private val MAX_RETRIES = 5

  private var retries = 1

  protected def getLogger:Logger
  /**
   * Public method to subscribe to the Web Socket
   * server specified (like LORIOT) and publish
   * incoming events in the RocksDB
   */
  def subscribeAndPublish():Unit = {
    /*
     * Set connection security context
     */
    setContext(options.getContext)
    /*
     * Build socket handler
     */
    val serverUrl = options.getServerUrl
    val socketHandler = new SocketHandler() {
      /*
       * This method is invoked after the connection
       * to the web socket server is closed, regularly
       * or with a failure
       */
      override def handleClose(status: Boolean): Unit = {

        if (status) {
          /*
           * The connection to the web socket server
           * closed regularly; as we do not want to
           * stop listening to sensor reading, we try
           * to re-connect
           */
          getLogger.warn(s"The connection to the web socket server `$serverUrl` was closed.")
          Thread.sleep(RESTART_WAIT_MS)

          subscribeAndPublish()

        } else {
          /*
           * The connection to the web socket server
           * closed with a failure; as we do not want
           * to stop listening to sensor reading, we
           * try to re-connect
           */
          getLogger.warn(s"The connection to the web socket server `$serverUrl` was closed with a failure.")
          Thread.sleep(RESTART_WAIT_MS)

          subscribeAndPublish()

        }

      }
      /*
       * This method is invoked after the connection
       * to the web socket server was established either
       * regularly or failed
       */
      override def handleConnect(status: Boolean): Unit = {

        if (status) {
          getLogger.warn(s"The connection to the web socket server `$serverUrl` is successfully established.")

        } else {
          /*
           * Connecting to the web socket server
           * failed; as we do not want to stop listening
           * to sensor reading, we try to re-connect
           */
          getLogger.warn(s"Connecting to the web socket server `$serverUrl` failed.")
          if (retries < MAX_RETRIES) {

            getLogger.info(s"Retry to Connect to the web socket server `$serverUrl`.")
            Thread.sleep(RESTART_WAIT_MS)

            retries += 1
            subscribeAndPublish()

          }

        }

      }
      /*
       * This method is invoked after a text message was
       * received from the web socket server
       */
      override def handleMessage(message: String): Unit = {
        try {
          val loriotMessage = mapper.readValue(message, classOf[LoriotUplink])
          publish(loriotMessage)

        } catch {
          case t:Throwable =>
            getLogger.error(s"Extracting message from LORIOT server failed: ${t.getLocalizedMessage}")
        }
      }

    }

    getLogger.info(s"Connecting to the web socket server `$serverUrl`.")
    connect(serverUrl, socketHandler)

  }

  def publish(message:LoriotUplink)

}
