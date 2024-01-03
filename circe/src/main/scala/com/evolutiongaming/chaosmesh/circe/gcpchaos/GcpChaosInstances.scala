package com.evolutiongaming.chaosmesh.circe.gcpchaos

import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.common._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.gcpchaos.GcpChaos
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import com.evolutiongaming.chaosmesh.circe.k8s._
import com.evolutiongaming.chaosmesh.model.k8s.ExperimentKind

trait GcpChaosInstances
    extends ModeInstances
    with SelectorsInstances
    with OptionalInfDurationInstances
    with GcpChaosActionInstances
    with ExperimentKindInstances
    with ResourceMetadataInstances {

  implicit val gcpChaosSpecEnc: Encoder.AsObject[GcpChaos.Spec] =
    deriveEncoder[GcpChaos.Spec]
      .mapJsonObject(_.deepMergeObjInField(ActionsEncoding.ActionFieldKey))
      .mapJsonObject(_.deepMergeObjInField(ModeField))

  implicit val gcpChaosSpecDec: Decoder[GcpChaos.Spec] =
    for {
      action <- gcpChaosActionDec
      mode   <- modeDec
      decoder <- deriveDecoder[GcpChaos.Spec]
        .prepare(_.replaceFieldValue(ActionsEncoding.ActionFieldKey, action.asJson))
        .prepare(_.replaceFieldValue(ModeField, mode.asJson))
    } yield decoder

  implicit val gcpChaosKindDec: Decoder[ExperimentKind.GcpChaos.type] =
    experimentKindDec.narrow

  implicit val gcpChaosDec: Decoder[GcpChaos] = deriveDecoder

}
