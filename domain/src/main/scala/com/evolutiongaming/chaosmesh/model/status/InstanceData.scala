package com.evolutiongaming.chaosmesh.model.status

import java.time.Instant

sealed trait InstanceData

object InstanceData {
  final case class IntValue(value: Int)                           extends InstanceData
  final case class StartTimeData(startTime: Instant, uid: String) extends InstanceData
}
