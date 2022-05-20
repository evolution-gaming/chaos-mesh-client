package com.evolutiongaming.chaosmesh.circe.networkchaos

import cats.syntax.all._
import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.k8s._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.k8s.ExperimentKind
import com.evolutiongaming.chaosmesh.model.networkchaos.NetChaos
import com.evolutiongaming.chaosmesh.model.spec.Direction
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait NetworkChaosInstances
    extends NetworkChaosActionInstances
    with ModeInstances
    with SelectorsInstances
    with ExperimentKindInstances
    with ResourceMetadataInstances {

  protected val DirectionField = "direction"

  implicit val netDirectionTargetEnc: Encoder[Direction.Target] =
    deriveEncoder[Direction.Target]
      .mapJsonObject(_.deepMergeObjInField(ModeField))

  implicit val netDirectionTargetDec: Decoder[Direction.Target] =
    for {
      mode <- modeDec
      decoder <- deriveDecoder[Direction.Target]
        .prepare(_.replaceFieldValue(ModeField, mode.asJson))
    } yield decoder

  implicit val netDirectionToEnc: Encoder.AsObject[Direction.To] = deriveEncoder

  implicit val netDirectionToDec: Decoder[Direction.To] = deriveDecoder

  implicit val netDirectionFromDec: Decoder[Direction.From] = deriveDecoder

  implicit val netDirectionFromEnc: Encoder.AsObject[Direction.From] = deriveEncoder

  implicit val netDirectionBothDec: Decoder[Direction.Both] = deriveDecoder

  implicit val netDirectionBothEnc: Encoder.AsObject[Direction.Both] = deriveEncoder

  implicit val directionEnc: Encoder.AsObject[Direction] =
    Encoder.encodeJsonObject.contramapObject {
      case to: Direction.To =>
        to.asJsonObject.addType(DirectionField, "to")
      case from: Direction.From =>
        from.asJsonObject.addType(DirectionField, "from")
      case both: Direction.Both =>
        both.asJsonObject.addType(DirectionField, "both")
    }

  implicit val directionDec: Decoder[Direction] =
    Decoder.instance { c =>
      for {
        directionType <- c.get[String](DirectionField)
        result <- directionType match {
          case "to"   => c.as[Direction.To]
          case "from" => c.as[Direction.From]
          case "both" => c.as[Direction.Both]
          case other  => DecodingFailure(s"Unknown target type $other", c.history).asLeft
        }
      } yield result
    }

  implicit val netChaosSpecEnc: Encoder.AsObject[NetChaos.Spec] =
    deriveEncoder[NetChaos.Spec]
      .mapJsonObject(_.deepMergeObjInField(DirectionField))
      .mapJsonObject(_.deepMergeObjInField(ActionsEncoding.ActionFieldKey))
      .mapJsonObject(_.deepMergeObjInField(ModeField))

  implicit val netChaosSpecDec: Decoder[NetChaos.Spec] =
    for {
      action    <- netChaosActionDec
      mode      <- modeDec
      direction <- directionDec
      decoder <- deriveDecoder[NetChaos.Spec]
        .prepare(_.replaceFieldValue(DirectionField, direction.asJson))
        .prepare(_.replaceFieldValue(ActionsEncoding.ActionFieldKey, action.asJson))
        .prepare(_.replaceFieldValue(ModeField, mode.asJson))
    } yield decoder

  implicit val netChaosKindDec: Decoder[ExperimentKind.NetworkChaos.type] =
    experimentKindDec.narrow

  implicit val netChaosDec: Decoder[NetChaos] = deriveDecoder

}
