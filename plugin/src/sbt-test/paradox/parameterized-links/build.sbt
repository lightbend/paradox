val akkaVersion = "2.4.10"

enablePlugins(ParadoxPlugin)
paradoxTheme := None
paradoxProperties in Compile ++= Map(
  "extref.rfc.base_url" -> "http://tools.ietf.org/html/rfc%s",
  "extref.akka-docs.base_url" -> s"http://doc.akka.io/docs/akka/$akkaVersion/%s.html"
)
