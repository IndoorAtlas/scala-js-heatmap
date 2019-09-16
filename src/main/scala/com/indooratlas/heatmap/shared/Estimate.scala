package com.indooratlas.heatmap.shared

case class Estimate(
  lat: Double,
  lon: Double,
  floor: Int,
  sdkSetupId: String
){
  def colorIndex: Int = sdkSetupId.getBytes().sum % 16 + 1
}

object Estimate {
  def getMean(es: Seq[Estimate]): Estimate = {
    val lat = es.map{_.lat}.sum/es.size
    val lon = es.map{_.lon}.sum/es.size
    Estimate(lat, lon, -999, "n/a")
  }
}
