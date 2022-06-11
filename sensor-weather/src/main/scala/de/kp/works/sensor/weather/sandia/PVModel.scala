package de.kp.works.sensor.weather.sandia


case class PVModule(
  /*
   * Unique identifier of the solar module
   */
  id:String,
  /*
   * The name of the solar module as defined
   * by Sandia
   */
  name:String,
  /*
   * The name of the inverter that is used as
   * defined by Sandia
   */
  inverter:String,
  /*
   * The azimuth of the solar module in degrees
   * (West=270, South=180, East=90)
   */
  azimuth:Int,
  /*
   * The inclination angle of the solar module in degrees
   * (0 degrees would be horizontal).
   */
  elevation:Int,
  /*
   * The number of panels per string in the solar module
   */
  numPanels:Int)

case class PVModel(
  /*
   * The latitude of the PV system
   */
  latitude:Double,
  /*
   * The longitude of the PV system
   */
  longitude:Double,
  /*
   * The altitude in metre of the PV system location
   */
  altitude:Double,
  /*
   * The modules that form the PV system
   */
  modules:Seq[PVModule],
  /*
   * The measure of the diffuse reflection of solar
   * radiation out of the total solar radiation and
   * measured on a scale from 0 to 1
   */
  albedo:Double,
  /*
   * The temperature model (see pvlib documentation)
   * that is the closest match. Samples:
   *
   * Roof mounted systems = `open_rack`
   */
  tempModel:String)
