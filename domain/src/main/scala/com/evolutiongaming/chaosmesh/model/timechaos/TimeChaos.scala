package com.evolutiongaming.chaosmesh.model.timechaos

import cats.data.NonEmptyList
import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.spec.Attributes._
import com.evolutiongaming.chaosmesh.model.spec._

import scala.concurrent.duration.FiniteDuration

final case class TimeChaos(
  metadata: ResourceMetadata,
  spec:     TimeChaos.Spec,
  kind:     ExperimentKind.TimeChaos.type = ExperimentKind.TimeChaos,
)

object TimeChaos {

  /**
    * Simulate a time offset scenario.
    * NOTE: TimeChaos only affects the PID 1 process in the PID namespace of the container,
    * and child processes of the PID 1.
    * For example, the process started by kubectl exec does not be affected.
    * 
    * @param mode - Specifies the mode of the experiment
    * @param selector - Specifies the target Pod
    * @param containerNames - Specifies the name of the container into which the fault is injected
    * @param timeOffset - Specifies the length of time offset
    * @param clockIds - Specifies the ID of clock that will be offset.
    * see https://man7.org/linux/man-pages/man2/clock_gettime.2.html
    */
  final case class Spec(
    mode:           Mode,
    selector:       Selectors[Selectors.Filled],
    containerNames: Option[NonEmptyList[String]] = None,
    timeOffset:     FiniteDuration,
    clockIds:       NonEmptyList[String] = NonEmptyList.one("CLOCK_REALTIME"),
  ) extends HasMode
      with HasTargetContainers[Option]
      with HasSelectors {

    /**
      * Specifies the name of the container into which the fault is injected
      *
      */
    def withTargetContainers(names: String*) =
      copy(containerNames = NonEmptyList.fromFoldable(names))

    /**
       * Specifies the ID of clock that will be offset. See https://man7.org/linux/man-pages/man2/clock_gettime.2.html
       *
       */
    def withTargetClockIds(first: String, rest: String*) =
      copy(clockIds = NonEmptyList.of(first, rest: _*))
  }
}
