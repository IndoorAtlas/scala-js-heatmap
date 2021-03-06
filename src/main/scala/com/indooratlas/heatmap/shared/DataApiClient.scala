package com.indooratlas.heatmap.shared

import com.indooratlas.heatmap.js.DataUtils

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object DataApiClient {
  private val urlPrefix = "https://data-api.indooratlas.com/public/v1/sdk-sessions"
  type UrlLoader = String => Future[String]

  object Callbacks {
    trait StatusUpdate {
      def apply(sessionCount: Int, estimateCount: Int, resetBeforeIncrement: Boolean): Unit
    }

    trait DataReady {
      def apply(estimates: Seq[Estimate]): Unit
    }
  }

  def fetchEstimates(
    queryConfig: QueryConfig,
    dataReadyCallback: Callbacks.DataReady,
    statusCallback: Callbacks.StatusUpdate,
    urlLoader: UrlLoader = DataUtils.loadUrlJS
  ): Unit = {
    // Resetting the status
    statusCallback(sessionCount = 0, estimateCount = 0, resetBeforeIncrement = true)

    val sessionsForDates = queryConfig.dates.map { date =>
      fetchSessionMetadata(date.year, date.month, date.day, queryConfig.dataApiKey, urlLoader)
    }

    // Flatten and force limit for maximum number of sessions
    val sessionsFuture: Future[Seq[Session]] = Future.sequence(sessionsForDates).map {
      _.flatten.take(queryConfig.maxSessions)
    }

    sessionsFuture.onComplete {
      case Success(sessions) => {
        println("Session metadata ready. Total session count: " + sessions.size)
        println("Fetching session data...")
      }
      case Failure(_) => println("Fetching session metadata failed..."); ()
    }

    // Add (filtered) estimate data to sessions
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
        println("All session data ready.")
        val finalSessions = sessions.filter(queryConfig.sessionFilter)
        val finalEstimates = finalSessions.flatMap(_.estimates)
        statusCallback(finalSessions.size, finalEstimates.size, resetBeforeIncrement = true)
        dataReadyCallback(finalEstimates)
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
    println("Fetching session metadata...")
    val url = s"$urlPrefix/$year/$month/$day?key=$apiKey"
    urlLoader(url).map { jsonStr =>
      val sessionData = ujson.read(jsonStr).arr
      sessionData.map { d =>
        val session = Session(d("sdkSetupId").str, d("bundleId").str, d("idaUuid").str, Seq())
        session
      }
    }
  }

  def fetchSession(
    sdkSetupId: String,
    apiKey: String,
    statusCallBack: Callbacks.StatusUpdate,
    urlLoader: UrlLoader
  ): Future[Seq[Estimate]] ={
    val url = s"$urlPrefix/$sdkSetupId/events?key=$apiKey"
    val htmlStr = urlLoader(url)

    val estimates = htmlStr.map{ srt =>
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

    estimates.onComplete{
      case Success(es: Seq[Estimate]) => {
        statusCallBack(sessionCount = 1, estimateCount = es.size, resetBeforeIncrement = false)
        println(s"Session ready. Estimate count: ${es.size}, sdkSetupId: $sdkSetupId")
      }
      case Failure(_) => ()
    }

    estimates
  }

}
