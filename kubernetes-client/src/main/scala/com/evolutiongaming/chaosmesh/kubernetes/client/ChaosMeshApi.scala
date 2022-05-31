package com.evolutiongaming.chaosmesh.kubernetes

import com.evolutiongaming.chaosmesh.circe.instances._
import com.evolutiongaming.chaosmesh.model._
import com.evolutiongaming.chaosmesh.model.k8s.Api
import com.goyeau.kubernetes.client.KubernetesClient
import com.goyeau.kubernetes.client.crd.CrdContext

import ChaosMeshApi._

class ChaosMeshApi[F[_]](client: KubernetesClient[F]) {

  lazy val pod =
    client.customResources[podchaos.PodChaos.Spec, Unit](podChaosContext)

  lazy val network =
    client.customResources[networkchaos.NetChaos.Spec, Unit](networkChaosContext)

  lazy val stress =
    client.customResources[stresschaos.StressChaos.Spec, Unit](stressChaosContext)

  lazy val io =
    client.customResources[iochaos.IoChaos.Spec, Unit](ioChaosContext)

  lazy val dns =
    client.customResources[dnschaos.DnsChaos.Spec, Unit](dnsChaosContext)

  lazy val time =
    client.customResources[timechaos.TimeChaos.Spec, Unit](timeChaosContext)

  lazy val jvm =
    client.customResources[jvmchaos.JvmChaos.Spec, Unit](jvmChaosContext)

  lazy val kernel =
    client.customResources[kernelchaos.KernelChaos.Spec, Unit](kernelChaosContext)

  lazy val aws =
    client.customResources[awschaos.AwsChaos.Spec, Unit](awsChaosContext)

  lazy val gcp =
    client.customResources[gcpchaos.GcpChaos.Spec, Unit](gcpChaosContext)

  lazy val http =
    client.customResources[httpchaos.HttpChaos.Spec, Unit](httpChaosContext)

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
