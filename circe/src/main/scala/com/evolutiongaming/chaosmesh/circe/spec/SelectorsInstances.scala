package com.evolutiongaming.chaosmesh.circe.spec

import com.evolutiongaming.chaosmesh.circe.k8s._
import com.evolutiongaming.chaosmesh.model.spec.Selectors
import io.circe._
import io.circe.generic.semiauto._

trait SelectorsInstances extends PodPhaseInstances with ExpressionInstances {
  implicit val selectorsEnc: Encoder[Selectors[Selectors.Filled]] = deriveEncoder
  implicit val selectorsDec: Decoder[Selectors[Selectors.Filled]] = deriveDecoder
}
