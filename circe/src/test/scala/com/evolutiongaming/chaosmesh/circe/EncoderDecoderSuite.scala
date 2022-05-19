package com.evolutiongaming.chaosmesh.circe

import cats.effect.IO
import cats.effect.kernel.Sync
import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.podchaos.PodChaos
import com.evolutiongaming.chaosmesh.model.spec._
import com.evolutiongaming.chaosmesh.circe.instances._
import io.circe._
import io.circe.syntax._
import weaver._

import scala.concurrent.duration._

object EncoderDecoderSuite extends SimpleIOSuite {

  def getJsonContent(name: String): IO[Json] = Sync[IO].fromEither {
    io.circe.yaml.parser.parse(scala.io.Source.fromURL(getClass.getResource(name)).mkString)
  }

  val testPrinter = Printer.noSpacesSortKeys.copy(dropNullValues = true)

  test("test pod failure decode") {
    for {
      fileJson <- getJsonContent("/pod-failure.yaml")
      expected = PodChaos(
        metadata = ResourceMetadata(
          name = "pod-failure-example",
          namespace = "chaos-testing",
        ),
        spec = PodChaos.Spec(
          action = Action.PodChaos.PodFailure,
          mode = Mode.One,
          duration = 30.seconds,
          selector = Selectors().withByLabels("app.kubernetes.io/component" -> "tikv"),
        ),
      )
      podChaos <- IO.fromEither(fileJson.as[PodChaos])
    } yield expect(podChaos == expected)
  }

  test("test pod failure encode") {
    val podKill: CustomResource[PodChaos.Spec, ExperimentKind] =
      PodChaos(
        metadata = ResourceMetadata(
          name = "pod-failure-example",
          namespace = "chaos-testing",
        ),
        spec = PodChaos.Spec(
          action = Action.PodChaos.PodFailure,
          mode = Mode.One,
          duration = 30.seconds,
          selector = Selectors().withByLabels("app.kubernetes.io/component" -> "tikv"),
        ),
      )
    for {
      fileJson <- getJsonContent("/pod-failure.yaml")
      encoded = podKill.asJson
    } yield expect(encoded.printWith(testPrinter) == fileJson.printWith(testPrinter))
  }
}
