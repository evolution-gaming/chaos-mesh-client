import Dependencies._

inThisBuild(
  Seq(
    homepage             := Some(new URL("https://github.com/evolution-gaming/chaos-mesh-client")),
    organization         := "com.evolutiongaming",
    organizationName     := "Evolution",
    organizationHomepage := Some(url("http://evolution.com")),
    startYear            := Some(2022),
    licenses             := Seq(("MIT", url("https://opensource.org/licenses/MIT"))),
    crossScalaVersions   := Seq("2.12.15", "2.13.8", "3.2.0"),
    versionScheme        := Some("semver-spec"),
    scalaVersion         := crossScalaVersions.value.head,
    publishTo            := Some(Resolver.evolutionReleases),
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq.empty
        case _ =>
          Seq(
            "-Wconf:cat=other-match-analysis:error",
          )
      }
    },
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
    ),
  ),
)

lazy val commonSettings = Seq(
  releaseCrossBuild    := true,
  scalacOptsFailOnWarn := Some(false),
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
      Circe.Parser % Test,
      Circe.Yaml   % Test,
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
