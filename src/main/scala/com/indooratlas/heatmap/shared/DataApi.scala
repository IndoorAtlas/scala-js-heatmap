package com.indooratlas.heatmap.shared

import com.indooratlas.heatmap.js.DataUtils

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object DataApi {
  private val urlPrefix = "https://data-api.indooratlas.com/public/v1/sdk-sessions"
  type UrlLoader = String => Future[String]
  // session count, estimate count, flag if final count (else increment)
  type StatusCallBack = (Int, Int, Boolean) => Unit

  def fetchEstimates(
    queryConfig: QueryConfig,
    dataReadyCallback: Seq[Estimate] => Unit,
    statusCallback: StatusCallBack,
    urlLoader: UrlLoader = DataUtils.loadUrlJS
  ): Unit = {
    println(s"MaxSessions: ${queryConfig.maxSessions}")
    // Resetting the status
    statusCallback(0, 0, true)

    val daySessions = queryConfig.dates.map { date =>
      fetchSessionMetadata(date.year, date.month, date.day, queryConfig.dataApiKey, urlLoader)
    }

    val sessionsFuture: Future[Seq[Session]] = Future.sequence(daySessions).map {
      _.flatten.take(queryConfig.maxSessions)
    }

    sessionsFuture.onComplete {
      case Success(sessions) => println("Total session count: " + sessions.size)
      case Failure(_) => println("Fetching sessions failed..."); ()
    }

    val sessionsWithEstimates: Future[Future[Seq[Try[Session]]]]  = sessionsFuture.map { sessions =>
      val sessionFutures = sessions.map { s: Session =>
        for {
          es <- fetchSession(s.sdkSetupId, queryConfig.dataApiKey, statusCallback, urlLoader)
          filteredEs = es.filter(queryConfig.estimateFilter)
          if filteredEs.nonEmpty
          sessionWithEstimates = s.copy(estimates = filteredEs)
        } yield {
          sessionWithEstimates
        }
      }
      Future.sequence(sessionFutures.map{_.transform(Success(_))})
    }

    // Remove outer future and gather successes
    val sessions: Future[Seq[Session]] = for{
      resultFut <- sessionsWithEstimates
      resultSessions <- resultFut
      successes = resultSessions.collect{case Success(x) => x}
    } yield {
      successes
    }

    sessions.onComplete{
      case Success(sessions) => {
        statusCallback(sessions.size, sessions.map{_.estimates.size}.sum, true)
        dataReadyCallback(sessions.filter(queryConfig.sessionFilter).flatMap(_.estimates))
      }
      case Failure(_) => println("Fetching estimates failed...")
    }
  }

  def fetchSessionMetadata(
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
        session
      }
    }
  }

  def fetchSession(
    sdkSetupId: String,
    apiKey: String,
    statusCallBack: StatusCallBack,
    urlLoader: UrlLoader
  ): Future[Seq[Estimate]] ={
    val url = s"$urlPrefix/$sdkSetupId/events?key=$apiKey"
    val htmlStr = urlLoader(url)

    val es = htmlStr.map{ srt =>
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

    es.onComplete{
      case Success(es: Seq[Estimate]) => {
        statusCallBack(1, es.size, false)
        println(s"Session estimate count ${es.size}")
      }
      case Failure(_) => ()
    }

    es
  }

}
