package com.indooratlas.heatmap.js

import org.scalajs.dom
import dom.ext.Ajax
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object DataUtils {
  def loadUrlJS(url: String): Future[String] = Ajax.get(url).map(_.responseText)
}
