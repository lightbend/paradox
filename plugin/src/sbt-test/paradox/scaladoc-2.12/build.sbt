val akkaVersion = "2.4.13"
val akkaHttpVersion = "10.0.0"

version := "0.1.0"
scalaVersion := "2.12.1"

enablePlugins(ParadoxPlugin)
paradoxTheme := None
paradoxProperties in Compile ++= Map(
  "scaladoc.akka.base_url" -> s"http://doc.akka.io/api/akka/$akkaVersion",
  "scaladoc.akka.http.base_url" -> s"http://doc.akka.io/api/akka-http/$akkaHttpVersion"
)
paradoxRoots := List("scaladoc-2.12.html")
