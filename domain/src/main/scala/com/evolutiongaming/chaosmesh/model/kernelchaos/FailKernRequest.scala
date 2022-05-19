package com.evolutiongaming.chaosmesh.model.kernelchaos

import cats.ApplicativeThrow
import cats.data.NonEmptyList
import cats.syntax.all._

import scala.util.control.NoStackTrace

/**
  * Specifies the fault mode (such as kmallo and bio).
  * It also specifies a specific call chain path and the optional filtering conditions
  *
  * @param failtype - Specifies the fault type
  * @param callchain - Specifies a specific call chain
  * @param headers - Specifies the kernel header file you need
  * @param probability - Specifies the probability of faults
  * @param times - Specifies the maximum number of times a fault is triggered
  */
final case class FailKernRequest(
  failtype:    FailKernRequest.FailType,
  callchain:   Option[NonEmptyList[FailKernRequest.Callchain]],
  headers:     Option[NonEmptyList[String]],
  probability: Int,
  times:       Int,
)

object FailKernRequest {

  sealed trait FailType

  object FailType {
    object Failslab      extends FailType
    object FailAllocPage extends FailType
    object FailBio       extends FailType

    def from[F[_]: ApplicativeThrow](value: Int): F[FailType] = value match {
      case 0     => Failslab.pure.widen
      case 1     => FailAllocPage.pure.widen
      case 2     => FailBio.pure.widen
      case other => UnknownFailKernType(s"cannot convert $other into FailType").raiseError
    }

  }

  final case class UnknownFailKernType(msg: String) extends RuntimeException(msg) with NoStackTrace

  /**
    * see https://chaos-mesh.org/docs/simulate-kernel-chaos-on-kubernetes
    */
  final case class Callchain(
    funcname:   String,
    parameters: Option[String],
    predicate:  Option[String],
  )
}
