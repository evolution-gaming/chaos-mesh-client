package com.evolutiongaming.chaosmesh.model.gcpchaos

import cats.syntax.all._
import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.spec.Attributes._
import com.evolutiongaming.chaosmesh.model.spec._

import scala.concurrent.duration.Duration

final case class GcpChaos(
  metadata: ResourceMetadata,
  spec:     GcpChaos.Spec,
  kind:     ExperimentKind.GcpChaos.type = ExperimentKind.GcpChaos,
) extends CustomResource[GcpChaos.Spec, ExperimentKind.GcpChaos.type]

object GcpChaos {

  /**
    * Simulate fault scenarios of the specified GCP instance
    *
    * @param action - Indicates the specific fault type.
    * See [[com.evolutiongaming.chaosmesh.model.spec.Action.GcpChaos]] subtypes
    * @param mode - Specifies the mode of the experiment
    * @param duration - Duration of experiment. Can be infinite
    * @param secretName - Indicates the name of the Kubernetes secret that stores the GCP authentication information
    * @param project - Indicates the ID of GCP project
    * @param zone - Indicates the region of GCP instance
    * @param instance - Indicates the name of GCP instance
    */
  final case class Spec(
    action:     Action.GcpChaos,
    mode:       Mode,
    duration:   Duration = Duration.Inf,
    secretName: Option[String] = None,
    project:    Option[String] = None,
    zone:       Option[String] = None,
    instance:   Option[String] = None,
  ) extends HasAction[Action.GcpChaos]
      with HasMode
      with HasDuration
      with HasSecretName[Option] {

    /**
      * Indicates the name of the Kubernetes secret that stores the GCP authentication information
      *
      */
    def withSecretName(name: String) =
      copy(secretName = name.some)

    /**
      * Indicates the ID of GCP project
      *
      */
    def withProjectId(id: String) =
      copy(project = id.some)

    /**
      * Indicates the region of GCP instance
      *
      */
    def withRegion(region: String) =
      copy(zone = region.some)

    /**
      * Indicates the name of GCP instance
      * 
      */
    def withInstanceName(name: String) =
      copy(instance = name.some)

  }
}
