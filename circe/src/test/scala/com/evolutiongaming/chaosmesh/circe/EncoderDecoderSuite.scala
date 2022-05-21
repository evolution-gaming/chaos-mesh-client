package com.evolutiongaming.chaosmesh.circe

import cats.effect.IO
import cats.effect.kernel.Sync
import cats.syntax.all._
import com.evolutiongaming.chaosmesh.circe.common.DurationInstances
import com.evolutiongaming.chaosmesh.circe.instances._
import com.evolutiongaming.chaosmesh.model.dnschaos.DnsChaos
import com.evolutiongaming.chaosmesh.model.httpchaos.HttpChaos
import com.evolutiongaming.chaosmesh.model.iochaos.IoChaos
import com.evolutiongaming.chaosmesh.model.jvmchaos.JvmChaos
import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.networkchaos.NetChaos
import com.evolutiongaming.chaosmesh.model.podchaos.PodChaos
import com.evolutiongaming.chaosmesh.model.spec._
import com.evolutiongaming.chaosmesh.model.timechaos.TimeChaos
import io.circe._
import io.circe.syntax._
import weaver._

import scala.concurrent.duration._
import com.evolutiongaming.chaosmesh.model.stresschaos.StressChaos

/**
  * Example test files are based on https://github.com/chaos-mesh/chaos-mesh/tree/master/examples
  */
