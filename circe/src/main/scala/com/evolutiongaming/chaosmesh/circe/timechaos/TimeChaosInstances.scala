package com.evolutiongaming.chaosmesh.circe.timechaos

import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.common._
import com.evolutiongaming.chaosmesh.circe.k8s.{ExperimentKindInstances, ResourceMetadataInstances}
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.k8s.ExperimentKind
import com.evolutiongaming.chaosmesh.model.timechaos.TimeChaos
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait TimeChaosInstances
    extends ModeInstances
    with SelectorsInstances
    with DurationInstances
    with ExperimentKindInstances
    with ResourceMetadataInstances {

  implicit val timeChaosSpecEnc: Encoder.AsObject[TimeChaos.Spec] =
    deriveEncoder[TimeChaos.Spec]
      .mapJsonObject(_.deepMergeObjInField(ModeField))

  implicit val timeChaosSpecDec: Decoder[TimeChaos.Spec] =
    for {
      mode <- modeDec
      decoder <- deriveDecoder[TimeChaos.Spec]
        .prepare(_.replaceFieldValue(ModeField, mode.asJson))
    } yield decoder

  implicit val timeChaosKindDec: Decoder[ExperimentKind.TimeChaos.type] =
    experimentKindDec.narrow

  implicit val timeChaosDec: Decoder[TimeChaos] = deriveDecoder

}
