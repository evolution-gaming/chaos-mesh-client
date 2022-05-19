package com.evolutiongaming.chaosmesh.circe.k8s

import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.model.k8s.Expression
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

import scala.util.Try

trait ExpressionInstances {

  private val OperatorField = "operator"

  private val ValuesField = "values"

  implicit val operatorEnc: Encoder.AsObject[Expression.Operator] =
    Encoder.forProduct2(OperatorField, ValuesField) { op =>
      (op.operator, op.values)
    }

  implicit val operatorDec: Decoder[Expression.Operator] =
    Decoder.instanceTry { c =>
      for {
        operatorName <- c.get[String](OperatorField).toTry
        values       <- c.get[Expression.Operator.Values](ValuesField).toTry
        operator     <- Expression.Operator.from[Try](operatorName, values)
      } yield operator
    }

  implicit val expressionEnc: Encoder[Expression] =
    deriveEncoder[Expression]
      .mapJsonObject(_.deepMergeObjInField(OperatorField))

  implicit val expressionDec: Decoder[Expression] =
    for {
      operator <- operatorDec
      decoded <- deriveDecoder[Expression]
        .prepare(_.root.replaceFieldValue(OperatorField, operator.asJson))
    } yield decoded

}
