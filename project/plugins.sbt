/*
 * Copyright Lightbend, Inc.
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

addSbtPlugin("com.github.gseitz" % "sbt-release"     % "1.0.1")
addSbtPlugin("com.typesafe.sbt"  % "sbt-scalariform" % "1.3.0")
addSbtPlugin("com.typesafe.tmp"  % "sbt-header"      % "1.5.0-JDK6-0.1")
addSbtPlugin("me.lessis"         % "bintray-sbt"     % "0.3.0")
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox" % "0.2.5")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value

// override scalariform version to get some fixes
resolvers += Resolver.typesafeRepo("releases")
libraryDependencies += "org.scalariform" %% "scalariform" % "0.1.5-20140822-69e2e30"

lazy val metaroot = (project in file(".")).
  dependsOn(metaThemePlugin)

lazy val metaThemePlugin = (project in file("theme-plugin"))
  .settings(
    sbtPlugin := true,
    scalaVersion := "2.10.6",
    addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.1.1"),
    unmanagedSourceDirectories in Compile :=
      mirrorScalaSource((baseDirectory in ThisBuild).value.getParentFile / "theme-plugin")
  )

// http://stackoverflow.com/a/37513852/3827
def mirrorScalaSource(baseDirectory: File): Seq[File] = {
  val scalaSourceDir = baseDirectory / "src" / "main" / "scala"
  if (scalaSourceDir.exists) scalaSourceDir :: Nil
  else sys.error(s"Missing source directory: $scalaSourceDir")
}
