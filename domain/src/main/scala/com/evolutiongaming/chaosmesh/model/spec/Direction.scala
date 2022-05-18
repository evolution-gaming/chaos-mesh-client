package com.evolutiongaming.chaosmesh.model.spec

import cats.data.NonEmptyList

trait Direction

object Direction {
  
  final case class To(
    target: Option[Target],
    externalTargets: Option[NonEmptyList[String]],
  ) extends Direction {

    /**
      * Indicates the network targets except for Kubernetes, which can be IPv4 addresses or domains
      *
      */
    def withExternalTargets(targets: String*) =
      copy(externalTargets = NonEmptyList.fromFoldable(targets))

  }

  final case class  From(
    target: Target
  ) extends Direction

  final case class Both(
    target: Target
  ) extends Direction

  final case class Target(mode: Mode, selector: Selectors[Selectors.Filled])

}
