package com.evolutiongaming.chaosmesh.model.spec

import cats.data.NonEmptyList
import com.evolutiongaming.chaosmesh.model.spec._

import scala.concurrent.duration.FiniteDuration

object Attributes {

  trait HasAction[A <: Action] {
    def action: A
  }

  trait HasDirection[F[_]] {
    def direction: F[Direction]
  }

  trait HasDuration {
    def duration: FiniteDuration
  }

  trait HasMode {
    def mode: Mode
  }

  trait HasSelectors {
    def selector: Selectors[Selectors.Filled]
  }

  trait HasSecretName[F[_]] {
    def secretName: F[String]
  }

  trait HasTargetContainers[F[_]] {
    def containerNames: F[NonEmptyList[String]]
  }

  trait HasTargetNetworkDevice[F[_]] {
    def device: F[String]
  }

}
