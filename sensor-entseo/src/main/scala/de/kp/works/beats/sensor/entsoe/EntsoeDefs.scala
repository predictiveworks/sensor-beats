package de.kp.works.beats.sensor.entsoe

object EntsoeDefs {

  val ENTSOE_ENDPOINT = "https://web-api.tp.entsoe.eu/api"

  val ENTSOE_PARAMETER_DESC = Map(
    "B01" -> "Biomass",
    "B02" -> "Fossil Brown coal/Lignite",
    "B03" -> "Fossil Coal-derived gas",
    "B04" -> "Fossil Gas",
    "B05" -> "Fossil Hard coal",
    "B06" -> "Fossil Oil",
    "B07" -> "Fossil Oil shale",
    "B08" -> "Fossil Peat",
    "B09" -> "Geothermal",
    "B10" -> "Hydro Pumped Storage",
    "B11" -> "Hydro Run-of-river and poundage",
    "B12" -> "Hydro Water Reservoir",
    "B13" -> "Marine",
    "B14" -> "Nuclear",
    "B15" -> "Other renewable",
    "B16" -> "Solar",
    "B17" -> "Waste",
    "B18" -> "Wind Offshore",
    "B19" -> "Wind Onshore",
    "B20" -> "Other")

}
