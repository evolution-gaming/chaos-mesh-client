package com.evolutiongaming.chaosmesh.circe.stresschaos

import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.common._
import com.evolutiongaming.chaosmesh.circe.k8s._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.k8s.ExperimentKind
import com.evolutiongaming.chaosmesh.model.stresschaos.StressChaos
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait StressChaosInstances
    extends ModeInstances
    with SelectorsInstances
    with DurationInstances
    with ExperimentKindInstances
    with ResourceMetadataInstances {

  implicit val cpuStressorEnc: Encoder[StressChaos.CpuStressor] = deriveEncoder

  implicit val cpuStressorDec: Decoder[StressChaos.CpuStressor] = deriveDecoder

  implicit val memStressorEnc: Encoder[StressChaos.MemoryStressor] = deriveEncoder

  implicit val memStressorDec: Decoder[StressChaos.MemoryStressor] = deriveDecoder

  implicit val stressorsEnc: Encoder[StressChaos.Stressors] = deriveEncoder

  implicit val stressorsDec: Decoder[StressChaos.Stressors] = deriveDecoder

  implicit val stressChaosSpecEnc: Encoder[StressChaos.Spec] =
    deriveEncoder[StressChaos.Spec]
      .mapJsonObject(_.deepMergeObjInField(ModeField))

  implicit val stressChaosSpecDec: Decoder[StressChaos.Spec] =
    for {
      mode <- modeDec
      decoder <- deriveDecoder[StressChaos.Spec]
        .prepare(_.replaceFieldValue(ModeField, mode.asJson))
    } yield decoder

  implicit val stressChaosKindDec: Decoder[ExperimentKind.StressChaos.type] =
    experimentKindDec.narrow

  implicit val stressChaosDec: Decoder[StressChaos] = deriveDecoder

}
