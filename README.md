# Scala Chaos Mesh client

Compatible with Scala 2.x and Scala 3.x versions

## Domain

Contains all [Chaos Mesh](https://github.com/chaos-mesh/chaos-mesh) experiment types described in Scala code.

### Installation

[SBT](https://www.scala-sbt.org):
```scala
"com.evolutiongaming" %% "chaos-mesh-client-domain" % "<latest version>"
```

## Circe

Contains [Circe](https://github.com/circe/circe) Encoders and Decoders for Domain classes into Chaos Mesh experiment definitions in JSON format.

### Installation

[SBT](https://www.scala-sbt.org):
```scala
"com.evolutiongaming" %% "chaos-mesh-client-circe" % "<latest version>"
```


## Kubernetes-client

Wrapper around [kubernetes-client](https://github.com/joan38/kubernetes-client) which provides API for all Chaos Mesh resources.
 
### Installation

[SBT](https://www.scala-sbt.org):
```scala
"com.evolutiongaming" %% "chaos-mesh-client-kubernetes-client" % "<latest version>"
```

## Usage example:

###  Create Pod Failure Experiment

```scala
import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.syntax.all._
import cats.syntax.all._
import com.evolutiongaming.chaosmesh.kubernetes._
import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.podchaos._
import com.evolutiongaming.chaosmesh.model.spec._
import com.goyeau.kubernetes.client.{KubeConfig, KubernetesClient}
import org.http4s.Status
import org.typelevel.log4cats.Logger

implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

val experiment  =
  PodChaos(
    metadata =  ResourceMetadata(
	  name      = "pod-failure-example",
	  namespace = "chaos-testing".some,
	),
	spec =  PodChaos.Spec(
	  action   = Action.PodChaos.PodFailure,
	  mode     = Mode.One,
	  duration = 30.seconds,
	  selector = Selectors().withByLabels("app.kubernetes.io/component"  ->  "tikv"),
	),
  )
 
def api: Resource[IO, ChaosMeshApi[IO]] =
  for  {
    file       <- IO(new File(s"${System.getProperty("user.home")}/.kube/config")).toResource
    config     <- KubeConfig.fromFile(file).toResource
    k8sClient <- KubernetesClient[IO](config)
  } yield ChaosMeshApi(k8sClient)

def createPodFailure: IO[Status] =
  apiResource.use { chaosMeshApi =>
    chaosMeshApi.pod
	  .namespace("chaos-testing")
	  .create(experiment.asK8sClientResource)
  }
```

###  Create Network Partition experiment
```scala
import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.syntax.all._
import cats.syntax.all._
import com.evolutiongaming.chaosmesh.kubernetes._
import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.networkchaos._
import com.evolutiongaming.chaosmesh.model.spec._
import com.goyeau.kubernetes.client.{KubeConfig, KubernetesClient}
import org.http4s.Status
import org.typelevel.log4cats.Logger

implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

val experiment  =  
  NetChaos(
    metadata = ResourceMetadata(
      name      = "net-partition",
      namespace = "chaos-testing".some,
    ),
    spec = NetChaos.Spec(
      action   =  Action.NetChaos.NetPartition,
      mode     =  Mode.One,
      duration =  30.seconds,
      selector =  Selectors().withByLabels("app"  ->  "frontend"),
    )
    .withDirection(
	  Direction.Both(
	    Direction.Target(
		  mode     =  Mode.All,
		  selector = Selectors().withByLabels("app"  ->  "backend"),
		),
	  ),
	),
  )
 
def api: Resource[IO, ChaosMeshApi[IO]] =
  for {
    file       <- IO(new File(s"${System.getProperty("user.home")}/.kube/config")).toResource
    config     <- KubeConfig.fromFile(file).toResource
    k8sClient <- KubernetesClient[IO](config)
  } yield ChaosMeshApi(k8sClient)

def createPodFailure: IO[Status] =
  apiResource.use { chaosMeshApi =>
	chaosMeshApi.network
	  .namespace("chaos-testing")
	  .create(experiment.asK8sClientResource)
  }
```
More examples of experiment definitions could be found in [TestSuite](./circe/src/test/scala/com/evolutiongaming/chaosmesh/circe/EncoderDecoderSuite.scala)