package com.evolutiongaming.chaosmesh.circe.httpchaos

import cats.syntax.all._
import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.common._
import com.evolutiongaming.chaosmesh.circe.k8s._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.httpchaos.HttpChaos
import com.evolutiongaming.chaosmesh.model.k8s.ExperimentKind
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait HttpChaosInstances
    extends ModeInstances
    with SelectorsInstances
    with OptionalInfDurationInstances
    with ExperimentKindInstances
    with ResourceMetadataInstances {

  protected val TargetField              = "target"
  protected val CamelCaseResponseHeaders = "responseHeaders"
  protected val CamelCaseRequestHeaders  = "requestHeaders"
  protected val SnakeCaseResponseHeaders = "response_heads"
  protected val SnakeCaseRequestHeaders  = "request_headers"

  implicit val bodyEnc: Encoder[HttpChaos.Body] = deriveEncoder

  implicit val bodyDec: Decoder[HttpChaos.Body] =
    deriveDecoder[HttpChaos.Body] <+>
      Decoder.decodeNone.map(_ => HttpChaos.Body())

  implicit val patchEnc: Encoder[HttpChaos.Patch] = deriveEncoder

  implicit val patchDec: Decoder[HttpChaos.Patch] =
    deriveDecoder[HttpChaos.Patch] <+>
      Decoder.decodeNone.map(_ => HttpChaos.Patch())

  implicit val replaceEnc: Encoder[HttpChaos.Replace] = deriveEncoder

  implicit val replaceDec: Decoder[HttpChaos.Replace] =
    deriveDecoder[HttpChaos.Replace] <+>
      Decoder.decodeNone.map(_ => HttpChaos.Replace())

  implicit val requestReplaceEnc: Encoder[HttpChaos.RequestReplace] = deriveEncoder

  implicit val requestReplaceDec: Decoder[HttpChaos.RequestReplace] =
    deriveDecoder[HttpChaos.RequestReplace] <+>
      Decoder.decodeNone.map(_ => HttpChaos.RequestReplace())

  implicit val requestPatchEnc: Encoder[HttpChaos.RequestPatch] = deriveEncoder

  implicit val requestPatchDec: Decoder[HttpChaos.RequestPatch] =
    deriveDecoder[HttpChaos.RequestPatch] <+>
      Decoder.decodeNone.map(_ => HttpChaos.RequestPatch())

  implicit val responseReplaceEnc: Encoder[HttpChaos.ResponseReplace] =
    deriveEncoder[HttpChaos.ResponseReplace]
      .mapJsonObject(_.renameField(CamelCaseResponseHeaders, SnakeCaseResponseHeaders))

  implicit val responseReplaceDec: Decoder[HttpChaos.ResponseReplace] =
    deriveDecoder[HttpChaos.ResponseReplace]
      .prepare(_.renameField(SnakeCaseResponseHeaders, CamelCaseResponseHeaders))

  implicit val targetResponseEnc: Encoder.AsObject[HttpChaos.Target.Response] = deriveEncoder

  implicit val targetResponseDec: Decoder[HttpChaos.Target.Response] = deriveDecoder

  implicit val targetRequestEnc: Encoder.AsObject[HttpChaos.Target.Request] = deriveEncoder

  implicit val targetRequestDec: Decoder[HttpChaos.Target.Request] = deriveDecoder

  implicit val targetEnc: Encoder.AsObject[HttpChaos.Target] =
    Encoder.encodeJsonObject.contramapObject {
      case request: HttpChaos.Target.Request =>
        request.asJsonObject.addType(TargetField, "Request")
      case response: HttpChaos.Target.Response =>
        response.asJsonObject.addType(TargetField, "Response")
    }

  implicit val targetDec: Decoder[HttpChaos.Target] =
    Decoder.instance { c =>
      for {
        targetType <- c.get[String](TargetField)
        result <- targetType match {
          case "Request"  => c.as[HttpChaos.Target.Request]
          case "Response" => c.as[HttpChaos.Target.Response]
          case other      => DecodingFailure(s"Unknown target type $other", c.history).asLeft
        }
      } yield result
    }

  implicit val httpChaosSpecEnc: Encoder[HttpChaos.Spec] =
    deriveEncoder[HttpChaos.Spec]
      .mapJsonObject(_.deepMergeObjInField(TargetField))
      .mapJsonObject(_.deepMergeObjInField(ModeField))
      .mapJsonObject(_.renameField(CamelCaseRequestHeaders, SnakeCaseRequestHeaders))

  implicit val httpChaosSpecDec: Decoder[HttpChaos.Spec] =
    for {
      target <- targetDec
      mode   <- modeDec
      decoder <- deriveDecoder[HttpChaos.Spec]
        .prepare(_.renameField(SnakeCaseRequestHeaders, CamelCaseRequestHeaders))
        .prepare(_.replaceFieldValue(TargetField, target.asJson))
        .prepare(_.replaceFieldValue(ModeField, mode.asJson))
    } yield decoder

  implicit val httpChaosKindDec: Decoder[ExperimentKind.HttpChaos.type] =
    experimentKindDec.narrow

  implicit val httpChaosDec: Decoder[HttpChaos] = deriveDecoder

}
