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

package com.lightbend.paradox.sbt

import sbt._
import sbt.Keys._

import com.lightbend.paradox.ParadoxProcessor
import com.lightbend.paradox.template.PageTemplate
import com.typesafe.sbt.web.Import.{ Assets, WebKeys }
import com.typesafe.sbt.web.SbtWeb

object ParadoxPlugin extends AutoPlugin {
  object autoImport extends ParadoxKeys {
    def builtinParadoxTheme(name: String): ModuleID =
      readProperty("paradox.properties", "paradox.organization") % s"paradox-theme-$name" % readProperty("paradox.properties", "paradox.version")
  }
  import autoImport._

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
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    paradoxLeadingBreadcrumbs := Nil,
    libraryDependencies ++= paradoxTheme.value.toSeq
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
    paradoxProperties ++= linkProperties(scalaVersion.value, apiURL.value),

    sourceDirectory in paradox := sourceDirectory.value / "paradox",
    sourceDirectories in paradox := Seq((sourceDirectory in paradox).value),

    includeFilter in paradoxMarkdownToHtml := "*.md",
    excludeFilter in paradoxMarkdownToHtml := HiddenFileFilter,
    sources in paradoxMarkdownToHtml <<= Defaults.collectFiles(sourceDirectories in paradox, includeFilter in paradoxMarkdownToHtml, excludeFilter in paradoxMarkdownToHtml),
    mappings in paradoxMarkdownToHtml <<= Defaults.relativeMappings(sources in paradoxMarkdownToHtml, sourceDirectories in paradox),
    target in paradoxMarkdownToHtml := target.value / "paradox" / "html",

    managedSourceDirectories in paradoxTheme := paradoxTheme.value.toSeq.map { theme =>
      (WebKeys.webJarsDirectory in Assets).value / (WebKeys.webModulesLib in Assets).value / theme.name
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

    paradoxTemplate := {
      val dir = paradoxThemeDirectory.value
      if (!dir.exists) {
        IO.createDirectory(dir)
      }
      new PageTemplate(dir)
    },

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
        paradoxLeadingBreadcrumbs.value,
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

  def linkProperties(scalaVersion: String, apiURL: Option[java.net.URL]): Map[String, String] = {
    val defaults = Map(
      "scaladoc.scala.base_url" -> s"http://www.scala-lang.org/api/$scalaVersion"
    )
    val scaladoc = apiURL.map("scaladoc.base_url" -> _.toString)

    defaults ++ scaladoc
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
