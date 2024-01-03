package com.evolutiongaming.chaosmesh.model.podchaos

import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.spec.Attributes._
import com.evolutiongaming.chaosmesh.model.spec._

import scala.concurrent.duration.Duration

final case class PodChaos(
  metadata: ResourceMetadata,
  spec:     PodChaos.Spec,
  kind:     ExperimentKind.PodChaos.type = ExperimentKind.PodChaos,
) extends CustomResource[PodChaos.Spec, ExperimentKind.PodChaos.type]

object PodChaos {

  /**
    * Simulate fault scenarios of the specified Pods or containers
    * 
    * @param action - Indicates the specific fault type
    * See [[com.evolutiongaming.chaosmesh.model.spec.Action.PodChaos]] subtypes
    * @param mode - Specifies the mode of the experiment
    * @param selector - Specifies the target Pod
    * @param duration - Specifies the duration of the experiment, can be infinite
    */
  final case class Spec(
    action:   Action.PodChaos,
    mode:     Mode,
    selector: Selectors[Selectors.Filled],
    duration: Duration = Duration.Inf,
  ) extends HasAction[Action.PodChaos]
      with HasMode
      with HasSelectors
      with HasDuration
}
