package com.indooratlas.heatmap.shared

case class QueryConfig(
  dataApiKey: String,
  dates: Seq[QueryConfig.Date],
  sessionFilter: Session => Boolean = (_: Session) => true,
  estimateFilter: Estimate => Boolean = (_: Estimate) => true,
  maxSessions: Int = 1000
)

object QueryConfig{
  case class Date(year: Int, month: Int, day: Int)

  def parse(apiKey: String, days: String, floors: String): Option[QueryConfig] ={
    try {
      require(apiKey.length == 36)
      val splitDays = days.split(",")
      val dates = splitDays.map{ d =>
        val splits = d.split("/")
        require(splits.size == 3)
        Date(splits(0).toInt, splits(1).toInt, splits(2).toInt)
      }
      val floorNumbers = floors.split(",").map{_.toInt}
      require(floorNumbers.nonEmpty)
      val estimateFilter = (e: Estimate) => floorNumbers.contains(e.floor)
      Some(QueryConfig(apiKey, dates, estimateFilter = estimateFilter))
    } catch {
      case _: Exception => None
    }
  }

  object DemoConfigs{
    // TODO: change this
    val smarthouse = QueryConfig(
      "e362bf05-3e12-4e38-86e9-3ded71511fb2",
      Seq(Date(2019, 9, 19))
    )

  }
}
