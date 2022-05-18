package com.evolutiongaming.chaosmesh.model.k8s

import cats.ApplicativeThrow
import cats.syntax.all._
import scala.util.control.NoStackTrace

sealed trait ExperimentKind

object ExperimentKind {
  case object PodChaos     extends ExperimentKind
  case object NetworkChaos extends ExperimentKind
  case object StressChaos  extends ExperimentKind
  case object IoChaos      extends ExperimentKind
  case object DnsChaos     extends ExperimentKind
  case object TimeChaos    extends ExperimentKind
  case object JvmChaos     extends ExperimentKind
  case object KernelChaos  extends ExperimentKind
  case object AwsChaos     extends ExperimentKind
  case object GcpChaos     extends ExperimentKind
  case object HttpChaos    extends ExperimentKind

  def from[F[_]: ApplicativeThrow](name: String): F[ExperimentKind] = name.toLowerCase match {
    case "podchaos"     => PodChaos.pure.widen
    case "networkchaos" => NetworkChaos.pure.widen
    case "stresschaos"  => StressChaos.pure.widen
    case "iochaos"      => IoChaos.pure.widen
    case "dnschaos"     => DnsChaos.pure.widen
    case "timechaos"    => TimeChaos.pure.widen
    case "jvmchaos"     => JvmChaos.pure.widen
    case "kernelchaos"  => KernelChaos.pure.widen
    case "awschaos"     => AwsChaos.pure.widen
    case "gcpchaos"     => GcpChaos.pure.widen
    case "httpchaos"    => HttpChaos.pure.widen
    case other          => UnknownExperimentType(s"Experiment type $other is unknown").raiseError
  }

  final case class UnknownExperimentType(msg: String) extends RuntimeException(msg) with NoStackTrace

}
