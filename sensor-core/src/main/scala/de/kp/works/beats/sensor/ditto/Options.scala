package de.kp.works.beats.sensor.ditto

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

import com.neovisionaries.ws.client.WebSocket
import de.kp.works.beats.sensor.BeatConf
import org.eclipse.ditto.base.model.json.JsonSchemaVersion
import org.eclipse.ditto.client.configuration._
import org.eclipse.ditto.client.messaging._
import org.eclipse.ditto.things.model.ThingId

/**
 * The Eclipse Ditto [Options] class provides access
 * parameters to read & write context data from and
 * to Eclipse Ditto
 */
class Options[T <: BeatConf](config:T) {

  private val ENDPOINT             = "endpoint"
  private val NAMESPACE            = "namespace"
  private val OAUTH_CLIENT_ID      = "oauth_client_id"
  private val OAUTH_CLIENT_SECRET  = "oauth_client_secret"
  private val OAUTH_SCOPES         = "oauth_scopes"
  private val OAUTH_TOKEN_ENDPOINT = "oauth_token_endpoint"
  private val PROXY_HOST           = "proxy_host"
  private val PROXY_PORT           = "proxy_port"
  private val THING_CHANGES        = "thing_changes"
  private val THING_IDS            = "thing_ids"
  private val TRUST_FILE           = "trust_file"
  private val TRUST_PASS           = "trust_pass"
  private val USER_NAME            = "user_name"
  private val USER_PASS            = "user_pass"
  /*
   * - endpoint
   * - namespace
   * - oauth_client_id
   * - oauth_client_secret
   * - oauth_scopes
   * - oauth_token_endpoint
   * - proxy_host
   * - proxy_port
   * - thing_changes
   * - thing_ids
   * - trust_file
   * - trust_pass
   * - user_name
   * - user_pass
   */
  private val dittoConfig = config.getDittoCfg

  private def getEndpoint:String =
    dittoConfig.getString(ENDPOINT)

  def getNS:String =
    dittoConfig.getString(NAMESPACE)

  private def getOAuthClientId:String =
    dittoConfig.getString(OAUTH_CLIENT_ID)

  private def getOAuthClientSecret:String =
    dittoConfig.getString(OAUTH_CLIENT_SECRET)

  private def getOAuthScopes:java.util.List[String] = {

    val tokens = dittoConfig.getString(OAUTH_SCOPES).split(",")

    val s = new java.util.ArrayList[String]()
    tokens.foreach(t => s.add(t.trim))

    s

  }

  private def getOAuthTokenEndpoint:String =
    dittoConfig.getString(OAUTH_TOKEN_ENDPOINT)

  private def getProxyHost:Option[String] = {

    if (dittoConfig.hasPath(PROXY_HOST))
      Some(dittoConfig.getString(PROXY_HOST))

    else None

  }

  private def getProxyPort:Option[Int] = {

    if (dittoConfig.hasPath(PROXY_PORT))
      Some(dittoConfig.getInt(PROXY_PORT))

    else None

  }

  def getThingChanges:Boolean =
    dittoConfig.getBoolean(THING_CHANGES)

  def getThingIds:Seq[ThingId] = {

    if (dittoConfig.hasPath(THING_IDS)) {

      dittoConfig.getString(THING_IDS)
        .split(",")
        .map(thingId => {
          ThingId.of(thingId.trim)
        })

    } else Seq.empty[ThingId]

  }

  private def getTrustFile:Option[String] = {

    if (dittoConfig.hasPath(TRUST_FILE))
      Some(dittoConfig.getString(TRUST_FILE))

    else None

  }

  private def getTrustPass:Option[String] = {

    if (dittoConfig.hasPath(TRUST_PASS))
      Some(dittoConfig.getString(TRUST_PASS))

    else None

  }

  private def getUserName:Option[String] = {

    if (dittoConfig.hasPath(USER_NAME))
      Some(dittoConfig.getString(USER_NAME))

    else None

  }

  private def getUserPass:Option[String] = {

    if (dittoConfig.hasPath(USER_PASS))
      Some(dittoConfig.getString(USER_PASS))

    else None

  }

  def getAuthProvider(proxyConf:ProxyConfiguration):AuthenticationProvider[WebSocket] = {

    val username = getUserName
    val userpass = getUserPass

    if (username.nonEmpty && userpass.nonEmpty) {

      /** BASIC AUTHENTICATION **/

      if (username.get.isEmpty || userpass.get.isEmpty) {
        throw new IllegalArgumentException("Basic authentication requires username & password.")
      }

      val basicAuthConf = BasicAuthenticationConfiguration.newBuilder()
        .username(username.get)
        .password(userpass.get)

      if (proxyConf != null) basicAuthConf.proxyConfiguration(proxyConf)

      AuthenticationProviders.basic(basicAuthConf.build)

    }
    else {

      /** AUTH2 AUTHENTICATION **/

      try {

        val oAuthConf = ClientCredentialsAuthenticationConfiguration.newBuilder()
          .clientId(getOAuthClientId)
          .clientSecret(getOAuthClientSecret)
          .scopes(getOAuthScopes)
          .tokenEndpoint(getOAuthTokenEndpoint)

        if (proxyConf != null) oAuthConf.proxyConfiguration(proxyConf)
        AuthenticationProviders.clientCredentials(oAuthConf.build)

      } catch {
        case _: Exception => throw new IllegalArgumentException("Missing parameters for OAuth authentication.")
      }

    }

  }

  def getMessagingProvider: MessagingProvider = {

    val builder = WebSocketMessagingConfiguration.newBuilder()
    /* See Bosch IoT examples */
    builder.jsonSchemaVersion(JsonSchemaVersion.V_2)

    builder.endpoint(getEndpoint)

    val proxyConfiguration: ProxyConfiguration = {

      val host = getProxyHost
      val port = getProxyPort

      if (host.nonEmpty && port.nonEmpty) {

        if (host.get.isEmpty || port.get < 0) null
        else {

          val proxyConf = ProxyConfiguration.newBuilder()
            .proxyHost(host.get)
            .proxyPort(port.get)
            .build

          proxyConf

        }

      } else null

    }

    val trustFile = getTrustFile
    val trustPass = getTrustPass

    if (trustFile.nonEmpty && trustPass.nonEmpty) {

      if (trustFile.get.nonEmpty && trustPass.get.nonEmpty) {

        val trustStoreConf = TrustStoreConfiguration.newBuilder()
          .location(new java.net.URL(trustFile.get))
          .password(trustPass.get)
          .build

        builder.trustStoreConfiguration(trustStoreConf)

      }
    }

    val authProvider = getAuthProvider(proxyConfiguration)
    MessagingProviders.webSocket(builder.build(), authProvider)

  }

}
