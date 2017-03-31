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
import de.heikoseeberger.sbtheader.{ HeaderPattern, HeaderPlugin, AutomateHeaderPlugin }
import com.typesafe.sbt.SbtScalariform.{ scalariformSettings, ScalariformKeys }
import scalariform.formatter.preferences._

/**
 * Common sbt settings — automatically added to all projects.
 */
object Common extends AutoPlugin {

  override def trigger  = allRequirements

  override def requires = plugins.JvmPlugin && HeaderPlugin

  // AutomateHeaderPlugin is not an allRequirements-AutoPlugin, so explicitly add settings here:
  override def projectSettings = scalariformSettings ++ AutomateHeaderPlugin.projectSettings ++ Seq(
    scalacOptions ++= Seq("-encoding", "UTF-8", "-target:jvm-1.6", "-unchecked", "-deprecation", "-feature"),
    javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6"),
    // Scalariform settings
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(PreserveDanglingCloseParenthesis, true)
      .setPreference(AlignParameters, true),
    // Header settings
    HeaderPlugin.autoImport.headers := Map(
      "scala" -> (HeaderPattern.cStyleBlockComment, scalaOrJavaHeader),
      "java" -> (HeaderPattern.cStyleBlockComment, scalaOrJavaHeader),
      "conf" -> (HeaderPattern.hashLineComment, confHeader)
    )
  )

  // Header text generation

  val scalaOrJavaHeader = header(before = Some("/*"), prefix = " *", after = Some(" */"))
  val confHeader = header(before = None, prefix = "#", after = None)

  def header(before: Option[String], prefix: String, after: Option[String]): String = {
    val content = Seq("Copyright © 2015 - 2017 Lightbend, Inc. <http://www.lightbend.com>",
      "",
      """Licensed under the Apache License, Version 2.0 (the "License");""",
      """you may not use this file except in compliance with the License.""",
      """You may obtain a copy of the License at""",
      "",
      """http://www.apache.org/licenses/LICENSE-2.0""",
      "",
      """Unless required by applicable law or agreed to in writing, software""",
      """distributed under the License is distributed on an "AS IS" BASIS,""",
      """WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.""",
      """See the License for the specific language governing permissions and""",
      """limitations under the License.""")
    def addPrefix(line: String) = if (line.isEmpty) prefix else s"$prefix $line"
    (before.toSeq ++ content.map(addPrefix) ++ after.toSeq).mkString("", "\n", "\n\n")
  }
}
