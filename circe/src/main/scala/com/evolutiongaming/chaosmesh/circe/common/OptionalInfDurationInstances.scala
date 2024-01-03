package com.evolutiongaming.chaosmesh.circe.common

import io.circe.Encoder
import scala.concurrent.duration.Duration
import io.circe.Decoder
import scala.concurrent.duration.FiniteDuration

private[circe] trait OptionalInfDurationInstances extends DurationInstances {

  implicit private[circe] val optDurationEnc: Encoder[Duration] =
    Encoder[Option[FiniteDuration]].contramap {
      case finite: FiniteDuration => Some(finite)
      case _ => None
    }

  implicit private[circe] val optDurationDec: Decoder[Duration] =
    Decoder[Option[FiniteDuration]].map {
      case Some(finite) => finite
      case None         => Duration.Inf
    }

}
