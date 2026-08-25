package com.evolutiongaming.chaosmesh.circe.common

import io.circe.Decoder
import io.circe.Encoder

import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration

private[circe] trait OptionalInfDurationInstances extends DurationInstances {

  private[circe] implicit val optDurationEnc: Encoder[Duration] =
    Encoder[Option[FiniteDuration]].contramap {
      case finite: FiniteDuration => Some(finite)
      case _ => None
    }

  private[circe] implicit val optDurationDec: Decoder[Duration] =
    Decoder[Option[FiniteDuration]].map {
      case Some(finite) => finite
      case None => Duration.Inf
    }

}
