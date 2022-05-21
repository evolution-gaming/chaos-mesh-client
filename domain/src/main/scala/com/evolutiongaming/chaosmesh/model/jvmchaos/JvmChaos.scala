package com.evolutiongaming.chaosmesh.model.jvmchaos

import cats.syntax.all._
import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.spec.Attributes._
import com.evolutiongaming.chaosmesh.model.spec._

final case class JvmChaos(
  metadata: ResourceMetadata,
  spec:     JvmChaos.Spec,
  kind:     ExperimentKind.JvmChaos.type = ExperimentKind.JvmChaos,
) extends CustomResource[JvmChaos.Spec, ExperimentKind.JvmChaos.type]

object JvmChaos {

  /**
    * Simulates the faults of JVM application through Byteman
    * see https://github.com/chaos-mesh/byteman
    * 
    * NOTE:
    * In jvm return experiment definition of experiment
    * on chaos-mesh side expect to contain "value" key field
    * in top level of spec object,
    * however to specify Fixed, FixedPercent or RandomMaxPercent
    * "value" field in top level of spec is also required,
    * so those modes cannot be defined correctly for
    * jvm return experiment
    * 
    * @param action - Indicates the specific fault type
    * See [[com.evolutiongaming.chaosmesh.model.spec.Action.JvmChaos]] subtypes
    * @param mode - Specifies the mode of the experiment
    * @param selector - Specifies the target Pod
    * @param port - The port ID attached to the Java process agent.
    * The faults are injected into the Java process through this ID.
    */
  final case class Spec(
    action:   Action.JvmChaos,
    mode:     Mode,
    selector: Selectors[Selectors.Filled],
    port:     Option[Int] = None,
  ) extends HasAction[Action.JvmChaos]
      with HasMode
      with HasSelectors {

    /**
      * The port ID attached to the Java process agent
      *
      */
    def withAgentPort(port: Int) =
      copy(port = port.some)
  }
}
