val akkaVersion = "2.4.10"
val akkaHttpVersion = "10.0.0"

version := "0.1.0"
scalaVersion := "2.11.8"

enablePlugins(ParadoxPlugin)
paradoxTheme := None
paradoxProperties in Compile ++= Map(
  "extref.rfc.base_url" -> "http://tools.ietf.org/html/rfc%s",
  "extref.akka-docs.base_url" -> s"http://doc.akka.io/docs/akka/$akkaVersion/%s.html",
  "scaladoc.akka.base_url" -> s"http://doc.akka.io/api/akka/$akkaVersion",
  "scaladoc.akka.http.base_url" -> s"http://doc.akka.io/api/akka-http/$akkaHttpVersion"
)

apiURL := Some(url(s"https://example.org/api/${version.value}"))
scmInfo := Some(ScmInfo(url("https://github.com/lightbend/paradox"), "git@github.com:lightbend/paradox.git"))
