package com.evolutiongaming.chaosmesh.model.kernelchaos
import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.spec.Attributes._
import com.evolutiongaming.chaosmesh.model.spec._

final case class KernelChaos(
  metadata: ResourceMetadata,
  spec:     KernelChaos.Spec,
  kind:     ExperimentKind.KernelChaos.type = ExperimentKind.KernelChaos,
) extends CustomResource[KernelChaos.Spec, ExperimentKind.KernelChaos.type]

object KernelChaos {

  /**
    * Injects I/O-based or memory-based faults into the specified kernel paths using BPF
    * see https://lore.kernel.org/lkml/20171213180356.hsuhzoa7s4ngro2r@destiny/T/
    *
    * @param mode - Specifies the mode of the experiment
    * @param selector - Specifies the target Pod
    * @param failKernRequest - specifies the fault mode (such as kmallo and bio).
    * It also specifies a specific call chain path and the optional filtering conditions
    */
  final case class Spec(
    mode:            Mode,
    selector:        Selectors[Selectors.Filled],
    failKernRequest: FailKernRequest,
  ) extends HasMode
      with HasSelectors
}
