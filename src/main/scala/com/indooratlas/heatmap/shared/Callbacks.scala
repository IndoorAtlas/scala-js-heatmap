package com.indooratlas.heatmap.shared

object Callbacks {
  trait StatusUpdate {
    def apply(sessionCount: Int, estimateCount: Int, resetBeforeIncrement: Boolean): Unit
  }

  trait DataReady {
    def apply(estimates: Seq[Estimate]): Unit
  }
}
