package com.indooratlas.heatmap.shared

case class Session(
  sdkSetupId: String,
  bundleId: String,
  idaUuid: String,
  estimates: Seq[Estimate]
)
