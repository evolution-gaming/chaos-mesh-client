package com.evolutiongaming.chaosmesh.model.k8s

import com.evolutiongaming.chaosmesh.model.k8s.ExperimentKind

abstract class CustomResource[Spec, +Kind <: ExperimentKind] {
  val kind:     Kind
  val spec:     Spec
  val metadata: ResourceMetadata
}
