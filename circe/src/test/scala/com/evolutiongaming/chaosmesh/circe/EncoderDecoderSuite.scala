package com.evolutiongaming.chaosmesh.circe

import cats.effect.IO
import cats.effect.kernel.Sync
import cats.syntax.all._
import com.evolutiongaming.chaosmesh.circe.instances._
import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.podchaos.PodChaos
import com.evolutiongaming.chaosmesh.model.spec._
import io.circe._
import io.circe.syntax._
import weaver._

import scala.concurrent.duration._
import com.evolutiongaming.chaosmesh.model.networkchaos.NetChaos
import com.evolutiongaming.chaosmesh.model.httpchaos.HttpChaos
import com.evolutiongaming.chaosmesh.model.iochaos.IoChaos

object EncoderDecoderSuite extends SimpleIOSuite {

  val testPrinter = Printer.noSpacesSortKeys.copy(dropNullValues = true)

  private def getJsonContent(name: String): IO[Json] = Sync[IO].fromEither {
    io.circe.yaml.parser.parse(scala.io.Source.fromURL(getClass.getResource(name)).mkString)
  }

  private def testDecoding[Spec, Resource <: CustomResource[Spec, ExperimentKind]: Decoder](
    filename: String,
    expected: Resource,
  ): IO[Expectations] = {
    val test = for {
      fileJson <- getJsonContent(s"/$filename")
      parsed   <- IO.fromEither(fileJson.as[Resource])
    } yield expect(parsed == expected)
    test.handleError(_ => failure("exception happened on decoding"))
  }

  private def testEncoding[Spec: Encoder](
    filename: String,
    toEncode: CustomResource[Spec, ExperimentKind],
  ): IO[Expectations] =
    for {
      fileJson <- getJsonContent(s"/$filename")
      encoded        = toEncode.asJson
      encodedJsonStr = encoded.printWith(testPrinter)
      fileJsonStr    = fileJson.printWith(testPrinter)
    } yield expect(encodedJsonStr == fileJsonStr)

  private def testEncodingDecoding[Spec, Resource <: CustomResource[Spec, ExperimentKind]](
    filename:     String,
    resource:     Resource,
  )(implicit dec: Decoder[Resource], enc: Encoder[Spec]): IO[Expectations] =
    for {
      decoding <- testDecoding[Spec, Resource](
        filename = filename,
        expected = resource,
      )
      encoding <- testEncoding(
        filename = filename,
        toEncode = resource,
      )
    } yield encoding && decoding

  test("pod failure") {
    val podFailure =
      PodChaos(
        metadata = ResourceMetadata(
          name = "pod-failure-example",
          namespace = "chaos-testing".some,
        ),
        spec = PodChaos.Spec(
          action = Action.PodChaos.PodFailure,
          mode = Mode.One,
          duration = 30.seconds,
          selector = Selectors().withByLabels("app.kubernetes.io/component" -> "tikv"),
        ),
      )
    testEncodingDecoding[PodChaos.Spec, PodChaos]("pod-failure.yaml", podFailure)
  }

  test("pod kill") {
    val podKill =
      PodChaos(
        metadata = ResourceMetadata(
          name = "pod-kill-example",
        ),
        spec = PodChaos.Spec(
          action = Action.PodChaos.PodKill(550.milli),
          mode = Mode.Fixed(5),
          duration = 30.seconds,
          selector = Selectors().withByLabels("app.kubernetes.io/component" -> "tikv"),
        ),
      )
    testEncodingDecoding[PodChaos.Spec, PodChaos]("pod-kill.yaml", podKill)
  }

  test("container kill") {
    val containerKill =
      PodChaos(
        metadata = ResourceMetadata(
          name = "container-kill-example",
        ),
        spec = PodChaos.Spec(
          action = Action.PodChaos.ContainerKill("prometheus", "loki"),
          mode = Mode.FixedPercent(50),
          duration = 30.seconds,
          selector = Selectors().withByLabels("app.kubernetes.io/component" -> "monitor"),
        ),
      )
    testEncodingDecoding[PodChaos.Spec, PodChaos]("container-kill.yaml", containerKill)
  }

  test("net partition") {
    val experiment =
      NetChaos(
        metadata = ResourceMetadata(
          name = "network-partition-example",
        ),
        spec = NetChaos.Spec(
          action = Action.NetChaos.NetPartition,
          direction = Direction
            .To()
            .withTarget(
              Direction.Target(
                mode = Mode.One,
                selector = Selectors().withByAnnotations("traffic" -> "input"),
              ),
            )
            .withExternalTargets(
              "8.8.8.8",
              "www.google.com",
              "8.8.0.0/16",
            )
            .some,
          mode = Mode.RandomMaxPercent(100),
          duration = 10.seconds,
          selector = Selectors().withByLabels("traffic" -> "output"),
        ),
      )
    testEncodingDecoding[NetChaos.Spec, NetChaos](
      "network-partition-with-external-targets.yaml",
      experiment,
    )
  }

