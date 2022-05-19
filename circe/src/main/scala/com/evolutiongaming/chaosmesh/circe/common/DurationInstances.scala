package com.evolutiongaming.chaosmesh.circe.common

import io.circe._

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Try
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.NANOSECONDS
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.TimeUnit.HOURS
import java.util.concurrent.TimeUnit.MICROSECONDS

private[circe] trait DurationInstances {

  implicit private[circe] val durationEnc: Encoder[FiniteDuration] =
    Encoder.encodeString.contramap { d =>
      val coarsest = d.toCoarsest
      s"${coarsest.length}${shortString(d.unit)}"
    }

  private def shortString(unit: TimeUnit): String = unit match {
    case MINUTES      => "m"
    case NANOSECONDS  => "ns"
    case MILLISECONDS => "ms"
    case DAYS         => "d"
    case SECONDS      => "s"
    case HOURS        => "h"
    case MICROSECONDS => "µs"
  }

  implicit private[circe] val durationDec: Decoder[FiniteDuration] =
    Decoder.decodeString.emapTry { str =>
      Try(Duration(str)).collect {
        case finite: FiniteDuration => finite
      }
    }

}
