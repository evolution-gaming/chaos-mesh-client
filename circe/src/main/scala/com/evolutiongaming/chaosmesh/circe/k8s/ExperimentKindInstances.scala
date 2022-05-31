package com.evolutiongaming.chaosmesh.circe.k8s

import com.evolutiongaming.chaosmesh.model.k8s.ExperimentKind
import io.circe._

import scala.util.Try

trait ExperimentKindInstances {

  implicit val experimentKindEnc: Encoder[ExperimentKind] =
    Encoder.encodeString.contramap(_.value)

  implicit val experimentKindDec: Decoder[ExperimentKind] =
    Decoder.decodeString.emapTry(ExperimentKind.from[Try](_))

}
