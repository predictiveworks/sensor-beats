package de.kp.works.beats.sensor.weather.api

case class InverterReq()
case class InvertersReq(
  /*
   * The external table name is CEC_INVERTERS
   * and must be replaced by the internal
   * table name before the SQL query is
   * executed.
   */
  sql:String)

case class ModuleReq()
case class ModulesReq(
  /*
   * The external table name is CEC_MODULES
   * and must be replaced by the internal
   * table name before the SQL query is
   * executed.
   */
  sql:String
)