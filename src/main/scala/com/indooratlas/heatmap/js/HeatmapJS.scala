package com.indooratlas.heatmap.js

import org.scalajs.dom.html
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Heatmap")
object HeatmapJS {
  @JSExport
  def initializeForm(div: html.Div, mapBoxCallback: MapBoxGeoJson.DataReadyCallback): Unit ={
    println("Initializing form")
    val layout = Layout(mapBoxCallback)
    layout.appendAll(div)
  }
}
