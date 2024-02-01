package com.evolutiongaming.chaosmesh.model.status

final case class Status(
  conditions: List[Condition],
  instances:  Map[String, InstanceData],
  experiment: ExperimentStatus,
)
