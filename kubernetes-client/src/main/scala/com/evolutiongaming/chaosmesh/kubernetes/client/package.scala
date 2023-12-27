package com.evolutiongaming.chaosmesh.kubernetes

import cats.syntax.all._
import com.evolutiongaming.chaosmesh.model.k8s.{Api, CustomResource, ExperimentKind}
import com.evolutiongaming.chaosmesh.model.status.Status
import com.goyeau.kubernetes.client.crd.{CustomResource => K8sClientCustomResource}
import io.k8s.apimachinery.pkg.apis.meta.v1.ObjectMeta

package object client {

  implicit class CustomResourceOps[Spec](val cr: CustomResource[Spec, ExperimentKind]) {

    def asK8sClientResource(labels: Map[String, String] = Map.empty): K8sClientCustomResource[Spec, Status] =
      K8sClientCustomResource(
        apiVersion = s"${Api.Group}/${Api.Version}",
        kind = cr.kind.value,
        spec = cr.spec,
        metadata = ObjectMeta(
          name = cr.metadata.name.some,
          namespace = cr.metadata.namespace,
          labels = if (labels.isEmpty) None else Some(labels),
        ).some,
        status = None,
      )

  }
}
