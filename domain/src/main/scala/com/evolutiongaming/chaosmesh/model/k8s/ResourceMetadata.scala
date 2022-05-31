package com.evolutiongaming.chaosmesh.model.k8s

final case class ResourceMetadata(
  name:      String,
  namespace: Option[String] = None,
)
