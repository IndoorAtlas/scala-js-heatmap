package com.indooratlas.heatmap.shared

import com.indooratlas.heatmap.js.DataUtils

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object DataApi {
  private val urlPrefix = "https://data-api.indooratlas.com/public/v1/sdk-sessions"
  type UrlLoader = String => Future[String]

  def fetchCoordinates(
    queryConfig: QueryConfig,
    callBack: Seq[Estimate] => Unit,
    urlLoader: UrlLoader = DataUtils.loadUrlJS
  ): Unit = {

    val daySessions = queryConfig.dates.map { date =>
      fetchSessions(date.year, date.month, date.day, queryConfig.dataApiKey, urlLoader)
    }

    val sessionsFuture: Future[Seq[Session]] = Future.sequence(daySessions).map {
      _.flatten
    }

    sessionsFuture.onComplete {
      case Success(sessions) => println("Total session count: " + sessions.size)
      case Failure(_) => println("Fetching sessions failed..."); ()
    }

    val sessionsWithEstimates: Future[Future[Seq[Session]]] = sessionsFuture.map { sessions =>
      val sessionFutures = sessions.map { s: Session =>
        val estimatesFuture = fetchEstimates(s.sdkSetupId, queryConfig.dataApiKey, urlLoader)
        for {
          es <- estimatesFuture
        } yield {
          s.copy(estimates = es)
        }
      }
      Future.sequence(sessionFutures)
    }

    sessionsWithEstimates.onComplete {
      case Success(innerFuture) => {
        innerFuture.onComplete {
          case Success(sessions) => {
            val finalEstimates = sessions.filter(queryConfig.sessionFilter)
              .flatMap(_.estimates).filter(queryConfig.estimateFilter)
            callBack(finalEstimates)
          }
          case Failure(_) => println("Fetching estimates failed...")
        }
      }
      case Failure(_) => println("Fetching estimates failed...")
    }
  }

  def fetchSessions(
    year: Int,
    month: Int,
    day: Int,
    apiKey: String,
    urlLoader: UrlLoader
  ): Future[Seq[Session]] = {
    val url = s"$urlPrefix/$year/$month/$day?key=$apiKey"
    urlLoader(url).map { jsonStr =>
      val sessionData = ujson.read(jsonStr).arr
      sessionData.map { d =>
        val session = Session(d("sdkSetupId").str, d("apikeyId").str, d("bundleId").str, d("idaUuid").str, Seq())
        println("Loading session: " + session.apiKeyId)
        session
      }
    }
  }

  def fetchEstimates(
    sdkSetupId: String,
    apiKey: String,
    urlLoader: UrlLoader
  ): Future[Seq[Estimate]] ={
    val url = s"$urlPrefix/$sdkSetupId/events?key=$apiKey"
    val htmlStr = urlLoader(url)

    htmlStr.map{ srt =>
      val data = ujson.read(srt)
      for{
        d <- data.arr
        content = d("content").obj
        location <- content.get("location")
        coordinates = location("coordinates")
        lat = coordinates("lat").num
        lon = coordinates("lon").num
        floor = location("floorNumber").num.toInt
      } yield {
        Estimate(lat, lon, floor, sdkSetupId)
      }
    }
  }

}
