package com.evolutiongaming.chaosmesh.circe.spec

import cats.syntax.all._
import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.common._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.spec.Action
import com.evolutiongaming.chaosmesh.model.spec.Action.DnsChaos
import io.circe._

trait DnsChaosActionInstances extends DurationInstances {

  implicit val dnsChaosActionEnc: Encoder.AsObject[Action.DnsChaos] =
    Encoder.encodeJsonObject.contramapObject {
      case DnsChaos.Random =>
        JsonObject.empty.addType(ActionsEncoding.ActionFieldKey, "random")
      case DnsChaos.Error =>
        JsonObject.empty.addType(ActionsEncoding.ActionFieldKey, "error")
    }

  implicit val dnsChaosActionDec: Decoder[Action.DnsChaos] =
    Decoder.instance { c =>
      for {
        actionType <- c.get[String](ActionsEncoding.ActionFieldKey)
        result <- actionType match {
          case "random" => DnsChaos.Random.asRight
          case "error"  => DnsChaos.Error.asRight
          case other    => DecodingFailure(s"Unknown DNS chaos action $other", c.history).asLeft
        }
      } yield result
    }

}
