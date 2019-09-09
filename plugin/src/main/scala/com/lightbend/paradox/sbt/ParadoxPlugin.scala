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
import sbt.Keys._
import sbt.Defaults.generate
import com.lightbend.paradox.ParadoxProcessor
import com.lightbend.paradox.markdown.{ GitHubResolver, SnipDirective, Writer }
import com.lightbend.paradox.template.PageTemplate
import com.typesafe.sbt.web.Import.{ Assets, WebKeys }
import com.typesafe.sbt.web.{ SbtWeb, Compat => WCompat }

object ParadoxPlugin extends AutoPlugin {
  object autoImport extends ParadoxKeys {
    def builtinParadoxTheme(name: String): ModuleID =
      readProperty("paradox.properties", "paradox.organization") % s"paradox-theme-$name" % readProperty("paradox.properties", "paradox.version")
  }
  import autoImport._

  override def requires = SbtWeb

  override def trigger = noTrigger

  lazy val ParadoxTheme = config("paradox-theme").hide

  override def projectConfigurations: Seq[Configuration] = super.projectConfigurations :+ ParadoxTheme

  override def projectSettings: Seq[Setting[_]] = paradoxSettings(Compile)

  def paradoxGlobalSettings: Seq[Setting[_]] = Seq(
    paradoxOrganization := readProperty("paradox.properties", "paradox.organization"),
    paradoxVersion := readProperty("paradox.properties", "paradox.version"),
    paradoxSourceSuffix := ".md",
    paradoxTargetSuffix := ".html",
    paradoxNavigationDepth := 2,
    paradoxNavigationExpandDepth := None,
    paradoxNavigationIncludeHeaders := false,
    paradoxExpectedNumberOfRoots := 1,
    paradoxRoots := List("index.html"),
    paradoxDirectives := Writer.defaultDirectives,
    paradoxProperties := Map.empty,
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    paradoxDefaultTemplateName := "page",
    paradoxLeadingBreadcrumbs := Nil,
    paradoxGroups := Map.empty,
    libraryDependencies ++= paradoxTheme.value.toSeq map (_ % ParadoxTheme)
  )

  def paradoxSettings(config: Configuration): Seq[Setting[_]] = paradoxGlobalSettings ++
    inConfig(ParadoxTheme)(Defaults.configSettings) ++
    inConfig(config)(baseParadoxSettings)

  private def classLoader(classpath: Classpath): ClassLoader =
    new java.net.URLClassLoader(Path.toURLs(classpath.files), null)

