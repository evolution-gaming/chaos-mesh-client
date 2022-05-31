package com.evolutiongaming.chaosmesh.circe.iochaos

import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.common._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.iochaos.IoChaos
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import com.evolutiongaming.chaosmesh.circe.k8s._
import com.evolutiongaming.chaosmesh.model.k8s.ExperimentKind

trait IoChaosInstances
    extends IoChaosActionInstances
    with ModeInstances
    with SelectorsInstances
    with DurationInstances
    with ExperimentKindInstances
    with ResourceMetadataInstances {

  implicit val ioChaosSpecEnc: Encoder.AsObject[IoChaos.Spec] =
    deriveEncoder[IoChaos.Spec]
      .mapJsonObject(_.deepMergeObjInField(ActionsEncoding.ActionFieldKey))
      .mapJsonObject(_.deepMergeObjInField(ModeField))

  implicit val ioChaosSpecDec: Decoder[IoChaos.Spec] =
    for {
      action <- ioChaosActionDec
      mode   <- modeDec
      decoder <- deriveDecoder[IoChaos.Spec]
        .prepare(_.replaceFieldValue(ActionsEncoding.ActionFieldKey, action.asJson))
        .prepare(_.replaceFieldValue(ModeField, mode.asJson))
    } yield decoder

  implicit val ioChaosKindDec: Decoder[ExperimentKind.IoChaos.type] =
    experimentKindDec.narrow

  implicit val ioChaosDec: Decoder[IoChaos] = deriveDecoder
}
