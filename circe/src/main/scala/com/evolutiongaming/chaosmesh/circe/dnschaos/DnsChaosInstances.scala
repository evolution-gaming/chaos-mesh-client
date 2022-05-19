package com.evolutiongaming.chaosmesh.circe.dnschaos

import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.k8s._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.dnschaos.DnsChaos
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import com.evolutiongaming.chaosmesh.model.k8s.ExperimentKind

trait DnsChaosInstances
    extends DnsChaosActionInstances
    with ModeInstances
    with SelectorsInstances
    with ExperimentKindInstances
    with ResourceMetadataInstances {

  implicit val dnsChaosSpecEnc: Encoder.AsObject[DnsChaos.Spec] =
    deriveEncoder[DnsChaos.Spec]
      .mapJsonObject(_.deepMergeObjInField(ActionsEncoding.ActionFieldKey))
      .mapJsonObject(_.deepMergeObjInField(ModeField))

  implicit val dnsChaosSpecDec: Decoder[DnsChaos.Spec] =
    for {
      action <- dnsChaosActionDec
      mode   <- modeDec
      decoder <- deriveDecoder[DnsChaos.Spec]
        .prepare(_.replaceFieldValue(ActionsEncoding.ActionFieldKey, action.asJson))
        .prepare(_.replaceFieldValue(ModeField, mode.asJson))
    } yield decoder

  implicit val dnsChaosKindDec: Decoder[ExperimentKind.DnsChaos.type] =
    experimentKindDec.narrow

  implicit val dnsChaosDec: Decoder[DnsChaos] = deriveDecoder

}
