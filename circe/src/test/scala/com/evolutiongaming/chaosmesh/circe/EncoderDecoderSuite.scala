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

object EncoderDecoderSuite extends SimpleIOSuite {

  val testPrinter = Printer.noSpacesSortKeys.copy(dropNullValues = true)

  private def getJsonContent(name: String): IO[Json] = Sync[IO].fromEither {
    io.circe.yaml.parser.parse(scala.io.Source.fromURL(getClass.getResource(name)).mkString)
  }

  private def testDecoding[Spec, Resource <: CustomResource[Spec, ExperimentKind]: Decoder](
    filename: String,
    expected: Resource,
  ): IO[Expectations] =
    for {
      fileJson <- getJsonContent(s"/$filename")
      parsed   <- IO.fromEither(fileJson.as[Resource])
    } yield expect(parsed == expected)

  private def testEncoding[Spec: Encoder](
    filename: String,
    toEncode: CustomResource[Spec, ExperimentKind],
  ): IO[Expectations] =
    for {
      fileJson <- getJsonContent(s"/$filename")
      encoded = toEncode.asJson
      encodedJsonStr = encoded.printWith(testPrinter)
      fileJsonStr = fileJson.printWith(testPrinter)
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
    } yield decoding && encoding

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

}
