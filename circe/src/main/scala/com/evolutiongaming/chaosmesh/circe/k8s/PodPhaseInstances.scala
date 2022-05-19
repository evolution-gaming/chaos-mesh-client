package com.evolutiongaming.chaosmesh.circe.k8s

import com.evolutiongaming.chaosmesh.model.k8s.PodPhase
import com.evolutiongaming.chaosmesh.model.k8s.PodPhase._
import io.circe.{Decoder, Encoder}

import scala.util.Try

trait PodPhaseInstances {

  implicit val podPhaseEnc: Encoder[PodPhase] =
    Encoder.encodeString.contramap {
      case Pending  => "Pending"
      case Running  => "Running"
      case Succeeds => "Succeeds"
      case Failed   => "Failed"
      case Unknown  => "Unknown"
    }

  implicit val podPhaseDec: Decoder[PodPhase] =
    Decoder.decodeString.emapTry(PodPhase.from[Try](_))

}
