package com.evolutiongaming.chaosmesh.kubernetes

import com.evolutiongaming.chaosmesh.model.k8s.CustomResource
import com.goyeau.kubernetes.client.crd.{CustomResource => K8sClientCustomResource}
import com.evolutiongaming.chaosmesh.model.k8s.ExperimentKind
import com.evolutiongaming.chaosmesh.model.k8s.Api
import io.k8s.apimachinery.pkg.apis.meta.v1.ObjectMeta
import cats.syntax.all._

package object client {

  implicit class CustomResourceOps[Spec](val cr: CustomResource[Spec, ExperimentKind]) {

    def asK8sClientResource: K8sClientCustomResource[Spec, Unit] =
      K8sClientCustomResource(
        apiVersion = s"${Api.Group}/${Api.Version}",
        kind = cr.kind.value,
        spec = cr.spec,
        metadata = ObjectMeta(
          name = cr.metadata.name.some,
          namespace = cr.metadata.namespace,
        ).some,
        status = None,
      )

  }
}
