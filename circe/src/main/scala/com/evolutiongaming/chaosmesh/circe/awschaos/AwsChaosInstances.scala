package com.evolutiongaming.chaosmesh.circe.awschaos

import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.common._
import com.evolutiongaming.chaosmesh.circe.k8s._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.awschaos.AwsChaos
import com.evolutiongaming.chaosmesh.model.k8s._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait AwsChaosInstances
    extends ModeInstances
    with SelectorsInstances
    with DurationInstances
    with AwsActionInstances
    with ExperimentKindInstances
    with ResourceMetadataInstances {

  implicit val awsChaosSpecEnc: Encoder.AsObject[AwsChaos.Spec] =
    deriveEncoder[AwsChaos.Spec]
      .mapJsonObject(_.deepMergeObjInField(ActionsEncoding.ActionFieldKey))
      .mapJsonObject(_.deepMergeObjInField(ModeField))

  implicit val awsChaosSpecDec: Decoder[AwsChaos.Spec] =
    for {
      action <- awsChaosActionDec
      mode   <- modeDec
      decoder <- deriveDecoder[AwsChaos.Spec]
        .prepare(_.replaceFieldValue(ActionsEncoding.ActionFieldKey, action.asJson))
        .prepare(_.replaceFieldValue(ModeField, mode.asJson))
    } yield decoder

  implicit val awsChaosKindDec: Decoder[ExperimentKind.AwsChaos.type] =
    experimentKindDec.narrow

  implicit val awsChaosDec: Decoder[AwsChaos] = deriveDecoder

}
