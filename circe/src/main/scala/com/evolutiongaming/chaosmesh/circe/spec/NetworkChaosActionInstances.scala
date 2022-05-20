package com.evolutiongaming.chaosmesh.circe.spec

import cats.syntax.all._
import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.common._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.spec.Action
import com.evolutiongaming.chaosmesh.model.spec.Action.NetChaos
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait NetworkChaosActionInstances extends DurationInstances {

  implicit val netBandwidthEnc: Encoder.AsObject[NetChaos.BandwidthLimit] = deriveEncoder

  implicit val netBandwidthDec: Decoder[NetChaos.BandwidthLimit] = deriveDecoder

  implicit val netPacketLossEnc: Encoder.AsObject[NetChaos.PacketLoss] = deriveEncoder

  implicit val netPacketLossDec: Decoder[NetChaos.PacketLoss] = deriveDecoder

  implicit val netPacketCorruptEnc: Encoder.AsObject[NetChaos.PacketCorrupt] = deriveEncoder

  implicit val netPacketCorruptDec: Decoder[NetChaos.PacketCorrupt] = deriveDecoder

  implicit val netPacketReorderEnc: Encoder.AsObject[NetChaos.PacketReorder] = deriveEncoder

  implicit val netPacketReorderDec: Decoder[NetChaos.PacketReorder] = deriveDecoder

  implicit val netDelayRulesEnc: Encoder.AsObject[NetChaos.DelayRules] = deriveEncoder

  implicit val netDelayRulesDec: Decoder[NetChaos.DelayRules] = deriveDecoder

  implicit val netDelayEnc: Encoder.AsObject[NetChaos.Delay] = deriveEncoder

  implicit val netDelayDec: Decoder[NetChaos.Delay] = deriveDecoder

  implicit val netDuplicateEnc: Encoder.AsObject[NetChaos.Duplicate] = deriveEncoder

  implicit val netDuplicateDec: Decoder[NetChaos.Duplicate] = deriveDecoder

  implicit val netChaosActionEnc: Encoder.AsObject[Action.NetChaos] =
    Encoder.encodeJsonObject.contramapObject {
      case NetChaos.NetPartition =>
        JsonObject.empty.addType(ActionsEncoding.ActionFieldKey, "partition")
      case bandwidth: NetChaos.BandwidthLimit =>
        bandwidth.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "bandwidth")
      case loss: NetChaos.PacketLoss =>
        loss.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "loss")
      case corrupt: NetChaos.PacketCorrupt =>
        corrupt.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "corrupt")
      case delay: NetChaos.Delay =>
        delay.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "delay")
      case duplicate: NetChaos.Duplicate =>
        duplicate.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "duplicate")
    }

  implicit val netChaosActionDec: Decoder[Action.NetChaos] =
    Decoder.instance { c =>
      for {
        actionType <- c.get[String](ActionsEncoding.ActionFieldKey)
        result <- actionType match {
          case "partition" => NetChaos.NetPartition.asRight
          case "bandwidth" => c.as[NetChaos.BandwidthLimit]
          case "loss"      => c.as[NetChaos.PacketLoss]
          case "corrupt"   => c.as[NetChaos.PacketCorrupt]
          case "delay"     => c.as[NetChaos.Delay]
          case "duplicate" => c.as[NetChaos.Duplicate]
          case other => DecodingFailure(s"Unknown network chaos action $other", c.history).asLeft
        }
      } yield result
    }

}
