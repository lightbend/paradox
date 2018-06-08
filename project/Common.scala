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

import sbt._
import sbt.Keys._
import de.heikoseeberger.sbtheader.{ CommentStyle, FileType, License, HeaderPlugin, AutomateHeaderPlugin }
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

/**
 * Common sbt settings — automatically added to all projects.
 */
object Common extends AutoPlugin {

  override def trigger  = allRequirements

  override def requires = plugins.JvmPlugin && HeaderPlugin

  // AutomateHeaderPlugin is not an allRequirements-AutoPlugin, so explicitly add settings here:
  override def projectSettings = AutomateHeaderPlugin.projectSettings ++ Seq(
    scalaVersion := { (sbtBinaryVersion in pluginCrossBuild).value match {
      case "0.13" => "2.10.7"
      case _ => "2.12.6"
    }},
    crossSbtVersions := Seq("0.13.17", "1.0.4"),
    // fixed in https://github.com/sbt/sbt/pull/3397 (for sbt 0.13.17)
    sbtBinaryVersion in update := (sbtBinaryVersion in pluginCrossBuild).value,
    scalacOptions ++= Seq("-encoding", "UTF-8", "-unchecked", "-deprecation", "-feature"),
    scalacOptions ++= { (sbtBinaryVersion in pluginCrossBuild).value match {
      case "0.13" => Seq("-target:jvm-1.6")
      case _ => Seq.empty
    }},
    javacOptions ++= Seq("-encoding", "UTF-8"),
    javacOptions ++= { (sbtBinaryVersion in pluginCrossBuild).value match {
      case "0.13" => Seq("-source", "1.6", "-target", "1.6")
      case _ => Seq.empty
    }},
    resolvers += Resolver.typesafeIvyRepo("releases"),
    // Scalariform settings
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
      .setPreference(DoubleIndentConstructorArguments, true)
      .setPreference(DanglingCloseParenthesis, Preserve)
      .setPreference(AlignParameters, true),
    // Header settings
    HeaderPlugin.autoImport.headerMappings := Map(
      FileType.scala -> CommentStyle.cStyleBlockComment,
      FileType.java -> CommentStyle.cStyleBlockComment,
      FileType.conf -> CommentStyle.hashLineComment
    ),
    HeaderPlugin.autoImport.headerLicense := Some(License.Custom(licenseText))
  )

  val licenseText: String = {
    """|Copyright © 2015 - 2017 Lightbend, Inc. <http://www.lightbend.com>

       |Licensed under the Apache License, Version 2.0 (the "License");
       |you may not use this file except in compliance with the License.
       |You may obtain a copy of the License at
       |
       |http://www.apache.org/licenses/LICENSE-2.0
       |
       |Unless required by applicable law or agreed to in writing, software
       |distributed under the License is distributed on an "AS IS" BASIS,
       |WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       |See the License for the specific language governing permissions and
       |limitations under the License.""".stripMargin
  }
}
