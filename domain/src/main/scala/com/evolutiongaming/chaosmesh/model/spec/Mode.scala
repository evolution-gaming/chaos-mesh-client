package com.evolutiongaming.chaosmesh.model.spec

import cats.ApplicativeThrow
import cats.syntax.all._

import scala.util.control.NoStackTrace

sealed abstract class Mode(
  val mode:  String,
  val value: Option[String],
)

object Mode {

  object One extends Mode("one", None)
  object All extends Mode("all", None)

  final case class Fixed(amount: Int)             extends Mode("fixed", amount.toString().some)
  final case class FixedPercent(percent: Int)     extends Mode("fixed-percent", percent.toString().some)
  final case class RandomMaxPercent(percent: Int) extends Mode("random-max-percent", percent.toString().some)

  def from[F[_]: ApplicativeThrow](name: String, value: Option[Int]): F[Mode] = (name, value) match {
    case ("one", _) => One.pure.widen
    case ("all", _) => All.pure.widen

    case ("fixed", Some(value))              => Fixed(value).pure.widen
    case ("fixed-percent", Some(value))      => FixedPercent(value).pure.widen
    case ("random-max-percent", Some(value)) => RandomMaxPercent(value).pure.widen

    case (otherName, otherValue) =>
      UnknownModeType(s"Mode with name $otherName and value $otherValue cannot be created").raiseError
  }

  final case class UnknownModeType(msg: String) extends RuntimeException(msg) with NoStackTrace
}
