package com.evolutiongaming.chaosmesh.circe

import com.evolutiongaming.chaosmesh.circe.awschaos._
import com.evolutiongaming.chaosmesh.circe.dnschaos._
import com.evolutiongaming.chaosmesh.circe.gcpchaos._
import com.evolutiongaming.chaosmesh.circe.httpchaos._
import com.evolutiongaming.chaosmesh.circe.iochaos._
import com.evolutiongaming.chaosmesh.circe.jvmchaos._
import com.evolutiongaming.chaosmesh.circe.k8s.CustomResourceInstances
import com.evolutiongaming.chaosmesh.circe.kernelchaos._
import com.evolutiongaming.chaosmesh.circe.networkchaos._
import com.evolutiongaming.chaosmesh.circe.podchaos._
import com.evolutiongaming.chaosmesh.circe.stresschaos._
import com.evolutiongaming.chaosmesh.circe.timechaos._

object instances
    extends CustomResourceInstances
    with PodChaosInstances
    with NetworkChaosInstances
    with StressChaosInstances
    with IoChaosInstances
    with DnsChaosInstances
    with TimeChaosInstances
    with JvmChaosInstances
    with KernelChaosInstances
    with AwsChaosInstances
    with GcpChaosInstances
    with HttpChaosInstances
