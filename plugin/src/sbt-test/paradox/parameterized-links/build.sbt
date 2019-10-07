val akkaVersion = "2.4.10"
val akkaHttpVersion = "10.0.0"

version := "0.1.0"
scalaVersion := "2.11.12"

enablePlugins(ParadoxPlugin)
paradoxTheme := None
paradoxProperties in Compile ++= Map(
  "extref.rfc.base_url" -> "http://tools.ietf.org/html/rfc%s",
  "extref.akka-docs.base_url" -> s"http://doc.akka.io/docs/akka/$akkaVersion/%s.html",
  "scaladoc.akka.base_url" -> s"http://doc.akka.io/api/akka/$akkaVersion",
  "scaladoc.akka.http.base_url" -> s"http://doc.akka.io/api/akka-http/$akkaHttpVersion",
  "javadoc.link_style" -> "frames",
  "javadoc.base_url" -> s"https://api.example.com/java",
  "javadoc.akka.base_url" -> s"http://doc.akka.io/japi/akka/$akkaVersion",
  "javadoc.akka.http.base_url" -> s"http://doc.akka.io/japi/akka-http/$akkaHttpVersion"
)
paradoxRoots := List(
  "extref.html",
  "github.html",
  "javadoc.html",
  "javadoc-javalib.html",
  "scaladoc.html",
)

apiURL := Some(url(s"https://example.org/api/${version.value}"))
scmInfo := Some(ScmInfo(url("https://github.com/lightbend/paradox"), "git@github.com:lightbend/paradox.git"))

TaskKey[Unit]("checkJavadocJavalibContent") := {
  val file = (target in (Compile, paradox)).value / "javadoc-javalib.html"

  assert(file.exists, s"${file.getAbsolutePath} did not exist")
  val content = IO.readLines(file).mkString
  assert(content.matches(
    raw"""<p><a href="https://docs.oracle.com/javase/\d+/docs/api/\?java/io/File\.html#separator" title="java.io.File"><code>File\.separator</code></a></p>"""))
}