  def baseParadoxSettings: Seq[Setting[_]] = Seq(
    WebKeys.webJarsClassLoader in Assets := classLoader((dependencyClasspath in ParadoxTheme).value),

    paradoxProcessor := new ParadoxProcessor(writer = new Writer(serializerPlugins = Writer.defaultPlugins(paradoxDirectives.value))),

    sourceDirectory := {
      val config = configuration.value
      if (config.name != Compile.name)
        sourceDirectory.value / config.name
      else
        sourceDirectory.value
    },

    name in paradox := name.value,
    version in paradox := version.value,
    description in paradox := description.value,
    licenses in paradox := licenses.value,

    paradoxProperties ++= Map(
      "project.name" -> (name in paradox).value,
      "project.version" -> (version in paradox).value,
      "project.version.short" -> shortVersion((version in paradox).value),
      "project.description" -> (description in paradox).value,
      "project.license" -> (licenses in paradox).value.map(_._1).mkString(","),
      "snip.root.base_dir" -> baseDirectory.value.toString,
      SnipDirective.buildBaseDir -> (baseDirectory in ThisBuild).value.toString,
      SnipDirective.showGithubLinks -> "true",
      "github.root.base_dir" -> (baseDirectory in ThisBuild).value.toString,
      "scala.version" -> scalaVersion.value,
      "scala.binary.version" -> scalaBinaryVersion.value),
    paradoxProperties ++= {
      homepage.value match {
        case Some(url) => Map(
          "project.url" -> url.toString,
          "canonical.base_url" -> url.toString
        )
        case None => Map.empty
      }
    },
    paradoxProperties ++= dateProperties,
    paradoxProperties ++= linkProperties(scalaVersion.value, apiURL.value, scmInfo.value, isSnapshot.value, version.value),

    paradoxOverlayDirectories := Nil,

    sourceDirectory in paradox := sourceDirectory.value / "paradox",
    unmanagedSourceDirectories in paradox := Seq((sourceDirectory in paradox).value) ++ paradoxOverlayDirectories.value,
    sourceManaged in paradox := target.value / "paradox_managed",
    managedSourceDirectories in paradox := Seq((sourceManaged in paradox).value),
    sourceDirectories in paradox := Classpaths.concatSettings(unmanagedSourceDirectories in paradox, managedSourceDirectories in paradox).value,

    includeFilter in paradoxMarkdownToHtml := "*.md",
    excludeFilter in paradoxMarkdownToHtml := HiddenFileFilter,

    unmanagedSources in paradoxMarkdownToHtml := Defaults.collectFiles(unmanagedSourceDirectories in paradox, includeFilter in paradoxMarkdownToHtml, excludeFilter in paradoxMarkdownToHtml).value,
    sourceGenerators in paradoxMarkdownToHtml := Nil,
    managedSources in paradoxMarkdownToHtml := generate(sourceGenerators in paradoxMarkdownToHtml).value,
    sources in paradoxMarkdownToHtml := Classpaths.concatDistinct(unmanagedSources in paradoxMarkdownToHtml, managedSources in paradoxMarkdownToHtml).value,
    mappings in paradoxMarkdownToHtml := Defaults.relativeMappings(sources in paradoxMarkdownToHtml, sourceDirectories in paradox).value,
    target in paradoxMarkdownToHtml := target.value / "paradox" / "html" / configTarget(configuration.value),

    managedSourceDirectories in paradoxTheme := paradoxTheme.value.toSeq.map { theme =>
      (WebKeys.webJarsDirectory in Assets).value / (WebKeys.webModulesLib in Assets).value / theme.name
    },
    sourceDirectory in paradoxTheme := sourceDirectory.value / "paradox" / "_template",
    sourceDirectories in paradoxTheme := (managedSourceDirectories in paradoxTheme).value :+ (sourceDirectory in paradoxTheme).value,
    includeFilter in paradoxTheme := AllPassFilter,
    excludeFilter in paradoxTheme := HiddenFileFilter,
    sources in paradoxTheme := (Defaults.collectFiles(sourceDirectories in paradoxTheme, includeFilter in paradoxTheme, excludeFilter in paradoxTheme) dependsOn {
      WebKeys.webJars in Assets // extract webjars first
    }).value,
    mappings in paradoxTheme := Defaults.relativeMappings(sources in paradoxTheme, sourceDirectories in paradoxTheme).value,
    // if there are duplicates, select the file from the local template to allow overrides/extensions in themes
    WebKeys.deduplicators in paradoxTheme += SbtWeb.selectFileFrom((sourceDirectory in paradoxTheme).value),
    mappings in paradoxTheme := SbtWeb.deduplicateMappings((mappings in paradoxTheme).value, (WebKeys.deduplicators in paradoxTheme).value),
    target in paradoxTheme := target.value / "paradox" / "theme" / configTarget(configuration.value),
    paradoxThemeDirectory := SbtWeb.syncMappings(WCompat.cacheStore(streams.value, "paradox-theme"), (mappings in paradoxTheme).value, (target in paradoxTheme).value),

    paradoxTemplate := {
      val dir = paradoxThemeDirectory.value
      if (!dir.exists) {
        IO.createDirectory(dir)
      }
      new PageTemplate(dir, paradoxDefaultTemplateName.value)
    },

    sourceDirectory in paradoxTemplate := (target in paradoxTheme).value, // result of combining published theme and local theme template
    sourceDirectories in paradoxTemplate := Seq((sourceDirectory in paradoxTemplate).value),
    includeFilter in paradoxTemplate := AllPassFilter,
    excludeFilter in paradoxTemplate := "*.st" || "*.stg",
    sources in paradoxTemplate := (Defaults.collectFiles(sourceDirectories in paradoxTemplate, includeFilter in paradoxTemplate, excludeFilter in paradoxTemplate) dependsOn {
      paradoxThemeDirectory // trigger theme extraction first
    }).value,
    mappings in paradoxTemplate := Defaults.relativeMappings(sources in paradoxTemplate, sourceDirectories in paradoxTemplate).value,

    paradoxMarkdownToHtml := (Def.taskDyn {
      val strms = streams.value
      IO.delete((target in paradoxMarkdownToHtml).value)
      Def.task {
        paradoxProcessor.value.process(
          (mappings in paradoxMarkdownToHtml).value,
          paradoxLeadingBreadcrumbs.value,
          (target in paradoxMarkdownToHtml).value,
          paradoxSourceSuffix.value,
          paradoxTargetSuffix.value,
          paradoxGroups.value,
          paradoxProperties.value,
          paradoxNavigationDepth.value,
          paradoxNavigationExpandDepth.value,
          paradoxNavigationIncludeHeaders.value,
          paradoxRoots.value,
          paradoxTemplate.value,
          s => strms.log.warn(s)
        )
      }
    }).value,

    includeFilter in paradox := AllPassFilter,
    excludeFilter in paradox := {
      // exclude markdown sources and the _template directory sources
      (includeFilter in paradoxMarkdownToHtml).value || InDirectoryFilter((sourceDirectory in paradoxTheme).value)
    },
    sources in paradox := Defaults.collectFiles(sourceDirectories in paradox, includeFilter in paradox, excludeFilter in paradox).value,
    mappings in paradox := Defaults.relativeMappings(sources in paradox, sourceDirectories in paradox).value,
    mappings in paradox ++= (mappings in paradoxTemplate).value,
    mappings in paradox ++= paradoxMarkdownToHtml.value,
    mappings in paradox ++= {
      // include webjar assets, but not the assets from the theme
      val themeFilter = (managedSourceDirectories in paradoxTheme).value.headOption.map(InDirectoryFilter).getOrElse(NothingFilter)
      (mappings in Assets).value filterNot { case (file, path) => themeFilter.accept(file) }
    },
    target in paradox := target.value / "paradox" / "site" / configTarget(configuration.value),

    watchSources in Defaults.ConfigGlobal ++= Compat.sourcesFor((sourceDirectories in paradox).value),

    paradoxBrowse := openInBrowser(paradox.value / "index.html", streams.value.log),

    paradox := SbtWeb.syncMappings(WCompat.cacheStore(streams.value, "paradox"), (mappings in paradox).value, (target in paradox).value)
  )

