import sbt._

object Dependencies {

  object K8s {
    private val version  = "0.8.1-2-0d89a0c-SNAPSHOT"
    val KubernetesClient = "com.goyeau" %% "kubernetes-client" % version
  }

  object Cats {
    private val version = "2.7.0"
    val Core            = "org.typelevel" %% "cats-core" % version
  }

  object Circe {
    private val version = "0.14.1"
    val Core            = "io.circe" %% "circe-core"    % version
    val Generic         = "io.circe" %% "circe-generic" % version
    val Parser          = "io.circe" %% "circe-parser"  % version
    val Yaml            = "io.circe" %% "circe-yaml"    % version
  }

  object Testing {
    val WeaverCats = "com.disneystreaming" %% "weaver-cats" % "0.7.11" % Test
  }

}
