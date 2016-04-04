/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

lazy val paradox = project
  .in(file("."))
  .aggregate(core, plugin, themes)
  .enablePlugins(NoPublish)

lazy val core = project
  .in(file("core"))
  .settings(
    name := "paradox",
    libraryDependencies ++= Seq(
      Library.pegdown,
      Library.st4,
      Library.scalatest % "test",
      Library.jtidy % "test"
    )
  )

lazy val plugin = project
  .in(file("plugin"))
  .dependsOn(core)
  .settings(
    name := "sbt-paradox",
    sbtPlugin := true,
    addSbtPlugin(Library.sbtWeb),
    scriptedSettings,
    scriptedLaunchOpts += ("-Dproject.version=" + version.value),
    scriptedDependencies := {
      (publishLocal in core).value
      publishLocal.value
    },
    test in Test := {
      (test in Test).value
      scripted.toTask("").value
    },
    resourceGenerators in Compile += Def.task {
      val file = (resourceManaged in Compile).value / "paradox.version.properties"
      IO.write(file, s"paradox.version=${version.value}")
      Seq(file)
    }.taskValue
  )

lazy val themes = project
  .in(file("themes"))
  .aggregate(lightbendTheme)
  .enablePlugins(NoPublish)

lazy val lightbendTheme = project
  .in(file("themes/lightbend"))
  .enablePlugins(SbtWeb)
  .settings(
    name := "paradox-theme-lightbend",
    crossPaths := false,
    libraryDependencies ++= Seq(
      Library.foundation,
      Library.prettify
    )
  )