  test("net bandwidth") {
    val experiment =
      NetChaos(
        metadata = ResourceMetadata(
          name = "network-bandwidth-example",
        ),
        spec = NetChaos.Spec(
          action = Action.NetChaos
            .BandwidthLimit(
              rate = 100000,
              limit = 100,
              buffer = 10000,
            )
            .withPeakRate(1000000)
            .withPeakRateBucketSize(1000000),
          mode = Mode.One,
          duration = 10.seconds,
          selector = Selectors()
            .withByLabels("app.kubernetes.io/component" -> "tikv"),
        ),
      )
    testEncodingDecoding[NetChaos.Spec, NetChaos](
      "network-bandwidth.yaml",
      experiment,
    )
  }

  test("net loss") {
    val experiment =
      NetChaos(
        metadata = ResourceMetadata(
          name = "network-bandwidth-example",
        ),
        spec = NetChaos.Spec(
          action = Action.NetChaos
            .BandwidthLimit(
              rate = 100000,
              limit = 100,
              buffer = 10000,
            )
            .withPeakRate(1000000)
            .withPeakRateBucketSize(1000000),
          mode = Mode.One,
          duration = 10.seconds,
          selector = Selectors()
            .withByLabels("app.kubernetes.io/component" -> "tikv"),
        ),
      )
    testEncodingDecoding[NetChaos.Spec, NetChaos](
      "network-bandwidth.yaml",
      experiment,
    )
  }

  test("net corrupt") {
    val experiment =
      NetChaos(
        metadata = ResourceMetadata(
          name = "network-corrupt-example",
        ),
        spec = NetChaos.Spec(
          action = Action.NetChaos
            .PacketCorrupt()
            .withCorrelation(25)
            .withProbability(40),
          mode = Mode.One,
          duration = 10.seconds,
          selector = Selectors()
            .withByLabels("app.kubernetes.io/component" -> "tikv"),
        ),
      )
    testEncodingDecoding[NetChaos.Spec, NetChaos](
      "network-corrupt.yaml",
      experiment,
    )
  }

  test("net loss") {
    val experiment =
      NetChaos(
        metadata = ResourceMetadata(
          name = "network-loss-example",
        ),
        spec = NetChaos.Spec(
          action = Action.NetChaos
            .PacketLoss()
            .withCorrelation(25)
            .withProbability(25),
          mode = Mode.One,
          duration = 10.seconds,
          selector = Selectors()
            .withByLabels("app.kubernetes.io/component" -> "tikv"),
        ),
      )
    testEncodingDecoding[NetChaos.Spec, NetChaos](
      "network-loss.yaml",
      experiment,
    )
  }

  test("net duplication") {
    val experiment =
      NetChaos(
        metadata = ResourceMetadata(
          name = "network-duplicate-example",
        ),
        spec = NetChaos.Spec(
          action = Action.NetChaos
            .PacketDuplicate()
            .withCorrelation(25)
            .withProbability(40),
          mode = Mode.One,
          duration = 10.seconds,
          selector = Selectors()
            .withByLabels("app.kubernetes.io/component" -> "tikv"),
        ),
      )
    testEncodingDecoding[NetChaos.Spec, NetChaos](
      "network-duplicate.yaml",
      experiment,
    )
  }

  test("io errno") {
    val experiment =
      IoChaos(
        metadata = ResourceMetadata(
          name = "io-errno-example",
        ),
        spec = IoChaos
          .Spec(
            action = Action.IoChaos.Fault(5),
            mode = Mode.One,
            selector = Selectors()
              .withByLabels("app" -> "etcd"),
            duration = 400.seconds,
            volumePath = "/var/run/etcd",
          )
          .withPath("/var/run/etcd/**/*")
          .withProbability(50),
      )
    testEncodingDecoding[IoChaos.Spec, IoChaos](
      "io-errno.yaml",
      experiment,
    )
  }

  test("io latency") {
    val experiment =
      IoChaos(
        metadata = ResourceMetadata(
          name = "io-delay-example",
        ),
        spec = IoChaos
          .Spec(
            action = Action.IoChaos.Latency(10.milli),
            mode = Mode.One,
            selector = Selectors()
              .withByLabels("app" -> "etcd"),
            duration = 400.seconds,
            volumePath = "/var/run/etcd",
          )
          .withPath("/var/run/etcd/**/*")
          .withProbability(10)
          .withTargetContainer("etcd"),
      )
    testEncodingDecoding[IoChaos.Spec, IoChaos](
      "io-delay.yaml",
      experiment,
    )
  }

}
