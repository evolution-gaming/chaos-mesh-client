package com.evolutiongaming.chaosmesh.model.networkchaos

import cats.syntax.all._
import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.spec.Attributes._
import com.evolutiongaming.chaosmesh.model.spec._
import cats.data.NonEmptyList

final case class NetChaos(
  metadata: ResourceMetadata,
  spec:     NetChaos.Spec,
  kind:     ExperimentKind.NetworkChaos.type = ExperimentKind.NetworkChaos,
) extends CustomResource[NetChaos.Spec, ExperimentKind.NetworkChaos.type]

object NetChaos {

  /**
    * Simulate a network fault scenario for a cluster
    * 
    * @param action - Indicates the specific fault type.
    * See [[com.evolutiongaming.chaosmesh.model.spec.Action.NetChaos]] subtypes
    * @param mode - Specifies the mode of the experiment
    * @param direction - Indicates the direction.
    * Default value emulates direction from all selector targets to any other target
    * @param selector - Specifies the target Pod
    * @param containerNames - Specifies the name of the container into which the fault is injected
    * @param device - Specifies the target network interface
    */
  final case class Spec(
    action:         Action.NetChaos,
    mode:           Mode,
    direction:      Option[Direction] = Direction.To(target = None, externalTargets = None).some,
    selector:       Selectors[Selectors.Filled],
    containerNames: Option[NonEmptyList[String]] = None,
    device:         Option[String] = None,
  ) extends HasAction[Action.NetChaos]
      with HasMode
      with HasDirection[Option]
      with HasTargetContainers[Option]
      with HasSelectors
      with HasTargetNetworkDevice[Option] {

    /**
      * Indicates the direction
      *
      */
    def withDirection(direction: Direction) =
      copy(direction = direction.some)

    /**
      * Specifies the name of the container into which the fault is injected
      *
      */
    def withTargetContainNames(names: String*) =
      copy(containerNames = NonEmptyList.fromFoldable(names))

    /**
      * Specifies the target network interface
      *
      */
    def withTargetDevice(name: String) =
      copy(device = name.some)

  }

}
