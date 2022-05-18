package com.evolutiongaming.chaosmesh.model.stresschaos

import cats.data.NonEmptyList
import cats.syntax.all._
import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.spec.Attributes._
import com.evolutiongaming.chaosmesh.model.spec._

import scala.concurrent.duration.FiniteDuration

final case class StressChaos(
  metadata: ResourceMetadata,
  spec:     StressChaos.Spec,
  kind:     ExperimentKind.StressChaos.type = ExperimentKind.StressChaos,
) extends CustomResource[StressChaos.Spec, ExperimentKind.StressChaos.type]

object StressChaos {

  /**
    * Simulate stress scenarios inside containers
    *
    * @param mode - Specifies the mode of the experiment
    * @param selector - Specifies the target Pod
    * @param duration - Duration of experiment
    * @param stressors - Specifies the stress of CPU or memory
    * @param stressngStressors - Specifies the stres-ng parameter to reach richer stress injection
    * @param containerNames - Specifies the name of the container into which the fault is injected
    */
  final case class Spec(
    mode:              Mode,
    selector:          Selectors[Selectors.Filled],
    duration:          FiniteDuration,
    stressors:         Stressors,
    stressngStressors: Option[String] = None,
    containerNames:    Option[NonEmptyList[String]] = None,
  ) extends HasMode
      with HasSelectors
      with HasDuration
      with HasTargetContainers[Option] {

    /**
      * Specifies the stres-ng parameter
      *
      */
    def withStressngParameter(param: String) =
      copy(stressngStressors = param.some)

    /**
      * Specifies the name of the container into which the fault is injected
      *
      */
    def withTargetContainers(names: String*) =
      copy(containerNames = NonEmptyList.fromFoldable(names))

  }

  final case class Stressors(memory: Option[MemoryStressor] = None, cpu: Option[CpuStressor] = None)

  /**
    * 
    * @param workers - Specifies the number of threads that apply memory stress
    * @param size - Specifies the memory size to be occupied or a percentage of the total memory size.
    * The final sum of the occupied memory size is size. Like 256MB / 25%
    */
  final case class MemoryStressor(workers: Int, size: Option[String])

  /**
  * 
  *
  * @param workers - Specifies the number of threads that apply CPU stress	
  * @param load - Specifies the percentage of CPU occupied.
  * 0 means that no additional CPU is added, and 100 refers to full load.
  * The final sum of CPU load is workers * load.
  */
  final case class CpuStressor(workers: Int, load: Int)

}
