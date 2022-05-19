package com.evolutiongaming.chaosmesh.circe.kernelchaos

import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.kernelchaos._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

import scala.util.Try
import com.evolutiongaming.chaosmesh.circe.k8s._
import com.evolutiongaming.chaosmesh.model.k8s.ExperimentKind

trait KernelChaosInstances
    extends ModeInstances
    with SelectorsInstances
    with ExperimentKindInstances
    with ResourceMetadataInstances {

  implicit val faultTypeEnc: Encoder[FailKernRequest.FailType] =
    Encoder.encodeInt.contramap {
      case FailKernRequest.FailType.Failslab      => 0
      case FailKernRequest.FailType.FailAllocPage => 1
      case FailKernRequest.FailType.FailBio       => 2
    }

  implicit val faultTypeDec: Decoder[FailKernRequest.FailType] =
    Decoder.decodeInt.emapTry(FailKernRequest.FailType.from[Try](_))

  implicit val failKernRequestChainEnc: Encoder[FailKernRequest.Callchain] = deriveEncoder

  implicit val failKernRequestChainDec: Decoder[FailKernRequest.Callchain] = deriveDecoder

  implicit val failKernRequestEnc: Encoder[FailKernRequest] = deriveEncoder

  implicit val failKernRequestDec: Decoder[FailKernRequest] = deriveDecoder

  implicit val kernChaosSpecEnc: Encoder[KernelChaos.Spec] =
    deriveEncoder[KernelChaos.Spec]
      .mapJsonObject(_.deepMergeObjInField(ModeField))

  implicit val kernChaosSpecDec: Decoder[KernelChaos.Spec] =
    for {
      mode <- modeDec
      decoder <- deriveDecoder[KernelChaos.Spec]
        .prepare(_.replaceFieldValue(ModeField, mode.asJson))
    } yield decoder

  implicit val kernChaosKindDec: Decoder[ExperimentKind.KernelChaos.type] =
    experimentKindDec.narrow

  implicit val kernChaosDec: Decoder[KernelChaos] = deriveDecoder

}
