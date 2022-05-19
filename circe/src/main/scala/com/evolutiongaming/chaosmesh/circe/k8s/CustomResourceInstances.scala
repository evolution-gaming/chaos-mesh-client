package com.evolutiongaming.chaosmesh.circe.k8s

import com.evolutiongaming.chaosmesh.model.k8s._
import io.circe._

trait CustomResourceInstances extends ExperimentKindInstances with ResourceMetadataInstances {

  implicit def customResourceEnc[Spec: Encoder]: Encoder[CustomResource[Spec, ExperimentKind]] =
    Encoder.forProduct4("apiVersion", "kind", "spec", "metadata") { res =>
      (s"${Api.Group}/${Api.Version}", res.kind, res.spec, res.metadata)
    }
}
