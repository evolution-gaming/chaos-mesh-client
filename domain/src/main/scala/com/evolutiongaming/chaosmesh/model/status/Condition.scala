package com.evolutiongaming.chaosmesh.model.status

import cats.syntax.all._
import cats.ApplicativeThrow
import scala.util.control.NoStackTrace

sealed trait Condition

object Condition {

  val SelectedType  = "Selected"
  val InjectedType  = "AllInjected"
  val PausedType    = "Paused"
  val RecoveredType = "AllRecovered"

  case object Selected                                 extends Condition
  final case class NotSelected(reason: Option[String]) extends Condition
  case object AllInjected                              extends Condition
  final case class NotInjected(reason: Option[String]) extends Condition
  case object Running                                  extends Condition
  final case class Paused(reason: Option[String])      extends Condition
  case object OngoingExperiment                        extends Condition
  final case class Recovered(reason: Option[String])   extends Condition

  def from[F[_]: ApplicativeThrow](
    name:   String,
    status: Boolean,
    reason: Option[String],
  ): F[Condition] =
    (name, status) match {
      case (SelectedType, true)   => Selected.pure
      case (SelectedType, false)  => NotSelected(reason).pure
      case (InjectedType, true)   => AllInjected.pure
      case (InjectedType, false)  => NotInjected(reason).pure
      case (PausedType, false)    => Running.pure
      case (PausedType, true)     => Paused(reason).pure
      case (RecoveredType, false) => OngoingExperiment.pure
      case (RecoveredType, true)  => Recovered(reason).pure
      case otherName =>
        UnknownConditionType(
          s"Condition with name $otherName cannot be created",
        ).raiseError
    }

  final case class UnknownConditionType(msg: String) extends RuntimeException(msg) with NoStackTrace

}
