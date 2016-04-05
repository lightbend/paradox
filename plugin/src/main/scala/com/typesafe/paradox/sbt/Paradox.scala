/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.sbt

import sbt._
import sbt.Keys._

import com.typesafe.paradox.ParadoxProcessor
import com.typesafe.paradox.template.PageTemplate
import com.typesafe.sbt.web.Import.{ Assets, WebKeys }
import com.typesafe.sbt.web.SbtWeb

object Import {
  object ParadoxKeys {
    val paradox = taskKey[File]("Build the paradox site.")
    val paradoxMarkdownToHtml = taskKey[Seq[(File, String)]]("Convert markdown files to HTML.")
    val paradoxNavigationDepth = settingKey[Int]("Determines depth of TOC for page navigation.")
    val paradoxOrganization = settingKey[String]("Paradox dependency organization (for theme dependencies).")
    val paradoxProcessor = taskKey[ParadoxProcessor]("ParadoxProcessor to use when generating the site.")
    val paradoxProperties = taskKey[Map[String, String]]("Property map passed to paradox.")
    val paradoxSourceSuffix = settingKey[String]("Source file suffix for markdown files [default = \".md\"].")
    val paradoxTargetSuffix = settingKey[String]("Target file suffix for HTML files [default = \".html\"].")
    val paradoxTheme = settingKey[Option[String]]("Web module name of the paradox theme, otherwise local template.")
    val paradoxThemeDirectory = taskKey[File]("Sync combined theme and local template to a directory.")
    val paradoxTemplate = taskKey[PageTemplate]("PageTemplate to use when generating HTML pages.")
    val paradoxVersion = settingKey[String]("Paradox plugin version.")
  }
}

object Paradox extends AutoPlugin {
  import Import.ParadoxKeys._

  val autoImport = Import

  override def requires = SbtWeb

  override def trigger = noTrigger

  override def projectSettings: Seq[Setting[_]] = paradoxGlobalSettings ++ inConfig(Compile)(paradoxSettings)

  def paradoxGlobalSettings: Seq[Setting[_]] = Seq(
    paradoxOrganization := readProperty("paradox.properties", "paradox.organization"),
    paradoxVersion := readProperty("paradox.properties", "paradox.version"),
    paradoxSourceSuffix := ".md",
    paradoxTargetSuffix := ".html",
    paradoxNavigationDepth := 2,
    paradoxProperties := Map.empty,
    paradoxTheme := None,
    libraryDependencies ++= paradoxTheme.value.toSeq map { theme =>
      paradoxOrganization.value % theme % paradoxVersion.value
    }
  )

