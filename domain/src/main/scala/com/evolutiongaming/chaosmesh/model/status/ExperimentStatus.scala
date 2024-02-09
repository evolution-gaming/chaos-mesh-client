package com.evolutiongaming.chaosmesh.model.status

final case class ExperimentStatus(
  containerRecords: List[ContainerRecord],
  desiredPhase:     Option[String],
)
