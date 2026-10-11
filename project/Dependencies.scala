import sbt._

object Dependencies {

  object K8s {
    private val version = "0.11.1"
    val KubernetesClient = "com.goyeau" %% "kubernetes-client" % version
  }

  object Http4s {
    private val version = "0.23.38"
    val Client = "org.http4s" %% "http4s-client" % version
  }

  object BouncyCastle {
    private val version = "1.86"
    val Prov = "org.bouncycastle" % "bcprov-jdk18on" % version
    val Pkix = "org.bouncycastle" % "bcpkix-jdk18on" % version
  }

  object Jawn {
    val Parser = "org.typelevel" %% "jawn-parser" % "1.8.0"
  }

  object Cats {
    private val version = "2.13.0"
    val Core = "org.typelevel" %% "cats-core" % version
  }

  object Circe {
    private val version = "0.14.17"
    val Core = "io.circe" %% "circe-core" % version
    val Generic = "io.circe" %% "circe-generic" % version
    val Parser = "io.circe" %% "circe-parser" % version
    val Yaml = "io.circe" %% "circe-yaml" % "0.15.3" // should be compatible with k8s client
  }

  object Testing {
    val WeaverCats = "org.typelevel" %% "weaver-cats" % "0.13.0" % Test
  }

}
