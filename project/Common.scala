/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
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
    organization := "com.typesafe.paradox",
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

  val scalaOrJavaHeader = header(before = Some("/*"), prefix = " * ", after = Some(" */"))
  val confHeader = header(before = None, prefix = "# ", after = None)

  def header(before: Option[String], prefix: String, after: Option[String]): String = {
    val content = Seq("Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>")
    (before.toSeq ++ content.map(prefix.+) ++ after.toSeq).mkString("", "\n", "\n\n")
  }

}
