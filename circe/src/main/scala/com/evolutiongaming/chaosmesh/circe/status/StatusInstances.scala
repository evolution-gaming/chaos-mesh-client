package com.evolutiongaming.chaosmesh.circe.status

import io.circe.{Decoder, Encoder}
import com.evolutiongaming.chaosmesh.model.status._
import scala.util.Try
import io.circe.DecodingFailure.apply
import io.circe.DecodingFailure
import io.circe.generic.semiauto._
import io.circe.JsonObject
import com.evolutiongaming.chaosmesh.model.status.Condition._
import io.circe.Json
import cats.syntax.all._

trait StatusInstances {

  val ConditionTypeKey = "type"
  val ReasonKey        = "reason"
  val StatusKey        = "status"

  implicit val conditionDec: Decoder[Condition] =
    Decoder.instance { c =>
      for {
        cType        <- c.get[String](ConditionTypeKey)
        rawReasonStr <- c.get[String](ReasonKey)
        reason = if (rawReasonStr.trim().isEmpty()) None else Some(rawReasonStr)
        status <- c
          .get[String](StatusKey)
          .flatMap { statusStr =>
            Try(statusStr.toBoolean).toEither
          }
          .left
          .map(err => DecodingFailure(err.getMessage(), c.history))
        result <- Condition
          .from[Try](cType, status, reason)
          .toEither
          .left
          .map(err => DecodingFailure(err.getMessage(), c.history))
      } yield result
    }

  implicit val conditionEnc: Encoder[Condition] = {
    def objFrom(cType: String, status: Boolean, reason: Option[String]): JsonObject =
      JsonObject(
        ConditionTypeKey -> Json.fromString(cType),
        ReasonKey        -> Json.fromString(reason.getOrElse("")),
        StatusKey        -> Json.fromBoolean(status),
      )
    Encoder.encodeJsonObject.contramap {
      case Selected            => objFrom("Selected", true, None)
      case NotSelected(reason) => objFrom("Selected", false, reason)
      case AllInjected         => objFrom("AllInjected", true, None)
      case NotInjected(reason) => objFrom("AllInjected", false, reason)
      case Running             => objFrom("Paused", false, None)
      case Paused(reason)      => objFrom("Paused", true, reason)
      case OngoingExperiment   => objFrom("Recovered", false, None)
      case Recovered(reason)   => objFrom("Recovered", true, reason)
    }
  }

  implicit val containerRecordDec: Decoder[ContainerRecord] = deriveDecoder

  implicit val containerRecordEnc: Encoder[ContainerRecord] = deriveEncoder

  implicit val experimentStatusDec: Decoder[ExperimentStatus] = deriveDecoder

  implicit val experimentStatusEnc: Encoder[ExperimentStatus] = deriveEncoder

  implicit val statusDec: Decoder[Status] = deriveDecoder

  implicit val statusEnc: Encoder[Status] = deriveEncoder

}
