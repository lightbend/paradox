/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

lazy val paradox = project
  .in(file("."))
  .aggregate(core, plugin)
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
    }
  )