  def paradoxSettings: Seq[Setting[_]] = Seq(
    paradoxProcessor := new ParadoxProcessor,

    name in paradox := name.value,
    version in paradox := version.value,
    description in paradox := description.value,

    paradoxProperties += "project.name" -> (name in paradox).value,
    paradoxProperties += "project.version" -> (version in paradox).value,
    paradoxProperties += "project.version.short" -> shortVersion((version in paradox).value),
    paradoxProperties += "project.description" -> (description in paradox).value,
    paradoxProperties ++= dateProperties,

    sourceDirectory in paradox := sourceDirectory.value / "paradox",
    sourceDirectories in paradox := Seq((sourceDirectory in paradox).value),

    includeFilter in paradoxMarkdownToHtml := "*.md",
    excludeFilter in paradoxMarkdownToHtml := HiddenFileFilter,
    sources in paradoxMarkdownToHtml <<= Defaults.collectFiles(sourceDirectories in paradox, includeFilter in paradoxMarkdownToHtml, excludeFilter in paradoxMarkdownToHtml),
    mappings in paradoxMarkdownToHtml <<= Defaults.relativeMappings(sources in paradoxMarkdownToHtml, sourceDirectories in paradox),
    target in paradoxMarkdownToHtml := target.value / "paradox" / "html",

    managedSourceDirectories in paradoxTheme := paradoxTheme.value.toSeq.map { theme =>
      (WebKeys.webJarsDirectory in Assets).value / (WebKeys.webModulesLib in Assets).value / theme
    },
    sourceDirectory in paradoxTheme := sourceDirectory.value / "paradox" / "_template",
    sourceDirectories in paradoxTheme := (managedSourceDirectories in paradoxTheme).value :+ (sourceDirectory in paradoxTheme).value,
    includeFilter in paradoxTheme := AllPassFilter,
    excludeFilter in paradoxTheme := HiddenFileFilter,
    sources in paradoxTheme <<= Defaults.collectFiles(sourceDirectories in paradoxTheme, includeFilter in paradoxTheme, excludeFilter in paradoxTheme) dependsOn {
      WebKeys.webJars in Assets // extract webjars first
    },
    mappings in paradoxTheme <<= Defaults.relativeMappings(sources in paradoxTheme, sourceDirectories in paradoxTheme),
    // if there are duplicates, select the file from the local template to allow overrides/extensions in themes
    WebKeys.deduplicators in paradoxTheme += SbtWeb.selectFileFrom((sourceDirectory in paradoxTheme).value),
    mappings in paradoxTheme := SbtWeb.deduplicateMappings((mappings in paradoxTheme).value, (WebKeys.deduplicators in paradoxTheme).value),
    target in paradoxTheme := target.value / "paradox" / "theme",
    paradoxThemeDirectory := SbtWeb.syncMappings(streams.value.cacheDirectory, (mappings in paradoxTheme).value, (target in paradoxTheme).value),

    paradoxTemplate := new PageTemplate(paradoxThemeDirectory.value),

    sourceDirectory in paradoxTemplate := (target in paradoxTheme).value, // result of combining published theme and local theme template
    sourceDirectories in paradoxTemplate := Seq((sourceDirectory in paradoxTemplate).value),
    includeFilter in paradoxTemplate := AllPassFilter,
    excludeFilter in paradoxTemplate := "*.st" || "*.stg",
    sources in paradoxTemplate <<= Defaults.collectFiles(sourceDirectories in paradoxTemplate, includeFilter in paradoxTemplate, excludeFilter in paradoxTemplate) dependsOn {
      paradoxThemeDirectory // trigger theme extraction first
    },
    mappings in paradoxTemplate <<= Defaults.relativeMappings(sources in paradoxTemplate, sourceDirectories in paradoxTemplate),

    paradoxMarkdownToHtml := {
      IO.delete((target in paradoxMarkdownToHtml).value)
      paradoxProcessor.value.process(
        (mappings in paradoxMarkdownToHtml).value,
        (target in paradoxMarkdownToHtml).value,
        paradoxSourceSuffix.value,
        paradoxTargetSuffix.value,
        paradoxProperties.value,
        paradoxNavigationDepth.value,
        paradoxTemplate.value,
        new PageTemplate.ErrorLogger(s => streams.value.log.error(s))
      )
    },

    includeFilter in paradox := AllPassFilter,
    excludeFilter in paradox := {
      // exclude markdown sources and the _template directory sources
      (includeFilter in paradoxMarkdownToHtml).value || InDirectoryFilter((sourceDirectory in paradoxTheme).value)
    },
    sources in paradox <<= Defaults.collectFiles(sourceDirectories in paradox, includeFilter in paradox, excludeFilter in paradox),
    mappings in paradox <<= Defaults.relativeMappings(sources in paradox, sourceDirectories in paradox),
    mappings in paradox ++= (mappings in paradoxTemplate).value,
    mappings in paradox ++= paradoxMarkdownToHtml.value,
    mappings in paradox ++= {
      // include webjar assets, but not the assets from the theme
      val themeFilter = (managedSourceDirectories in paradoxTheme).value.headOption.map(InDirectoryFilter).getOrElse(NothingFilter)
      (mappings in Assets).value filterNot { case (file, path) => themeFilter.accept(file) }
    },
    target in paradox := target.value / "paradox" / "site",

    watchSources in Defaults.ConfigGlobal ++= (sourceDirectories in paradox).value.***.get,

    paradox := SbtWeb.syncMappings(streams.value.cacheDirectory, (mappings in paradox).value, (target in paradox).value)
  )

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

}
