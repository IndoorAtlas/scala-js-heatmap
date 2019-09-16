package com.indooratlas.heatmap.shared

case class Session(
  sdkSetupId: String,
  apiKeyId: String,
  bundleId: String,
  idaUuid: String,
  estimates: Seq[Estimate]
)
