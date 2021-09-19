/*
 * Copyright © 2015 - 2017 Lightbend, Inc. <http://www.lightbend.com>
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

import scala.collection.JavaConverters._
import java.lang.management.ManagementFactory

inThisBuild(List(
  organization := "com.lightbend.paradox",
  licenses += "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"),
  organizationName := "lightbend",
  organizationHomepage := Some(url("https://lightbend.com/")),
  homepage := Some(url("https://developer.lightbend.com/docs/paradox/current/")),
  scmInfo := Some(ScmInfo(url("https://github.com/lightbend/paradox"), "git@github.com:lightbend/paradox.git")),
  developers := List(
    Developer("pvlugter", "Peter Vlugter", "@pvlugter", url("https://github.com/pvlugter")),
    Developer("eed3si9n", "Eugene Yokota", "@eed3si9n", url("https://github.com/eed3si9n"))
  ),
  description := "Paradox is a markdown documentation tool for software projects.",
  dynverSonatypeSnapshots := false // not publishing snapshots, so no SNAPSHOT at the end please
))

lazy val paradox = project
  .in(file("."))
  .aggregate(core, testkit, tests, plugin, themePlugin, themes, docs)
  .settings(
    publish / skip := true
  )

lazy val core = project
  .in(file("core"))
  .settings(
    name := "paradox",
    crossScalaVersions := Seq(scalaVersion.value, "2.13.8"),
    libraryDependencies ++= Library.pegdown,
    libraryDependencies ++= Seq(
      Library.st4,
      Library.jsoup
    ),
    Test / parallelExecution := false
  )

lazy val testkit = project
  .in(file("testkit"))
  .dependsOn(core)
  .settings(
    name := "testkit",
    libraryDependencies ++= Seq(
      Library.jtidy
    )
  )

lazy val tests = project
  .in(file("tests"))
  .dependsOn(core, testkit)
  .settings(
    name := "tests",
    libraryDependencies ++= Seq(
      Library.scalatest % "test"
    ),
    publish / skip := true
  )

lazy val plugin = project
  .in(file("plugin"))
  .dependsOn(core)
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-paradox",
    sbtPlugin := true,
    addSbtPlugin(Library.sbtWeb),
    pluginCrossBuild / sbtVersion := "1.0.0", // support all sbt 1.x
    scriptedSbt := sbtVersion.value, // run scripted tests against build sbt by default
    scriptedLaunchOpts += ("-Dproject.version=" + version.value),
    scriptedLaunchOpts ++= ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.filter(
      a => Seq("-Xmx", "-Xms", "-XX", "-Dfile").exists(a.startsWith)
    ),
    scriptedDependencies := {
      val p1 = (core / publishLocal).value
      val p2 = publishLocal.value
      val p3 = (genericTheme / publishLocal).value
    },
    Compile / resourceGenerators += Def.task {
      val file = (Compile / resourceManaged).value / "paradox.properties"
      IO.write(file,
        s"""|paradox.organization=${organization.value}
            |paradox.version=${version.value}
            |""".stripMargin)
      Seq(file)
    }.taskValue
  )

lazy val themePlugin = project
  .in(file("theme-plugin"))
  .settings(
    name := "sbt-paradox-theme",
    sbtPlugin := true,
    addSbtPlugin(Library.sbtWeb)
  )

lazy val themes = project
  .in(file("themes"))
  .aggregate(genericTheme)
  .settings(
    publish / skip := true
  )

lazy val genericTheme = project
  .in(file("themes") / "generic")
  .enablePlugins(ParadoxThemePlugin)
  .settings(
    name := "paradox-theme-generic",
    libraryDependencies ++= Seq(
      Library.foundation % "provided",
      Library.prettify % "provided"
    ),
  )

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(ParadoxPlugin)
  .settings(
    name := "paradox docs",
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    Compile / paradoxProperties ++= Map(
      "empty" -> "",
      "version" -> version.value
    ),
    paradoxGroups := Map("Language" -> Seq("Scala", "Java")),
    publish / skip := true
  )

addCommandAlias("verify", ";Test/compile ;Compile/doc ;test ;scripted ;docs/paradox")
addCommandAlias("verify-no-docker", ";Test/compile ;Compile/doc ;test ;scripted paradox/* ;docs/paradox")
