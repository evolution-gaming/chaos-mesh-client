package com.evolutiongaming.chaosmesh.kubernetes

import com.evolutiongaming.chaosmesh.circe.instances._
import com.evolutiongaming.chaosmesh.model._
import com.evolutiongaming.chaosmesh.model.k8s.Api
import com.goyeau.kubernetes.client.KubernetesClient
import com.goyeau.kubernetes.client.crd.CrdContext
import io.circe.Json

import ChaosMeshApi._
import com.evolutiongaming.chaosmesh.model.status.Status

class ChaosMeshApi[F[_]](client: KubernetesClient[F]) {

  lazy val pod =
    client.customResources[podchaos.PodChaos.Spec, Status](podChaosContext)

  lazy val network =
    client.customResources[networkchaos.NetChaos.Spec, Status](networkChaosContext)

  lazy val stress =
    client.customResources[stresschaos.StressChaos.Spec, Status](stressChaosContext)

  lazy val io =
    client.customResources[iochaos.IoChaos.Spec, Status](ioChaosContext)

  lazy val dns =
    client.customResources[dnschaos.DnsChaos.Spec, Status](dnsChaosContext)

  lazy val time =
    client.customResources[timechaos.TimeChaos.Spec, Status](timeChaosContext)

  lazy val jvm =
    client.customResources[jvmchaos.JvmChaos.Spec, Status](jvmChaosContext)

  lazy val kernel =
    client.customResources[kernelchaos.KernelChaos.Spec, Status](kernelChaosContext)

  lazy val aws =
    client.customResources[awschaos.AwsChaos.Spec, Status](awsChaosContext)

  lazy val gcp =
    client.customResources[gcpchaos.GcpChaos.Spec, Status](gcpChaosContext)

  lazy val http =
    client.customResources[httpchaos.HttpChaos.Spec, Status](httpChaosContext)

}

object ChaosMeshApi {

  def apply[F[_]](
    client: KubernetesClient[F],
  ): ChaosMeshApi[F] = new ChaosMeshApi[F](client)

  private def ctx(name: String) =
    CrdContext(Api.Group, Api.Version, name)

  private val podChaosContext     = ctx("podchaos")
  private val networkChaosContext = ctx("networkchaos")
  private val stressChaosContext  = ctx("stresschaos")
  private val ioChaosContext      = ctx("iochaos")
  private val dnsChaosContext     = ctx("dnschaos")
  private val timeChaosContext    = ctx("timechaos")
  private val jvmChaosContext     = ctx("jvmchaos")
  private val kernelChaosContext  = ctx("kernelchaos")
  private val awsChaosContext     = ctx("awschaos")
  private val gcpChaosContext     = ctx("gpcchaos")
  private val httpChaosContext    = ctx("httpchaos")

}
