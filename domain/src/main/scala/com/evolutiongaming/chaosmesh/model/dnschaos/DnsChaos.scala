package com.evolutiongaming.chaosmesh.model.dnschaos

import cats.data.NonEmptyList
import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.spec.Attributes._
import com.evolutiongaming.chaosmesh.model.spec._
import scala.concurrent.duration.Duration

final case class DnsChaos(
  metadata: ResourceMetadata,
  spec:     DnsChaos.Spec,
  kind:     ExperimentKind.DnsChaos.type = ExperimentKind.DnsChaos,
) extends CustomResource[DnsChaos.Spec, ExperimentKind.DnsChaos.type]

object DnsChaos {

  /**
    * Simulate wrong DNS responses
    * 
    * @param action - Indicates the specific fault type.
    * See [[com.evolutiongaming.chaosmesh.model.spec.Action.DnsChaos]] subtypes
    * @param mode - Specifies the mode of the experiment
    * @param selector - Specifies the target Pod
    * @param patterns - Selects a domain template that matches faults. Placeholder ? and wildcard * are supported
    * @param duration - Specifies the duration of the experiment, can be infinite
    */
  final case class Spec(
    action:   Action.DnsChaos,
    mode:     Mode,
    selector: Selectors[Selectors.Filled],
    duration: Duration = Duration.Inf,
    patterns: Option[NonEmptyList[String]] = None,
  ) extends HasAction[Action.DnsChaos]
      with HasMode
      with HasSelectors
      with HasDuration {

    /**
      * Selects a domain template that matches faults. Placeholder ? and wildcard * are supported
      * 
      */
    def withTargetDomains(domains: String*): Spec =
      copy(patterns = NonEmptyList.fromList(domains.toList))

  }
}
