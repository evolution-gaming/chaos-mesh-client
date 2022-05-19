package com.evolutiongaming.chaosmesh.circe.spec

import cats.syntax.all._
import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.common._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.spec.Action
import com.evolutiongaming.chaosmesh.model.spec.Action.PodChaos
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait PodChaosActionInstances extends DurationInstances {

  implicit val containerKillEnc: Encoder.AsObject[PodChaos.ContainerKill] = deriveEncoder

  implicit val containerKillDec: Decoder[PodChaos.ContainerKill] = deriveDecoder

  implicit val podKillEnc: Encoder.AsObject[PodChaos.PodKill] = deriveEncoder

  implicit val podKillDec: Decoder[PodChaos.PodKill] = deriveDecoder

  implicit val podChaosActionEnc: Encoder.AsObject[Action.PodChaos] =
    Encoder.encodeJsonObject.contramapObject {
      case PodChaos.PodFailure =>
        JsonObject.empty.addType(ActionsEncoding.ActionFieldKey, "pod-failure")
      case containerKill: PodChaos.ContainerKill =>
        containerKill.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "container-kill")
      case podKill: PodChaos.PodKill =>
        podKill.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "pod-kill")
    }

  implicit val podChaosActionDec: Decoder[Action.PodChaos] =
    Decoder.instance { c =>
      for {
        actionType <- c.get[String](ActionsEncoding.ActionFieldKey)
        result <- actionType match {
          case "pod-kill"       => c.as[PodChaos.PodKill]
          case "container-kill" => c.as[PodChaos.ContainerKill]
          case "pod-failure"    => PodChaos.PodFailure.asRight
          case other => DecodingFailure(s"Unknown pod chaos action $other", c.history).asLeft
        }
      } yield result
    }

}
