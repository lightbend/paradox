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
  .aggregate(core, plugin /*, themes*/)
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
      val file = (resourceManaged in Compile).value / "paradox.properties"
      IO.write(file,
        s"""|paradox.organization=${organization.value}
            |paradox.version=${version.value}
            |""".stripMargin)
      Seq(file)
    }.taskValue
  )

// lazy val themes = project
//   .in(file("themes"))
//   .aggregate(lightbendTheme)
//   .enablePlugins(NoPublish)

// lazy val lightbendTheme = project
//   .in(file("themes/lightbend"))
//   .enablePlugins(Theme)
//   .settings(
//     name := "paradox-theme-lightbend",
//     libraryDependencies ++= Seq(
//       Library.foundation % "provided",
//       Library.prettify % "provided"
//     )
//   )
