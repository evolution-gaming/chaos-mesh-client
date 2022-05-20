package com.evolutiongaming.chaosmesh.model.spec

import cats.data.NonEmptyList
import cats.syntax.all._

trait Direction

object Direction {

  /**
    * Indicates the direction of experiment from experiment target to specified targets
    * making network chaos effective for outgoing traffic
    *
    * @param target - Specifies targets inside of Kubernetes cluster
    * @param externalTargets - Specifies targets by domain name
    */
  final case class To private(
    target:          Option[Target] = None,
    externalTargets: Option[NonEmptyList[String]] = None,
  ) extends Direction {

    /**
      * Indicates the network targets except for Kubernetes, which can be IPv4 addresses or domains
      *
      */
    def withExternalTargets(targets: String*) =
      copy(externalTargets = NonEmptyList.fromList(targets.toList))

    /**
      * Making network chaos effective for outgoing traffic to specified targets
      *
      */
    def withTarget(target: Target) =
      copy(target = target.some)

  }

  object To {
    def apply(): To = To(None, None)
  }

  /**
    * Indicates the direction of experiment from specified targets to experiment target
    * making network chaos effective for incoming traffic
    *
    * @param target - Specifies targets inside of Kubernetes cluster
    */
  final case class From(
    target: Target,
  ) extends Direction

  /**
    * Indicates the direction of experiment in both ways
    * making network chaos effective for incoming and outgoing traffic
    *
    * @param target - Specifies targets inside of Kubernetes cluster
    */
  final case class Both(
    target: Target,
  ) extends Direction

  final case class Target(mode: Mode, selector: Selectors[Selectors.Filled])

}
