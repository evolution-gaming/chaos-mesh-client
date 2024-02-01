package com.evolutiongaming.chaosmesh.circe.status

import com.evolutiongaming.chaosmesh.model.status.Condition._
import com.evolutiongaming.chaosmesh.model.status._
import io.circe.generic.semiauto._
import io.circe._

import scala.util.Try

trait StatusInstances {

  val ConditionTypeKey    = "type"
  val ReasonKey           = "reason"
  val StatusKey           = "status"
  val ConditionsListKey   = "conditions"
  val InstancesKey        = "instances"
  val ExperimentKey       = "experiment"
  val ContainerRecordsKey = "containerRecords"
  val DesiredPhaseKey     = "desiredPhase"

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

  implicit val experimentStatusDec: Decoder[ExperimentStatus] = Decoder.instance { c =>
    for {
      containerRecords <- c.get[Option[List[ContainerRecord]]](ContainerRecordsKey)
      desiredPhase     <- c.get[String](DesiredPhaseKey)
    } yield ExperimentStatus(
      containerRecords = containerRecords.getOrElse(List.empty),
      desiredPhase = desiredPhase,
    )
  }

  implicit val experimentStatusEnc: Encoder[ExperimentStatus] = deriveEncoder

  implicit val instanceDataWithStartTime: Decoder[InstanceData.StartTimeData] =
    deriveDecoder

  implicit val instanceDataDec: Decoder[InstanceData] =
    Decoder.decodeInt
      .map(InstanceData.IntValue(_))
      .or(instanceDataWithStartTime.map[InstanceData](identity))
      .or(Decoder.failedWithMessage[InstanceData]("not known type of InstanceData"))

  implicit val instanceDataEnc: Encoder[InstanceData] =
    Encoder.encodeNone.contramap(_ => None)

  implicit val statusDec: Decoder[Status] = Decoder.instance { c =>
    for {
      conditionsList <- c.get[Option[List[Condition]]](ConditionsListKey)
      instances      <- c.get[Option[Map[String, InstanceData]]](InstancesKey)
      experiment     <- c.get[ExperimentStatus](ExperimentKey)
    } yield Status(
      conditions = conditionsList.getOrElse(List.empty),
      instances = instances.getOrElse(Map.empty),
      experiment = experiment,
    )
  }

  implicit val statusEnc: Encoder[Status] = deriveEncoder

}
