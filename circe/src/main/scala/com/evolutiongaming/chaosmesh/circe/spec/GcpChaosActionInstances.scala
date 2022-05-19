package com.evolutiongaming.chaosmesh.circe.spec

import cats.syntax.all._
import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.common._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.spec.Action
import com.evolutiongaming.chaosmesh.model.spec.Action.GcpChaos
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait GcpChaosActionInstances extends DurationInstances {

  implicit val gcpDiskLossEnc: Encoder.AsObject[GcpChaos.DiskLoss] = deriveEncoder

  implicit val gcpDiskLossDec: Decoder[GcpChaos.DiskLoss] = deriveDecoder

  implicit val gcpChaosActionEnc: Encoder.AsObject[Action.GcpChaos] =
    Encoder.encodeJsonObject.contramapObject {
      case GcpChaos.NodeStop =>
        JsonObject.empty.addType(ActionsEncoding.ActionFieldKey, "node-stop")
      case GcpChaos.NodeReset =>
        JsonObject.empty.addType(ActionsEncoding.ActionFieldKey, "node-reset")
      case loss: GcpChaos.DiskLoss =>
        loss.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "disk-loss")
    }

  implicit val gcpChaosActionDec: Decoder[Action.GcpChaos] =
    Decoder.instance { c =>
      for {
        actionType <- c.get[String](ActionsEncoding.ActionFieldKey)
        result <- actionType match {
          case "node-stop"  => GcpChaos.NodeStop.asRight
          case "node-reset" => GcpChaos.NodeReset.asRight
          case "disk-loss"  => c.as[GcpChaos.DiskLoss]
          case other        => DecodingFailure(s"Unknown GCP chaos action $other", c.history).asLeft
        }
      } yield result
    }

}
