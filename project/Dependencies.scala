import sbt._

object Dependencies {

  object K8s {
    private val version  = "0.11.1"
    val KubernetesClient = "com.goyeau" %% "kubernetes-client" % version
  }

  object Cats {
    private val version = "2.13.0"
    val Core            = "org.typelevel" %% "cats-core" % version
  }

  object Circe {
    private val version = "0.14.14"
    val Core            = "io.circe" %% "circe-core"    % version
    val Generic         = "io.circe" %% "circe-generic" % version
    val Parser          = "io.circe" %% "circe-parser"  % version
    val Yaml            = "io.circe" %% "circe-yaml"    % "0.15.2" // should be compatible with k8s client
  }

  object Testing {
    val WeaverCats = "org.typelevel" %% "weaver-cats" % "0.10.1" % Test
  }

}