object EncoderDecoderSuite extends SimpleIOSuite with DurationInstances {

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
    test.handleError(err => failure(s"exception happened on decoding $err"))
  }
  import com.evolutiongaming.chaosmesh.circe.common.CirceOps._
  private def testEncoding[Spec: Encoder](
    filename: String,
    toEncode: CustomResource[Spec, ExperimentKind],
  ): IO[Expectations] =
    for {
      fileJson <- getJsonContent(s"/$filename")
      encoded        = toEncode.asJson
      encodedJsonStr = encoded.printWith(testPrinter)
      fileJsonStr    = fileJson.printWith(testPrinter)
    } yield expect.eql(encodedJsonStr, fileJsonStr)

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

  test("cpu stress") {
    val experiment =
      StressChaos(
        metadata = ResourceMetadata(
          name = "burn-cpu",
        ),
        spec = StressChaos
          .Spec(
            mode = Mode.One,
            selector = Selectors()
              .withByLabels("app.kubernetes.io/component" -> "tikv"),
            duration = 30.seconds,
            stressors = StressChaos
              .Stressors()
              .withCpuStress(
                StressChaos.CpuStressor(1, 100).withOptions("--cpu 2", "--timeout 600", "--hdd 1"),
              ),
          ),
      )
    testEncodingDecoding[StressChaos.Spec, StressChaos](
      "burn-cpu.yaml",
      experiment,
    )
  }

  test("memory stress") {
    val experiment =
      StressChaos(
        metadata = ResourceMetadata(
          name = "pod-oom",
        ),
        spec = StressChaos
          .Spec(
            mode = Mode.One,
            selector = Selectors()
              .withByLabels("app.kubernetes.io/component" -> "tikv"),
            duration = 30.seconds,
            stressors = StressChaos
              .Stressors()
              .withMemoryStress(
                StressChaos
                  .MemoryStressor(1)
                  .withOccupiedSize("10GB")
                  .withOomScoreAdj(-1000),
              ),
          ),
      )
    testEncodingDecoding[StressChaos.Spec, StressChaos](
      "cause-pod-oom.yaml",
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

  test("io attr override") {
    val experiment =
      IoChaos(
        metadata = ResourceMetadata(
          name = "io-attr-example",
        ),
        spec = IoChaos
          .Spec(
            action = Action.IoChaos.AttrOverride().withPermission(72),
            mode = Mode.One,
            selector = Selectors()
              .withByLabels("app" -> "etcd"),
            duration = 400.seconds,
            volumePath = "/var/run/etcd",
          )
          .withPath("/var/run/etcd/**/*")
          .withProbability(10),
      )
    testEncodingDecoding[IoChaos.Spec, IoChaos](
      "io-attr.yaml",
      experiment,
    )
  }

  test("io mistake override") {
    val experiment =
      IoChaos(
        metadata = ResourceMetadata(
          name = "io-mistake-example",
          namespace = "chaos-testing".some,
        ),
        spec = IoChaos
          .Spec(
            action = Action.IoChaos.Mistake(
              filling = Action.IoChaos.MistakeFillings.Zeros,
              maxOccurrences = 1,
              maxLength = 10,
            ),
            mode = Mode.One,
            selector = Selectors()
              .withByLabels("app" -> "etcd"),
            duration = 400.seconds,
            volumePath = "/var/run/etcd",
          )
          .withPath("/var/run/etcd/**/*")
          .withMethods("READ", "WRITE")
          .withProbability(10),
      )
    testEncodingDecoding[IoChaos.Spec, IoChaos](
      "io-mistake.yaml",
      experiment,
    )
  }

  test("dns chaos") {
    val experiment =
      DnsChaos(
        metadata = ResourceMetadata(
          name = "dns-chaos-example",
        ),
        spec = DnsChaos
          .Spec(
            action = Action.DnsChaos.Random,
            mode = Mode.All,
            selector = Selectors()
              .withByNamespaces("busybox"),
            duration = 50.seconds,
          )
          .withTargetDomains("google.com", "chaos-mesh.*", "github.?om"),
      )
    testEncodingDecoding[DnsChaos.Spec, DnsChaos](
      "dns-chaos.yaml",
      experiment,
    )
  }

  test("time chaos simple") {
    val experiment =
      TimeChaos(
        metadata = ResourceMetadata(
          name = "time-shift-example",
        ),
        spec = TimeChaos
          .Spec(
            mode = Mode.One,
            selector = Selectors()
              .withByLabels("app.kubernetes.io/component" -> "tikv"),
            timeOffset = 10.minutes,
            duration = 30.seconds,
          ),
      )
    testEncodingDecoding[TimeChaos.Spec, TimeChaos](
      "time-chaos.yaml",
      experiment,
    )
  }

  test("time chaos decode complex time offset") {
    for {
      fileJson <- getJsonContent("/time-chaos-complex-time.yaml")
      parsed   <- IO.fromEither(fileJson.as[TimeChaos])
      time = parsed.spec.timeOffset
    } yield expect(time == -600000000100L.nanos)
  }

  test("jvm exception") {
    val experiment =
      JvmChaos(
        metadata = ResourceMetadata(
          name = "exception",
        ),
        spec = JvmChaos
          .Spec(
            action = Action.JvmChaos.Exception(
              `class` = "Main",
              method = "sayhello",
              exception = "java.io.IOException(\"BOOM\")",
            ),
            mode = Mode.All,
            selector = Selectors()
              .withByNamespaces("helloworld"),
          ),
      )
    testEncodingDecoding[JvmChaos.Spec, JvmChaos](
      "jvm-exception.yaml",
      experiment,
    )
  }

  test("jvm return") {
    val experiment =
      JvmChaos(
        metadata = ResourceMetadata(
          name = "return",
        ),
        spec = JvmChaos
          .Spec(
            action = Action.JvmChaos.Return(
              `class` = "Main",
              method = "getnum",
              value = "9999",
            ),
            mode = Mode.All,
            selector = Selectors()
              .withByNamespaces("helloworld"),
          ),
      )
    testEncodingDecoding[JvmChaos.Spec, JvmChaos](
      "jvm-return.yaml",
      experiment,
    )
  }

  test("jvm rule data") {
    val experiment =
      JvmChaos(
        metadata = ResourceMetadata(
          name = "modify-return",
        ),
        spec = JvmChaos
          .Spec(
            action = Action.JvmChaos.RuleData(
              ruleData =
                "RULE modify return value\nCLASS Main\nMETHOD getnum\nAT ENTRY\nIF true\nDO\n    return 9999\nENDRULE",
            ),
            mode = Mode.All,
            selector = Selectors()
              .withByNamespaces("helloworld"),
          ),
      )
    testEncodingDecoding[JvmChaos.Spec, JvmChaos](
      "jvm-rule-data.yaml",
      experiment,
    )
  }

  test("http failure") {
    val experiment =
      HttpChaos(
        metadata = ResourceMetadata(
          name = "test-http-chaos",
        ),
        spec = HttpChaos
          .Spec(
            mode = Mode.All,
            selector = Selectors()
              .withByLabels("app" -> "nginx"),
            target = HttpChaos.Target
              .Request()
              .withReplacedMethod("DELETE")
              .withReplacedPath("/api/v2/"),
            port = 80,
            duration = 5.minutes,
          )
          .withDelay(10.seconds)
          .matchingMethod("GET")
          .withPatchBodyType("JSON")
          .withPatchBodyContent("{\"foo\": \"bar\"}")
          .withPatchHeaders("Token" -> "<one token>", "Token" -> "<another token>")
          .matchingPath("/api/*"),
      )
    testEncodingDecoding[HttpChaos.Spec, HttpChaos](
      "http-failure.yaml",
      experiment,
    )
  }

  test("http abort") {
    val experiment =
      HttpChaos(
        metadata = ResourceMetadata(
          name = "test-http-chaos",
        ),
        spec = HttpChaos
          .Spec(
            mode = Mode.All,
            selector = Selectors()
              .withByLabels("app" -> "nginx"),
            target = HttpChaos.Target
              .Request(),
            port = 80,
            duration = 5.minutes,
          )
          .abortRequest(true)
          .matchingMethod("GET")
          .matchingPath("/api"),
      )
    testEncodingDecoding[HttpChaos.Spec, HttpChaos](
      "http-abort-failure.yaml",
      experiment,
    )
  }

}
