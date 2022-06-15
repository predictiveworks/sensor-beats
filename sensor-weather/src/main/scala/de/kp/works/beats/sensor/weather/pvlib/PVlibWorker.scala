package de.kp.works.beats.sensor.weather.pvlib

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
import com.google.gson.JsonObject
import com.typesafe.config.Config
import de.kp.works.beats.sensor.weather.WeLogging
import PVlibMethods.PVlibMethod
import akka.actor.ActorRef

import scala.collection.mutable
import scala.sys.process._
import scala.util.Try

trait PVlibHandler {

  private var jid:Option[String] = None
  private var method:Option[PVlibMethod] = None

  private var actor:Option[ActorRef] = None

  def beforeTask(): Unit = {

    if (jid.nonEmpty && method.nonEmpty)
      PVlibMonitor.beforeTask(jid.get, method.get, actor)

  }

  def onComplete(output:Seq[String]):Unit = {
    /*
     * Inform the supervisor that the
     * Python `pvlib` task is finished
     */
    if (jid.isEmpty)
      throw new Exception(s"A Python `pvlib` task with an unknown jid hase finished.")

    PVlibMonitor.afterTask(jid.get, actor)
    /*
     * Continue with the post processing
     * of the Python `pvlib` result
     */
    doComplete(output)
  }

  def setActor(actor:ActorRef):Unit =
    this.actor = Some(actor)

  /**
   * Getter & setter to manage the Python
   * `pvlib` unique job identifier
   */
  def getJid:Option[String] = {
    this.jid
  }

  def setJid(jid:String):Unit = {
    this.jid = Some(jid)
  }

  def getMethod:Option[PVlibMethod] = {
    this.method
  }

  def setMethod(method:PVlibMethod):Unit = {
    this.method = Some(method)
  }

  def doComplete(output:Seq[String]):Unit
}

abstract class PVlibWorker extends WeLogging {

  protected val pythonCfg: Config = config.getPythonCfg
  private val pythonVersion = pythonCfg.getInt("version")

  private val pythonPackageVersionRegex = "^Python ([0-9]*)\\.([0-9]*)\\.([0-9]*)".r

  private val versionCheck = checkPythonVersion(pythonVersion)

  if (versionCheck.isDefined) {
    val (major, _, _) = versionCheck.get
    if (major != pythonVersion)
      throw new Exception(s"Installed Python version `$pythonVersion` is different from configured one.")
  }
  else
    throw new Exception(s"Python version `$pythonVersion` is not available.")

  /**
   * The computation engine used to access
   * `pvlib`
   */
  protected var engine:String
  /**
   * The Python program file system path
   */
  protected var program:String

  private def checkPythonVersion(pythonVersion: Int): Option[(Int, Int, Int)] =
    Try {
      s"python$pythonVersion --version"
        .lineStream
        .collectFirst {
          case pythonPackageVersionRegex(major, minor, patch) => (major.toInt, minor.toInt, patch.toInt)
        }
     }.getOrElse(None)

  protected def execute(method:PVlibMethod, params:JsonObject, handler:PVlibHandler):Unit = {
    /*
     * Build & register Python job
     */
    val jid = s"urn:works:pvlib:${java.util.UUID.randomUUID.toString}"

    handler.setJid(jid)
    handler.setMethod(method)

    handler.beforeTask()
    /*
     * Enrich provided parameters with control
     * information
     */
    params.addProperty("engine", engine)
    params.addProperty("method", s"${method.toString}")
    /*
     * Build the python command to retrieve solar
     * positions from the provided parameters
     */
    val command = s"$program -c ${params.toString}"
    run(command, handler)

  }

   protected def run(command:String, handler:PVlibHandler):Unit = {

    val cmdline = s"python$pythonVersion $command"
    try {

      val thread = new Thread {

        override def run() {

          val output = mutable.ArrayBuffer.empty[String]
          cmdline
            .lineStream
            .foreach(s => output += s)

          handler.onComplete(output)
          this.finalize()

        }

      }

      thread.start()

    } catch {
      case t:Throwable => /* Do nothing */
        t.printStackTrace()
    }

  }

}
