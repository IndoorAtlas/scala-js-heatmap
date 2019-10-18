package com.indooratlas.heatmap.js

import com.indooratlas.heatmap.shared.{Callbacks, DataApiClient, Estimate, QueryConfig}
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.html.{Button, Input, Paragraph}
import scalatags.JsDom.all._

import scala.scalajs.js

case class Layout(mapBoxCallback: js.Function2[String, js.Object, Unit]) {
  val apiKeyInput: Input = input(`type`:="text", placeholder:="dataApiKey").render
  val daysInput: Input = input(`type`:="text", placeholder:="Day(s): yyyy/mm/dd,yyyy/...").render
  val floorInput: Input = input(`type`:="text", placeholder:="Floor level(s): 1,2,...").render

  val status: Paragraph = p("Waiting for user input...").render
  status.style.color = "white"

  var sessionCount = 0
  var estimateCount = 0
  val statusCallBack: Callbacks.StatusUpdate = (sessions: Int, estimates: Int, isFinalCount: Boolean) => {
    if(isFinalCount){
      sessionCount = sessions
      estimateCount = estimates
      status.textContent = s"Data ready: $sessionCount sessions with $estimateCount estimates"
    } else {
      sessionCount += sessions
      estimateCount += estimates
      status.textContent = s"Downloading session ($sessionCount) data ($estimateCount)..."
    }
  }

  val submitButton: Button = button().render
  submitButton.textContent = "Submit"

  submitButton.onclick = (e: dom.MouseEvent) => {
    println("Button Click! " + apiKeyInput.value)
    QueryConfig.parse(
      apiKeyInput.value, daysInput.value, floorInput.value
    ) match {
      case Some(config) => {
        status.textContent = "Downloading session data..."
        MapBoxGeoJson.fetchGeoJson(config, mapBoxCallback,  statusCallBack)
      }
      case None => dom.window.alert("Malformed input")
    }
  }

  val exampleSubmitButton: Button = button().render
  exampleSubmitButton.textContent = "Example"

  exampleSubmitButton.onclick = (e: dom.MouseEvent) => {
    status.textContent = "Downloading demo session data..."
    MapBoxGeoJson.fetchGeoJson(QueryConfig.DemoConfigs.smarthouse, mapBoxCallback,  statusCallBack)
  }

  def appendAll(div: html.Div): Unit ={
    div.appendChild(apiKeyInput)
    div.appendChild(p().render)
    div.appendChild(daysInput)
    div.appendChild(p().render)
    div.appendChild(floorInput)
    div.appendChild(p().render)
    div.appendChild(submitButton)
    div.appendChild(exampleSubmitButton)
    div.appendChild(status)
  }

}