  private def configTarget(config: Configuration) =
    if (config.name == Compile.name) "main"
    else config.name

  def shortVersion(version: String): String = version.replace("-SNAPSHOT", "*")

  def dateProperties: Map[String, String] = {
    import java.text.SimpleDateFormat
    val now = new java.util.Date
    val day = new SimpleDateFormat("dd").format(now)
    val month = new SimpleDateFormat("MMM").format(now)
    val year = new SimpleDateFormat("yyyy").format(now)
    Map(
      "date" -> s"$month $day, $year",
      "date.day" -> day,
      "date.month" -> month,
      "date.year" -> year
    )
  }

  def linkProperties(scalaVersion: String, apiURL: Option[java.net.URL], scmInfo: Option[ScmInfo], isSnapshot: Boolean, version: String): Map[String, String] = {
    val JavaSpecVersion = """\d+\.(\d+)""".r
    Map(
      "javadoc.java.base_url" -> sys.props.get("java.specification.version").map {
        case JavaSpecVersion(v) => v
        case v                  => v
      }.map { v => url(s"https://docs.oracle.com/javase/$v/docs/api/") },
      "scaladoc.version" -> Some(scalaVersion),
      "scaladoc.scala.base_url" -> Some(url(s"http://www.scala-lang.org/api/$scalaVersion")),
      "scaladoc.base_url" -> apiURL,
      GitHubResolver.baseUrl -> scmInfo
        .map(_.browseUrl)
        .filter(_.getHost == "github.com")
        .map(_.toExternalForm)
        .collect {
          case url if !url.contains("/tree/") =>
            val branch = if (isSnapshot) "master" else s"v$version"
            s"$url/tree/$branch"
          case url => url
        }
    ).collect { case (prop, Some(value)) => (prop, value.toString) }
  }

  def readProperty(resource: String, property: String): String = {
    val props = new java.util.Properties
    val stream = getClass.getClassLoader.getResourceAsStream(resource)
    try { props.load(stream) }
    catch { case e: Exception => }
    finally { if (stream ne null) stream.close }
    props.getProperty(property)
  }

  def InDirectoryFilter(base: File): FileFilter =
    new SimpleFileFilter(_.getAbsolutePath.startsWith(base.getAbsolutePath))

  def openInBrowser(rootDocFile: File, log: Logger): Unit = {
    import java.awt.Desktop
    def logCouldntOpen() = log.info(s"Couldn't open default browser, but docs are at $rootDocFile")
    if (Desktop.isDesktopSupported && Desktop.getDesktop.isSupported(Desktop.Action.BROWSE)) Desktop.getDesktop.browse(rootDocFile.toURI)
    // This should work for all XDG compliant desktop environments
    else if (sys.env.contains("XDG_CURRENT_DESKTOP")) {
      import sys.process._
      if (Seq("xdg-open", rootDocFile.getAbsolutePath).! != 0) {
        logCouldntOpen()
      }
    } else logCouldntOpen()
  }

}
