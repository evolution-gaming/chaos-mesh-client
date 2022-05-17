import sbt._

object Dependencies {
  object Compiler {
    val KindProjector    = "org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full
    val BetterMonadicFor = "com.olegpy"    %% "better-monadic-for" % "0.3.1"
  }

  object K8s {
    private val version  = "0.8.0"
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
  }

  object Testing {
    val WeaverCats = "com.disneystreaming" %% "weaver-cats" % "0.7.11" % Test
  }

}
