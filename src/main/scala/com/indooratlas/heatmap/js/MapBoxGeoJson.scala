package com.indooratlas.heatmap.js

import com.indooratlas.heatmap.shared.{Callbacks, DataApiClient, Estimate, QueryConfig}

import scala.scalajs.js

object MapBoxGeoJson {
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
    mapBoxCallback: js.Function2[String, js.Object, Unit],
    statusCallBack: Callbacks.StatusUpdate): Unit = {

    def callBack(es: Seq[Estimate]): Unit = {
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
