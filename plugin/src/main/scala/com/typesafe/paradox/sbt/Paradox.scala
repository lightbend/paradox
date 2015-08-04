/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.sbt

import sbt._
import sbt.Keys._

import com.typesafe.paradox.ParadoxProcessor
import com.typesafe.paradox.template.PageTemplate
import com.typesafe.sbt.web.Import.Assets
import com.typesafe.sbt.web.SbtWeb

object Import {
  object ParadoxKeys {
    val paradox = taskKey[File]("Build the paradox site.")
    val paradoxMarkdownToHtml = taskKey[Seq[(File, String)]]("Convert markdown files to HTML.")
    val paradoxNavigationDepth = settingKey[Int]("Determines depth of TOC for page navigation.")
    val paradoxTemplate = taskKey[PageTemplate]("PageTemplate to use when generating HTML pages.")
    val paradoxProcessor = taskKey[ParadoxProcessor]("ParadoxProcessor to use when generating the site.")
    val paradoxProperties = taskKey[Map[String, String]]("Property map passed to paradox.")
    val paradoxSourceSuffix = settingKey[String]("Source file suffix for markdown files [default = \".md\"].")
    val paradoxTargetSuffix = settingKey[String]("Target file suffix for HTML files [default = \".html\"].")
  }
}

object Paradox extends AutoPlugin {
  import Import.ParadoxKeys._

  val autoImport = Import

  override def requires = SbtWeb

  override def trigger = noTrigger

  override def projectSettings: Seq[Setting[_]] = inConfig(Compile)(paradoxSettings)

  def paradoxSettings: Seq[Setting[_]] = Seq(
    paradoxProcessor := new ParadoxProcessor,

    paradoxNavigationDepth := 2,
    paradoxSourceSuffix := ".md",
    paradoxTargetSuffix := ".html",

    paradoxProperties := Map.empty,
    paradoxProperties += "version" -> version.value,
    paradoxProperties += "version.short" -> version.value.replace("-SNAPSHOT", "*"),

    sourceDirectory in paradox := sourceDirectory.value / "paradox",
    sourceDirectories in paradox := Seq((sourceDirectory in paradox).value),

    includeFilter in paradoxMarkdownToHtml := "*.md",
    excludeFilter in paradoxMarkdownToHtml := HiddenFileFilter,
    sources in paradoxMarkdownToHtml <<= Defaults.collectFiles(sourceDirectories in paradox, includeFilter in paradoxMarkdownToHtml, excludeFilter in paradoxMarkdownToHtml),
    mappings in paradoxMarkdownToHtml <<= Defaults.relativeMappings(sources in paradoxMarkdownToHtml, sourceDirectories in paradox),
    target in paradoxMarkdownToHtml := target.value / "paradox" / "html",

    sourceDirectory in paradoxTemplate := sourceDirectory.value / "paradox" / "_template",
    paradoxTemplate := new PageTemplate((sourceDirectory in paradoxTemplate).value),
    sourceDirectories in paradoxTemplate := Seq((sourceDirectory in paradoxTemplate).value),
    includeFilter in paradoxTemplate := AllPassFilter,
    excludeFilter in paradoxTemplate := "*.st" || "*.stg",
    sources in paradoxTemplate <<= Defaults.collectFiles(sourceDirectories in paradoxTemplate, includeFilter in paradoxTemplate, excludeFilter in paradoxTemplate),
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
      (includeFilter in paradoxMarkdownToHtml).value ||
        new SimpleFileFilter(_.getAbsolutePath.startsWith((sourceDirectory in paradoxTemplate).value.getAbsolutePath))
    },
    sources in paradox <<= Defaults.collectFiles(sourceDirectories in paradox, includeFilter in paradox, excludeFilter in paradox),
    mappings in paradox <<= Defaults.relativeMappings(sources in paradox, sourceDirectories in paradox),
    mappings in paradox ++= (mappings in paradoxTemplate).value,
    mappings in paradox ++= paradoxMarkdownToHtml.value,
    mappings in paradox ++= (mappings in Assets).value,
    target in paradox := target.value / "paradox" / "site",

    watchSources in Defaults.ConfigGlobal ++= (sourceDirectories in paradox).value.***.get,

    paradox := SbtWeb.syncMappings(streams.value.cacheDirectory, (mappings in paradox).value, (target in paradox).value)
  )

}
