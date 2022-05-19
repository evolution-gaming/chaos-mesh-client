package com.evolutiongaming.chaosmesh.circe.k8s

import com.evolutiongaming.chaosmesh.model.k8s.ExperimentKind
import io.circe._

import scala.util.Try

trait ExperimentKindInstances {

  implicit val experimentKindEnc: Encoder[ExperimentKind] =
    Encoder.encodeString.contramap {
      case ExperimentKind.PodChaos     => "PodChaos"
      case ExperimentKind.NetworkChaos => "NetworkChaos"
      case ExperimentKind.StressChaos  => "StressChaos"
      case ExperimentKind.IoChaos      => "IoChaos"
      case ExperimentKind.DnsChaos     => "DNSChaos"
      case ExperimentKind.TimeChaos    => "TimeChaos"
      case ExperimentKind.JvmChaos     => "JVMChaos"
      case ExperimentKind.KernelChaos  => "KernelChaos"
      case ExperimentKind.AwsChaos     => "AWSChaos"
      case ExperimentKind.GcpChaos     => "GCPChaos"
      case ExperimentKind.HttpChaos    => "HTTPChaos"
    }

  implicit val experimentKindDec: Decoder[ExperimentKind] =
    Decoder.decodeString.emapTry(ExperimentKind.from[Try](_))

}
