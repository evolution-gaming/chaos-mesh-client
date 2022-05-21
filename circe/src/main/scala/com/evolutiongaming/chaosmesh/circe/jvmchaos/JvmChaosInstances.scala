package com.evolutiongaming.chaosmesh.circe.jvmchaos

import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.jvmchaos.JvmChaos
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import com.evolutiongaming.chaosmesh.circe.k8s._
import com.evolutiongaming.chaosmesh.model.k8s.ExperimentKind

trait JvmChaosInstances
    extends JvmChaosActionInstances
    with ModeInstances
    with SelectorsInstances
    with ExperimentKindInstances
    with ResourceMetadataInstances {

  implicit val jvmChaosSpecEnc: Encoder.AsObject[JvmChaos.Spec] =
    deriveEncoder[JvmChaos.Spec]
      .mapJsonObject(_.deepMergeObjInField(ModeField))
      .mapJsonObject(_.deepMergeObjInField(ActionsEncoding.ActionFieldKey))

  implicit val jvmChaosSpecDec: Decoder[JvmChaos.Spec] =
    for {
      action <- jvmChaosActionDec
      mode   <- modeDec
      decoder <- deriveDecoder[JvmChaos.Spec]
        .prepare(_.replaceFieldValue(ActionsEncoding.ActionFieldKey, action.asJson))
        .prepare(_.replaceFieldValue(ModeField, mode.asJson))
    } yield decoder

  implicit val jvmChaosKindDec: Decoder[ExperimentKind.JvmChaos.type] =
    experimentKindDec.narrow

  implicit val jvmChaosDec: Decoder[JvmChaos] = deriveDecoder

}
