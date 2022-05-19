package com.evolutiongaming.chaosmesh.circe.spec

import cats.syntax.all._
import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.common._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.spec.Action
import com.evolutiongaming.chaosmesh.model.spec.Action.JvmChaos
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait JvmChaosActionInstances extends DurationInstances {

  implicit val jvmLatencyEnc: Encoder.AsObject[JvmChaos.Latency] = deriveEncoder

  implicit val jvmLatencyDec: Decoder[JvmChaos.Latency] = deriveDecoder

  implicit val jvmReturnEnc: Encoder.AsObject[JvmChaos.Return] = deriveEncoder

  implicit val jvmReturnDec: Decoder[JvmChaos.Return] = deriveDecoder

  implicit val jvmExceptionEnc: Encoder.AsObject[JvmChaos.Exception] = deriveEncoder

  implicit val jvmExceptionDec: Decoder[JvmChaos.Exception] = deriveDecoder

  implicit val jvmCpuStressEnc: Encoder.AsObject[JvmChaos.CpuStress] = deriveEncoder

  implicit val jvmCpuStressDec: Decoder[JvmChaos.CpuStress] = deriveDecoder

  implicit val jvmMemOverflowEnc: Encoder.AsObject[JvmChaos.MemOverflow] = deriveEncoder

  implicit val jvmMemOverflowDec: Decoder[JvmChaos.MemOverflow] = deriveDecoder

  implicit val jvmStressDec: Decoder[JvmChaos.Stress] =
    jvmCpuStressDec.widen[JvmChaos.Stress] <+> jvmMemOverflowDec.widen[JvmChaos.Stress]

  implicit val jvmRuleDataEnc: Encoder.AsObject[JvmChaos.RuleData] = deriveEncoder

  implicit val jvmRuleDataDec: Decoder[JvmChaos.RuleData] = deriveDecoder

  implicit val jvmChaosActionEnc: Encoder.AsObject[Action.JvmChaos] =
    Encoder.encodeJsonObject.contramapObject {
      case latency: JvmChaos.Latency =>
        latency.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "latency")
      case ret: JvmChaos.Return =>
        ret.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "return")
      case ex: JvmChaos.Exception =>
        ex.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "exception")
      case stress: JvmChaos.CpuStress =>
        stress.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "stress")
      case stress: JvmChaos.MemOverflow =>
        stress.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "stress")
      case JvmChaos.GC =>
        JsonObject.empty.addType(ActionsEncoding.ActionFieldKey, "gc")
      case rule: JvmChaos.RuleData =>
        rule.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "ruleData")
    }

  implicit val jvmChaosActionDec: Decoder[Action.JvmChaos] =
    Decoder.instance { c =>
      for {
        actionType <- c.get[String](ActionsEncoding.ActionFieldKey)
        result <- actionType match {
          case "latency"   => c.as[JvmChaos.Latency]
          case "return"    => c.as[JvmChaos.Return]
          case "exception" => c.as[JvmChaos.Exception]
          case "stress"    => c.as[JvmChaos.Stress]
          case "gc"        => JvmChaos.GC.asRight
          case "ruleData"  => c.as[JvmChaos.RuleData]
          case other       => DecodingFailure(s"Unknown pod chaos action $other", c.history).asLeft
        }
      } yield result
    }

}
