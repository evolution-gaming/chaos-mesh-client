package com.evolutiongaming.chaosmesh.circe.spec

import cats.syntax.all._
import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.common._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.spec.Action
import com.evolutiongaming.chaosmesh.model.spec.Action.IoChaos
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait IoChaosActionInstances extends DurationInstances {

  implicit val diskLatencyEnc: Encoder.AsObject[IoChaos.Latency] = deriveEncoder

  implicit val diskLatencyDec: Decoder[IoChaos.Latency] = deriveDecoder

  implicit val diskFaultEnc: Encoder.AsObject[IoChaos.Fault] = deriveEncoder

  implicit val diskFaultDec: Decoder[IoChaos.Fault] = deriveDecoder

  implicit val diskAttrTimeSpecEnc: Encoder[IoChaos.TimeSpec] = deriveEncoder

  implicit val diskAttrTimeSpecDec: Decoder[IoChaos.TimeSpec] = deriveDecoder

  implicit val diskAttrOverrideSpecEnc: Encoder[IoChaos.AttrOverrideRules] = deriveEncoder

  implicit val diskAttrOverrideSpecDec: Decoder[IoChaos.AttrOverrideRules] = deriveDecoder

  implicit val diskAttrOverrideEnc: Encoder.AsObject[IoChaos.AttrOverride] = deriveEncoder

  implicit val diskAttrOverrideDec: Decoder[IoChaos.AttrOverride] = deriveDecoder

  implicit val diskMistakeSpecEnc: Encoder[IoChaos.MistakeRules] = deriveEncoder

  implicit val diskMistakeSpecDec: Decoder[IoChaos.MistakeRules] = deriveDecoder

  implicit val diskMistakeEnc: Encoder.AsObject[IoChaos.Mistake] = deriveEncoder

  implicit val diskMistakeDec: Decoder[IoChaos.Mistake] = deriveDecoder

  implicit val ioChaosActionEnc: Encoder.AsObject[Action.IoChaos] =
    Encoder.encodeJsonObject.contramapObject {
      case latency: IoChaos.Latency =>
        latency.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "latency")
      case fault: IoChaos.Fault =>
        fault.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "fault")
      case attr: IoChaos.AttrOverride =>
        attr.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "attrOverride")
      case mistake: IoChaos.Mistake =>
        mistake.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "mistake")
    }

  implicit val ioChaosActionDec: Decoder[Action.IoChaos] =
    Decoder.instance { c =>
      for {
        actionType <- c.get[String](ActionsEncoding.ActionFieldKey)
        result <- actionType match {
          case "latency"      => c.as[IoChaos.Latency]
          case "fault"        => c.as[IoChaos.Fault]
          case "attrOverride" => c.as[IoChaos.AttrOverride]
          case "mistake"      => c.as[IoChaos.Mistake]
          case other => DecodingFailure(s"Unknown IO chaos action $other", c.history).asLeft
        }
      } yield result
    }

}
