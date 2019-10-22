package com.indooratlas.heatmap.shared

case class Estimate(
  lat: Double,
  lon: Double,
  floor: Int,
  sdkSetupId: String
){
  def colorIndex: Int = sdkSetupId.getBytes().sum % 16 + 1

  override def toString: String = {
    s"Estimate: [$lat, $lon, $floor, $sdkSetupId]"
  }
}

object Estimate {
  def getCenter(es: Seq[Estimate]): Estimate = {
    if(es.nonEmpty){
      val lats = es.map{_.lat}.sorted
      val lons = es.map{_.lon}.sorted
      val centerLat = lats(lats.size/2)
      val centerLon = lons(lons.size/2)
      Estimate(centerLat, centerLon, -999, "Median of estimates")
    } else {
      println("Empty session data: returning coordinates from Oulu.")
      Estimate(65.0121, 25.4651, -999, "Median of estimates")
    }

  }
}
