/*
 * Copyright © 2015 - 2016 Lightbend, Inc. <http://www.lightbend.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

lazy val paradox = project
  .in(file("."))
  .aggregate(core, plugin, themePlugin, themes)
  .enablePlugins(NoPublish)
  .settings(inThisBuild(List(
    organization := "com.lightbend.paradox",
    licenses += "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"),
    scalaVersion := "2.10.6",
    organizationName := "lightbend",
    organizationHomepage := Some(url("http://lightbend.com/")),
    homepage := Some(url("https://github.com/lightbend/paradox")),
    scmInfo := Some(ScmInfo(url("https://github.com/lightbend/paradox"), "git@github.com:lightbend/paradox.git")),
    developers := List(
      Developer("pvlugter", "Peter Vlugter", "@pvlugter", url("https://github.com/pvlugter")),
      Developer("eed3si9n", "Eugene Yokota", "@eed3si9n", url("https://github.com/eed3si9n"))
    ),
    description := "Paradox is a markdown documentation tool for software projects."
  )))

lazy val core = project
  .in(file("core"))
  .disablePlugins(BintrayPlugin)
  .enablePlugins(SonatypePublish)
  .settings(
    name := "paradox",
    libraryDependencies ++= Seq(
      Library.pegdown,
      Library.st4,
      Library.scalatest % "test",
      Library.jtidy % "test"
    ),
    parallelExecution in Test := false
  )

lazy val plugin = project
  .in(file("plugin"))
  .dependsOn(core)
  .enablePlugins(BintrayPublish)
  .settings(
    name := "sbt-paradox",
    sbtPlugin := true,
    addSbtPlugin(Library.sbtWeb),
    scriptedSettings,
    scriptedLaunchOpts += ("-Dproject.version=" + version.value),
    scriptedDependencies := {
      val p1 = (publishLocal in core).value
      val p2 = publishLocal.value
      val p3 = (publishLocal in genericTheme).value
    },
    test in Test := {
      (test in Test).value
      scripted.toTask("").value
    },
    resourceGenerators in Compile += Def.task {
      val file = (resourceManaged in Compile).value / "paradox.properties"
      IO.write(file,
        s"""|paradox.organization=${organization.value}
            |paradox.version=${version.value}
            |""".stripMargin)
      Seq(file)
    }.taskValue
  )

lazy val themePlugin = (project in file("theme-plugin"))
  .enablePlugins(BintrayPublish)
  .settings(
    name := "sbt-paradox-theme",
    sbtPlugin := true,
    addSbtPlugin(Library.sbtWeb)
  )

lazy val themes = (project in file("themes"))
  .aggregate(genericTheme)
  .enablePlugins(NoPublish)

lazy val genericTheme = (project in (file("themes") / "generic"))
  .disablePlugins(BintrayPlugin)
  .enablePlugins(ParadoxThemePlugin, SonatypePublish)
  .settings(
    name := "paradox-theme-generic",
    libraryDependencies ++= Seq(
      Library.foundation % "provided",
      Library.prettify % "provided"
    )
  )
