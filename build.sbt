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

import sbt.ScriptedPlugin.autoImport.sbtTestDirectory

inThisBuild(
  List(
    organization := "com.lightbend.paradox",
    licenses += "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"),
    organizationName     := "lightbend",
    organizationHomepage := Some(url("https://lightbend.com/")),
    homepage             := Some(url("https://lightbend.github.io/paradox/")),
    scmInfo    := Some(ScmInfo(url("https://github.com/lightbend/paradox"), "git@github.com:lightbend/paradox.git")),
    developers := List(
      Developer("pvlugter", "Peter Vlugter", "@pvlugter", url("https://github.com/pvlugter")),
      Developer("eed3si9n", "Eugene Yokota", "@eed3si9n", url("https://github.com/eed3si9n"))
    ),
    description := "Paradox is a markdown documentation tool for software projects."
  )
)

// https://github.com/djspiewak/sbt-github-actions
ThisBuild / githubWorkflowJavaVersions := List(
  JavaSpec.temurin("17"),
  JavaSpec.temurin("11")
)
ThisBuild / githubWorkflowScalaVersions := List("2.12.21", "3.8.1")
ThisBuild / githubWorkflowBuildMatrixExclusions := Seq(
  MatrixExclude(Map("scala" -> "3.8.1", "java" -> "temurin@11"))
)
ThisBuild / githubWorkflowSbtCommand := "sbt -batch"
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(List("verify"), name = Some("Verify project"))
)
ThisBuild / githubWorkflowTargetBranches := Seq("main")
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v")))
ThisBuild / githubWorkflowPublish               := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "CI_CLEAN" -> "clean",
      "CI_RELEASE" -> ";^ core/publishSigned ;^ testkit/publishSigned ;^ plugin/publishSigned ;^ themePlugin/publishSigned ; genericTheme/publishSigned",
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

lazy val paradox = project
  .in(file("."))
  .aggregate(core, testkit, tests, plugin, themePlugin, themes, docs)
  .settings(
    publish / skip := true
  )

lazy val scala3 = "3.8.1"

lazy val core = project
  .in(file("core"))
  .settings(
    name               := "paradox",
    crossScalaVersions := Seq(scalaVersion.value, "2.13.18", scala3),
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
    crossScalaVersions := (core / crossScalaVersions).value,
    libraryDependencies ++= Seq(
      Library.jtidy
    )
  )

lazy val tests = project
  .in(file("tests"))
  .dependsOn(core, testkit)
  .settings(
    name := "tests",
    crossScalaVersions := (core / crossScalaVersions).value,
    libraryDependencies ++= Seq(
      Library.scalatest % "test"
    ),
    publish / skip := true,
    Test / parallelExecution := false
  )

lazy val plugin = project
  .in(file("plugin"))
  .dependsOn(core)
  .enablePlugins(SbtPlugin)
  .settings(
    name      := "sbt-paradox",
    crossScalaVersions := Seq("2.12.21", scala3), // sbt 1 uses 2.12, sbt 2 uses 3 — no 2.13
    sbtPlugin := true,
    addSbtPlugin(Library.sbtWeb),
    addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.1.0"),
    (pluginCrossBuild / sbtVersion) := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.12.4"
        case _      => "2.0.0-RC9"
      }
    },
    scriptedSbt := (pluginCrossBuild / sbtVersion).value,
    scriptedLaunchOpts += ("-Dproject.version=" + version.value),
    scriptedLaunchOpts ++= ManagementFactory.getRuntimeMXBean.getInputArguments.asScala
      .filter(a => Seq("-Xmx", "-Xms", "-XX", "-Dfile").exists(a.startsWith)),
    scriptedDependencies := {
      val p1 = (core / publishLocal).value
      val p2 = publishLocal.value
      val p3 = (genericTheme / publishLocal).value
    },
    Compile / resourceGenerators += Def.task {
      val file = (Compile / resourceManaged).value / "paradox.properties"
      IO.write(
        file,
        s"""|paradox.organization=${organization.value}
            |paradox.version=${version.value}
            |""".stripMargin
      )
      Seq(file)
    }.taskValue
  )

lazy val themePlugin = project
  .in(file("theme-plugin"))
  .settings(
    name               := "sbt-paradox-theme",
    crossScalaVersions  := Seq("2.12.21", scala3),
    sbtPlugin          := true,
    addSbtPlugin(Library.sbtWeb),
    addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.1.0"),
    (pluginCrossBuild / sbtVersion) := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.12.4"
        case _      => "2.0.0-RC9"
      }
    }
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
      Library.prettify   % "provided"
    )
  )

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(ParadoxPlugin)
  .settings(
    name         := "paradox docs",
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    Compile / paradoxProperties ++= Map(
      "empty" -> "",
      "version" -> version.value
    ),
    paradoxGroups  := Map("Language" -> Seq("Scala", "Java")),
    publish / skip := true
  )

addCommandAlias("verify", ";Test/compile ;Compile/doc ;test ;scripted ;docs/paradox")

commands += Command.command("verify-no-docker") { state =>
  val extracted = Project.extract(state)
  val pluginRef = LocalProject("plugin")
  val base      = extracted.get(pluginRef / sbtTestDirectory) / "paradox"
  val sv        = extracted.get(pluginRef / scalaBinaryVersion)
  val exclude   = if (sv == "3") Set("libraryDependencies") else Set.empty[String]
  val tests = Option(base.listFiles).toSeq.flatten
    .filter(_.isDirectory)
    .map(_.getName)
    .filterNot(exclude)
    .sorted
    .map(n => s"paradox/$n")
    .mkString(" ")
  val cmds = "Test/compile" :: "Compile/doc" :: "test" ::
    s"plugin/scripted $tests" :: "docs/paradox" :: Nil
  cmds ::: state
}
