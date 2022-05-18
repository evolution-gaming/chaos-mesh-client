package com.evolutiongaming.chaosmesh.model.awschaos

import cats.syntax.all._
import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.spec.Attributes._
import com.evolutiongaming.chaosmesh.model.spec._

import scala.concurrent.duration.FiniteDuration

final case class AwsChaos(
  metadata: ResourceMetadata,
  spec:     AwsChaos.Spec,
  kind:     ExperimentKind.AwsChaos.type = ExperimentKind.AwsChaos,
) extends CustomResource[AwsChaos.Spec, ExperimentKind.AwsChaos.type]

object AwsChaos {

  /**
    * Simulate fault scenarios on the specified AWS instance
    *
    * @param action - Indicates the specific fault type.
    * See [[com.evolutiongaming.chaosmesh.model.spec.Action.AwsChaos]] subtypes
    * @param mode - Specifies the mode of the experiment
    * @param secretName - Specifies the name of the Kubernetes Secret that stores the AWS authentication information
    * @param awsRegion - Specifies the AWS region
    * @param ec2Instance - Specifies the ID of the EC2 instance
    * @param duration - Duration of experiment
    */
  final case class Spec(
    action:      Action.AwsChaos,
    mode:        Mode,
    duration:    FiniteDuration,
    secretName:  Option[String] = None,
    awsRegion:   String,
    ec2Instance: String,
  ) extends HasAction[Action.AwsChaos]
      with HasMode
      with HasDuration
      with HasSecretName[Option] {

    /**
      * - Specifies the name of the Kubernetes Secret that stores the AWS authentication information
      *
      */
    def withSecretName(name: String): Spec =
      copy(secretName = name.some)

  }
}
