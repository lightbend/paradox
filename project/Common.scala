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
import xerial.sbt.Sonatype.SonatypeKeys

/**
 * Common sbt settings — automatically added to all projects.
 */
object Common extends AutoPlugin {

  override def trigger  = allRequirements

  override def requires = plugins.JvmPlugin && HeaderPlugin

  // AutomateHeaderPlugin is not an allRequirements-AutoPlugin, so explicitly add settings here:
  override def projectSettings = AutomateHeaderPlugin.projectSettings ++ Seq(
    scalaVersion := "2.12.6",
    scalacOptions ++= Seq("-encoding", "UTF-8", "-unchecked", "-deprecation", "-feature"),
    javacOptions ++= Seq("-encoding", "UTF-8"),
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
    HeaderPlugin.autoImport.headerLicense := Some(License.Custom(licenseText)),
    SonatypeKeys.sonatypeProfileName := "com.lightbend"
  )

  val licenseText: String = {
    """|Copyright © 2015 - 2019 Lightbend, Inc. <http://www.lightbend.com>

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
