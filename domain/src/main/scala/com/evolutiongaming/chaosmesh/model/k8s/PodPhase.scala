package com.evolutiongaming.chaosmesh.model.k8s

import cats.ApplicativeThrow
import cats.syntax.all._
import scala.util.control.NoStackTrace
/**
  * see https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#pod-phase
  */
sealed trait PodPhase

object PodPhase {

  case object Pending  extends PodPhase
  case object Running  extends PodPhase
  case object Succeeds extends PodPhase
  case object Failed   extends PodPhase
  case object Unknown  extends PodPhase

  def from[F[_]: ApplicativeThrow](str: String) = str.toLowerCase match {
    case "pending"  => Pending.pure.widen
    case "running"  => Running.pure.widen
    case "succeeds" => Succeeds.pure.widen
    case "failed"   => Failed.pure.widen
    case "unknown"  => Unknown.pure.widen
    case other      => PodPhaseTypeIsNotDefined(s"Pod phase $other doesn't have corresponding type").raiseError
  }

  final case class PodPhaseTypeIsNotDefined(msg: String) extends RuntimeException(msg) with NoStackTrace
}
