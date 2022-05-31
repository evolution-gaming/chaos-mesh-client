package com.evolutiongaming.chaosmesh.model.iochaos

import cats.data.NonEmptyList
import cats.syntax.all._
import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.spec.Attributes._
import com.evolutiongaming.chaosmesh.model.spec._

import scala.concurrent.duration.FiniteDuration

final case class IoChaos(
  metadata: ResourceMetadata,
  spec:     IoChaos.Spec,
  kind:     ExperimentKind.IoChaos.type = ExperimentKind.IoChaos,
) extends CustomResource[IoChaos.Spec, ExperimentKind.IoChaos.type]

object IoChaos {

  /**
    *  Simulate a scenario of file system fault
    * 
    * @param action - Indicates the specific fault type.
    * See [[com.evolutiongaming.chaosmesh.model.spec.Action.IoChaos]] subtypes
    * @param mode - Specifies the mode of the experiment
    * @param selector - Specifies the target Pod
    * @param volumePath - The mount point of volume in the target container.
    * Must be the root directory of the mount.
    * @param path - The valid range of fault injections,
    * either a wildcard or a single file.
    * @param methods - Type of the file system call that requires injecting fault
    * https://chaos-mesh.org/docs/simulate-io-chaos-on-kubernetes/#appendix-a-methods-type
    * @param percent - Probability of failure per operation, in % 0..100
    * @param containerName - Specifies the name of the container into which the fault is injected
    * @param duration - Specifies the duration of the experiment
    */
  final case class Spec(
    action:        Action.IoChaos,
    mode:          Mode,
    selector:      Selectors[Selectors.Filled],
    volumePath:    String,
    path:          Option[String] = None,
    methods:       Option[NonEmptyList[String]] = None,
    percent:       Int = 100,
    containerName: Option[String] = None,
    duration:      FiniteDuration,
  ) extends HasAction[Action.IoChaos]
      with HasMode
      with HasSelectors
      with HasDuration {

    /**
      * The valid range of fault injections, either a wildcard or a single file
      *
      */
    def withPath(path: String) =
      copy(path = path.some)

    /**
      * Type of the file system call that requires injecting fault
      * https://chaos-mesh.org/docs/simulate-io-chaos-on-kubernetes/#appendix-a-methods-type
      * 
      */
    def withMethods(first: String, rest: String*) =
      copy(methods = NonEmptyList.of(first, rest: _*).some)

    /**
      * Probability of failure per operation, in % 0..100
      *
      */
    def withProbability(probability: Int) =
      copy(percent = probability)

    /**
      * Specifies the name of the container into which the fault is injected
      *
      */
    def withTargetContainer(name: String) =
      copy(containerName = name.some)
  }
}
