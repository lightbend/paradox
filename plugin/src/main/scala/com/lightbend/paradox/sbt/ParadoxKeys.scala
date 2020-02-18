/*
 * Copyright © 2015 - 2019 Lightbend, Inc. <http://www.lightbend.com>
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

package com.lightbend.paradox.sbt

import sbt._
import com.lightbend.paradox.ParadoxProcessor
import com.lightbend.paradox.markdown.{ Directive, Writer }
import com.lightbend.paradox.template.PageTemplate

import scala.util.matching.Regex

trait ParadoxKeys {
  val paradox = taskKey[File]("Build the paradox site.")
  val paradoxMarkdownToHtml = taskKey[Seq[(File, String)]]("Convert markdown files to HTML.")
  val paradoxNavigationDepth = settingKey[Int]("Determines depth of TOC for page navigation.")
  val paradoxNavigationExpandDepth = settingKey[Option[Int]]("Depth of auto-expanding navigation below the active page.")
  val paradoxNavigationIncludeHeaders = settingKey[Boolean]("Whether to include headers in the navigation.")
  @deprecated("Enumerate the roots in `paradoxRoots`", since = "0.6.1")
  val paradoxExpectedNumberOfRoots = settingKey[Int]("How many ToC roots to expect.")
  val paradoxRoots = settingKey[List[String]]("Which ToC roots (pages without parent) to expect.")
  val paradoxLeadingBreadcrumbs = settingKey[List[(String, String)]]("Any leading breadcrumbs (label -> url)")
  val paradoxIllegalLinkPath = settingKey[Regex]("Path pattern to fail site creation (eg. to protect against missing `@ref` for links).")
  val paradoxOrganization = settingKey[String]("Paradox dependency organization (for theme dependencies).")
  val paradoxDirectives = taskKey[Seq[Writer.Context => Directive]]("Enabled paradox directives.")
  val paradoxProcessor = taskKey[ParadoxProcessor]("ParadoxProcessor to use when generating the site.")
  val paradoxProperties = taskKey[Map[String, String]]("Property map passed to paradox.")
  val paradoxSourceSuffix = settingKey[String]("Source file suffix for markdown files [default = \".md\"].")
  val paradoxTargetSuffix = settingKey[String]("Target file suffix for HTML files [default = \".html\"].")
  val paradoxTheme = settingKey[Option[ModuleID]]("Web module name of the paradox theme, otherwise local template.")
  val paradoxThemeDirectory = taskKey[File]("Sync combined theme and local template to a directory.")
  val paradoxOverlayDirectories = settingKey[Seq[File]]("Directory containing common source files for configuration.")
  val paradoxDefaultTemplateName = settingKey[String]("Name of default template for generating pages.")
  val paradoxTemplate = taskKey[PageTemplate]("PageTemplate to use when generating HTML pages.")
  val paradoxVersion = settingKey[String]("Paradox plugin version.")
  val paradoxGroups = settingKey[Map[String, Seq[String]]]("Paradox groups.")
  val paradoxBrowse = taskKey[Unit]("Open the docs in the default browser")
  val paradoxValidateInternalLinks = taskKey[Unit]("Validate internal, non ref paradox links.")
  val paradoxValidateLinks = taskKey[Unit]("Validate all non ref paradox links.")
  val paradoxValidationIgnorePaths = settingKey[List[Regex]]("List of regular expressions to apply to paths to determine if they should be ignored.")
  val paradoxValidationSiteBasePath = settingKey[Option[String]]("The base path that the documentation is deployed to, allows validating links on the docs site that are outside of the documentation root tree")
  val paradoxSingle = taskKey[File]("Build the single page HTML Paradox site")
  val paradoxSingleMarkdownToHtml = taskKey[Seq[(File, String)]]("Convert markdown files to single page HTML")
  val paradoxPdf = taskKey[File]("Build the paradox PDF")
  val paradoxPdfSite = taskKey[File]("Build the single page HTML Paradox site")
  val paradoxPdfDockerImage = settingKey[String]("The wkhtmltopdf docker image to us")
  val paradoxPdfArgs = settingKey[Seq[String]]("Arguments for wkhtmltopdf generation")
  val paradoxPdfTocTemplate = settingKey[Option[String]]("XSL template to use for generating the table of contents, relative to the theme directory.")
  val paradoxPdfMarkdownToHtml = taskKey[Seq[(File, String)]]("Convert markdown files to single page HTML")
}
