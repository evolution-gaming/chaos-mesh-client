package com.evolutiongaming.chaosmesh.model.k8s

import cats.ApplicativeThrow
import cats.syntax.all._
import scala.util.control.NoStackTrace

abstract class ExperimentKind(val value: String)

object ExperimentKind {
  case object PodChaos     extends ExperimentKind("PodChaos")
  case object NetworkChaos extends ExperimentKind("NetworkChaos")
  case object StressChaos  extends ExperimentKind("StressChaos")
  case object IoChaos      extends ExperimentKind("IOChaos")
  case object DnsChaos     extends ExperimentKind("DNSChaos")
  case object TimeChaos    extends ExperimentKind("TimeChaos")
  case object JvmChaos     extends ExperimentKind("JVMChaos")
  case object KernelChaos  extends ExperimentKind("KernelChaos")
  case object AwsChaos     extends ExperimentKind("AWSChaos")
  case object GcpChaos     extends ExperimentKind("GCPChaos")
  case object HttpChaos    extends ExperimentKind("HTTPChaos")

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

  final case class UnknownExperimentType(msg: String)
      extends RuntimeException(msg)
      with NoStackTrace

}
