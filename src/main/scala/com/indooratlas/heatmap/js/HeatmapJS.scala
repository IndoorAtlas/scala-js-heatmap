package com.indooratlas.heatmap.js

import org.scalajs.dom.html
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Heatmap")
object HeatmapJS {
  @JSExport
  def initializeForm(div: html.Div, mapBoxCallback: js.Function2[String, js.Object, Unit]): Unit ={
    println("Initializing form")
    val layout = Layout(mapBoxCallback)
    layout.appendAll(div)
  }
}
