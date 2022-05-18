package com.evolutiongaming.chaosmesh.model.k8s

import cats.syntax.all._
import cats.data.NonEmptyList
import cats.ApplicativeThrow
import scala.util.control.NoStackTrace
/**
  * see https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/#resources-that-support-set-based-requirements
  *
  */
final case class Expression(
  key:      String,
  operator: Expression.Operator,
)

object Expression {

  sealed abstract class Operator(
    val operator: String,
    val values:   Operator.Values,
  )

  object Operator {

    type Values = Option[NonEmptyList[String]]

    case class In(in: NonEmptyList[String]) extends Operator("In", in.some)

    object In {
      def apply(head: String, tail: String*): In =
        In(NonEmptyList.of(head, tail: _*))
    }

    case class NotIn(notIn: NonEmptyList[String]) extends Operator("NotIn", notIn.some)

    object NotIn {
      def apply(head: String, tail: String*): NotIn =
        NotIn(NonEmptyList.of(head, tail: _*))
    }

    case class Exists(exists: Option[NonEmptyList[String]]) extends Operator("Exists", exists)

    object Exists {
      def apply(values: String*): Exists =
        Exists(NonEmptyList.fromFoldable(values))
    }

    case class DoesNotExist(doesNotExist: Option[NonEmptyList[String]]) extends Operator("DoesNotExist", doesNotExist)

    object DoesNotExist {
      def apply(values: String*): DoesNotExist =
        DoesNotExist(NonEmptyList.fromFoldable(values))
    }

    def from[F[_]: ApplicativeThrow](name: String, values: Values): F[Operator] = (name, values) match {
      case ("In", values) =>
        values
          .liftTo[F](UnknownExpressionType("In operator must provide non empty values"))
          .map(In(_))
      case ("NotIn", values) =>
        values
          .liftTo[F](UnknownExpressionType("NotIn operator must provide non empty values"))
          .map(NotIn(_))
      case ("Exists", values)       => Exists(values).pure.widen
      case ("DoesNotExist", values) => DoesNotExist(values).pure.widen

      case (otherName, _) => UnknownExpressionType(s"Unknown expression operator: $otherName").raiseError
    }
  }

  final case class UnknownExpressionType(msg: String) extends RuntimeException(msg) with NoStackTrace

}
