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
      copy(containerNames = NonEmptyList.fromList(names.toList))

  }

  /**
    * Specifies the stress of CPU or memory
    *
    * @param memory - memory stress specification
    * @param cpu - cpu stress specification
    */
  final case class Stressors private[chaosmesh] (
    memory: Option[MemoryStressor] = None,
    cpu:    Option[CpuStressor] = None,
  ) {

    /**
      * Specifies CPU stress options
      *
      */
    def withCpuStress(cpu: CpuStressor) =
      copy(cpu = cpu.some)

    /**
      * Specifies memory stress options
      *
      */
    def withMemoryStress(mem: MemoryStressor) =
      copy(memory = mem.some)
  }

  object Stressors {
    def apply(): Stressors =
      Stressors(None, None)
  }

  /**
    * Specifies the memory stress
    * 
    * @param workers - Specifies the number of threads that apply memory stress
    * @param size - Specifies the memory size to be occupied or a percentage of the total memory size.
    * The final sum of the occupied memory size. Like 256MB / 25%
    * @param oomScoreAdj - Specifies oom score value, which defines probability to be killed by OOM.
    * -1000 (very unlikely to be killed by the OOM killer) up to 1000 (very likely to be killed by the OOM killer)
    *
    */
  final case class MemoryStressor(
    workers:     Int,
    size:        Option[String] = None,
    oomScoreAdj: Option[Int] = None,
  ) {

    /**
      * Specifies the memory size to be occupied or a percentage of the total memory size.
      * The final sum of the occupied memory size. Like 256MB / 25%
      *
      */
    def withOccupiedSize(size: String) =
      copy(size = size.some)

    /**
      * Specifies oom score value, which defines probability to be killed by OOM.
      * -1000 (very unlikely to be killed by the OOM killer) 
      * up to 1000 (very likely to be killed by the OOM killer)
      *
      */
    def withOomScoreAdj(score: Int) =
      copy(oomScoreAdj = score.some)
  }

  /**
    * Specifies the CPU stress
    *
    * @param workers - Specifies the number of threads that apply CPU stress	
    * @param load - Specifies the percentage of CPU occupied.
    * @param options - Specifies additional options
    * 0 means that no additional CPU is added, and 100 refers to full load.
    * The final sum of CPU load is workers * load.
    */
  final case class CpuStressor(
    workers: Int,
    load:    Int,
    options: Option[NonEmptyList[String]] = None,
  ) {

    /**
      * Specifies additional options
      *
      */
    def withOptions(first: String, rest: String*) =
      copy(options = NonEmptyList.of(first, rest: _*).some)
  }

}
