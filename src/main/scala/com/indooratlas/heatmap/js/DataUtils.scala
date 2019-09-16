package com.indooratlas.heatmap.js

import org.scalajs.dom

import scala.concurrent.Future

object DataUtils {
  def loadUrlJS(url: String): Future[String] ={
    import dom.ext.Ajax

    import scala.concurrent.ExecutionContext.Implicits.global

    Ajax.get(url).map(_.responseText)
  }

}
