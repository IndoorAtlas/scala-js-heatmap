package com.indooratlas.heatmap.js

import com.indooratlas.heatmap.shared.{DataApiClient, Estimate, QueryConfig}

import scala.scalajs.js

object MapBoxGeoJson {
  /**
   * Provides the results to MapBox visualization. Arguments:
   * String - GeoJSON string of estimate data
   * js.Object - Mean coordinates of the estimate (to show correct place)
   */
  type DataReadyCallback = js.Function2[String, js.Object, Unit]

  // "Diameter at breast height" - basically determines the size of the points
  val DBH = 3

  private def estimateToGeoJson(e: Estimate): ujson.Obj ={
    val geometry = "geometry" -> ujson.Obj("type" -> "Point", "coordinates" -> ujson.Arr(e.lon, e.lat))
    val myType = "type" -> "Feature"
    val properties = "properties" -> ujson.Obj("dbh" -> DBH, "colorIndex" -> e.colorIndex)
    ujson.Obj(
      myType, properties, geometry
    )
  }

  def fromEstimates(estimates: Seq[Estimate]): ujson.Value ={
    val json = ujson.Obj(
      "type" -> "FeatureCollection",
      "features" -> estimates.map(e => estimateToGeoJson(e))
    )
    json
  }

  def fetchGeoJson(
    config: QueryConfig,
    mapBoxCallback: DataReadyCallback,
    statusCallBack: DataApiClient.Callbacks.StatusUpdate): Unit = {

    // Redirects the results to MapBox visualization
    val callBack: DataApiClient.Callbacks.DataReady = (es: Seq[Estimate]) => {
      val geoJsonString = MapBoxGeoJson.fromEstimates(es).toString()
      println(s"fetchGeoJson callback: estimate count = ${es.size} ")
      val mean = Estimate.getCenter(es)
      val jsMean = js.Dynamic.literal("center" -> js.Array(mean.lon, mean.lat))
      println("Data ready")
      mapBoxCallback(geoJsonString, jsMean)
    }

    DataApiClient.fetchEstimates(config, callBack, statusCallBack)
  }
}
