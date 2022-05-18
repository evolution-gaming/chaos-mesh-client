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
