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
  val IdKey               = "id"
  val PhaseKey            = "phase"
  val SelectorKeyKey      = "selectorKey"
  val InjectedCountKey    = "injectedCount"
  val RecoveredCountKey   = "recoveredCount"
  val EventsKey           = "events"
  val EventTypeKey        = "type"
  val OperationKey        = "operation"
  val TimestampKey        = "timestamp"

  implicit val conditionDec: Decoder[Condition] =
    Decoder.instance { c =>
      for {
        cType        <- c.get[String](ConditionTypeKey)
        rawReasonStr <- c.get[Option[String]](ReasonKey)
        reason = rawReasonStr.map(_.trim()).filter(_.nonEmpty)
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

  implicit val containerRecordEventDec: Decoder[ContainerRecord.Event] =
    Decoder.instance { c =>
      for {
        eventType <- c.get[String](EventTypeKey)
        operation <- c.get[String](OperationKey)
        timestamp <- c.get[java.time.Instant](TimestampKey)
      } yield ContainerRecord.Event(
        `type` = eventType,
        operation = operation,
        timestamp = timestamp,
      )
    }

  implicit val containerRecordEventEnc: Encoder[ContainerRecord.Event] =
    Encoder.encodeJsonObject.contramap { event =>
      JsonObject(
        EventTypeKey -> Json.fromString(event.`type`),
        OperationKey -> Json.fromString(event.operation),
        TimestampKey -> Encoder[java.time.Instant].apply(event.timestamp),
      )
    }

  implicit val containerRecordDec: Decoder[ContainerRecord] =
    Decoder.instance { c =>
      for {
        id             <- c.get[String](IdKey)
        phase          <- c.get[String](PhaseKey)
        selectorKey    <- c.get[String](SelectorKeyKey)
        injectedCount  <- c.getOrElse[Int](InjectedCountKey)(0)
        recoveredCount <- c.getOrElse[Int](RecoveredCountKey)(0)
        events         <- c.getOrElse[List[ContainerRecord.Event]](EventsKey)(Nil)
      } yield ContainerRecord(
        id = id,
        phase = phase,
        selectorKey = selectorKey,
        injectedCount = injectedCount,
        recoveredCount = recoveredCount,
        events = events,
      )
    }

  implicit val containerRecordEnc: Encoder[ContainerRecord] =
    Encoder.encodeJsonObject.contramap { record =>
      JsonObject(
        IdKey             -> Json.fromString(record.id),
        PhaseKey          -> Json.fromString(record.phase),
        SelectorKeyKey    -> Json.fromString(record.selectorKey),
        InjectedCountKey  -> Json.fromInt(record.injectedCount),
        RecoveredCountKey -> Json.fromInt(record.recoveredCount),
        EventsKey         -> Encoder[List[ContainerRecord.Event]].apply(record.events),
      )
    }

  implicit val experimentStatusDec: Decoder[ExperimentStatus] = Decoder.instance { c =>
    for {
      containerRecords <- c.get[Option[List[ContainerRecord]]](ContainerRecordsKey)
      desiredPhase     <- c.get[Option[String]](DesiredPhaseKey)
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
