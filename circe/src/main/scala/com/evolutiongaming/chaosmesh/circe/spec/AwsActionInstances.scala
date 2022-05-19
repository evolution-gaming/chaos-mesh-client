package com.evolutiongaming.chaosmesh.circe.spec

import cats.syntax.all._
import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
import com.evolutiongaming.chaosmesh.circe.common._
import com.evolutiongaming.chaosmesh.circe.spec._
import com.evolutiongaming.chaosmesh.model.spec.Action
import com.evolutiongaming.chaosmesh.model.spec.Action.AwsChaos
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait AwsActionInstances extends DurationInstances {

  implicit val awsDetainVolumeEnc: Encoder.AsObject[AwsChaos.DetainVolume] = deriveEncoder

  implicit val awsDetainVolumeDec: Decoder[AwsChaos.DetainVolume] = deriveDecoder

  implicit val awsChaosActionEnc: Encoder.AsObject[Action.AwsChaos] =
    Encoder.encodeJsonObject.contramapObject {
      case AwsChaos.EC2Restart =>
        JsonObject.empty.addType(ActionsEncoding.ActionFieldKey, "ec2-restart")
      case AwsChaos.EC2Stop =>
        JsonObject.empty.addType(ActionsEncoding.ActionFieldKey, "ec2-stop")
      case detain: AwsChaos.DetainVolume =>
        detain.asJsonObject.addType(ActionsEncoding.ActionFieldKey, "detain-volume")
    }

  implicit val awsChaosActionDec: Decoder[Action.AwsChaos] =
    Decoder.instance { c =>
      for {
        actionType <- c.get[String](ActionsEncoding.ActionFieldKey)
        result <- actionType match {
          case "ec2-restart"   => AwsChaos.EC2Restart.asRight
          case "ec2-stop"      => AwsChaos.EC2Stop.asRight
          case "detain-volume" => c.as[AwsChaos.DetainVolume]
          case other => DecodingFailure(s"Unknown AWS chaos action $other", c.history).asLeft
        }
      } yield result
    }

}
