package com.evolutiongaming.chaosmesh.model.status

import java.time.Instant

final case class ContainerRecord(
  id:             String,
  phase:          String,
  selectorKey:    String,
  injectedCount:  Int,
  recoveredCount: Int,
  events:         List[ContainerRecord.Event],
)

object ContainerRecord {
  final case class Event(
    `type`:    String,
    operation: String,
    timestamp: Instant,
  )
}
