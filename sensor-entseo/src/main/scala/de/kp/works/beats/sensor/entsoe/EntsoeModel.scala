package de.kp.works.beats.sensor.entsoe

case class EntsoeDot(
  startTs:Long,
  endTs:Long,
  quantity:Int)

case class EntsoeSeries(
  startTs:Long,
  endTs:Long,
  resolution:String,
  unitOfMeasure:String,
  dots:Seq[EntsoeDot]
)
