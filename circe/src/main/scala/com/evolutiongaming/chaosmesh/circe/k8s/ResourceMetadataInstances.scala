package com.evolutiongaming.chaosmesh.circe.k8s

import com.evolutiongaming.chaosmesh.model.k8s._
import io.circe._
import io.circe.generic.semiauto._

trait ResourceMetadataInstances {

  implicit val resourceMetadataEnc: Encoder[ResourceMetadata] = deriveEncoder

  implicit val resourceMetadataDec: Decoder[ResourceMetadata] = deriveDecoder

}
