package com.evolutiongaming.chaosmesh.circe.common

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.{DAYS, HOURS, MICROSECONDS, MILLISECONDS, MINUTES, NANOSECONDS, SECONDS}

import cats.syntax.all._
import cats.{ApplicativeThrow, MonadThrow}
import io.circe._

import scala.annotation.tailrec
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Try
import scala.util.control.NoStackTrace

import DurationInstances._
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
      if (str.startsWith("-")) parseStringAsDuration[Try](str.drop(1)).map(-_)
      else parseStringAsDuration[Try](str)
    }
}

private object DurationInstances {

  private val timeRegex = "\\d+(?:d|h|ms|m|s|ns|µs)".r

  final def splitDurations(
    s: String,
  ): List[String] = {
    @tailrec
    def aux(s: String, acc: List[String]): List[String] =
      if (s.isEmpty()) acc
      else
        timeRegex.findFirstIn(s) match {
          case None        => List.empty
          case Some(found) => aux(s.drop(found.size), found :: acc)
        }
    aux(s, List.empty)
  }

  private def convert[F[_]: MonadThrow](list: List[String]): F[List[FiniteDuration]] =
    list.traverse { str =>
      ApplicativeThrow[F].catchNonFatal(Duration(str)).flatMap {
        case finite: FiniteDuration => finite.pure
        case _ => DurationParsingError(s"Unknown duration format: $str").raiseError
      }
    }

  private def parseStringAsDuration[F[_]: MonadThrow](str: String): F[FiniteDuration] =
    convert(splitDurations(str)).flatMap { list =>
      list match {
        case Nil =>
          DurationParsingError(s"Unknown duration format: $str").raiseError
        case head :: tail =>
          tail.fold(head)(_ + _).pure
      }
    }

  final case class DurationParsingError(msg: String) extends RuntimeException(msg) with NoStackTrace

}
