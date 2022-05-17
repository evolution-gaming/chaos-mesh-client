import Dependencies._

inThisBuild(
  Seq(
    homepage             := Some(new URL("https://github.com/evolution-gaming/chaos-mesh-client")),
    organization         := "com.evolutiongaming",
    organizationName     := "Evolution",
    organizationHomepage := Some(url("http://evolution.com")),
    startYear            := Some(2022),
    licenses             := Seq(("MIT", url("https://opensource.org/licenses/MIT"))),
    crossScalaVersions   := Seq("3.1.1", "2.13.8", "2.12.15"),
    versionScheme        := Some("semver-spec"),
    scalaVersion         := crossScalaVersions.value.head,
    publishTo            := Some(Resolver.evolutionReleases),
    addCompilerPlugin(Compiler.KindProjector),
    addCompilerPlugin(Compiler.BetterMonadicFor),
  ),
)

lazy val commonSettings = Seq(
  releaseCrossBuild    := true,
  scalacOptsFailOnWarn := Some(false),
  scalacOptions += "-Wconf:cat=other-match-analysis:error",
  testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
  libraryDependencies ++= Seq(
    Testing.WeaverCats,
  ),
)

lazy val root = project
  .in(file("."))
  .settings(
    commonSettings,
    name            := "chaos-mesh-client",
    publish / skip  := true,
    publishArtifact := false,
  )
  .aggregate(
    domain,
    circe,
    `kubernetes-client`,
  )

lazy val domain = project
  .settings(
    commonSettings,
    name := "chaos-mesh-client-domain",
    libraryDependencies ++= Seq(
      Cats.Core,
    ),
  )

lazy val circe = project
  .dependsOn(domain)
  .settings(
    commonSettings,
    name := "chaos-mesh-client-circe",
    libraryDependencies ++= Seq(
      Circe.Core,
      Circe.Generic,
    ),
  )

lazy val `kubernetes-client` = project
  .dependsOn(
    domain,
    circe,
  )
  .settings(
    commonSettings,
    name := "chaos-mesh-client-kubernetes-client",
    libraryDependencies ++= Seq(
      K8s.KubernetesClient,
    ),
  )
