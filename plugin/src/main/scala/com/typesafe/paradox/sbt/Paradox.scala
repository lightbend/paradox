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
    val paradoxPageTemplate = taskKey[PageTemplate]("PageTemplate to use when generating HTML pages.")
    val paradoxProcessor = taskKey[ParadoxProcessor]("ParadoxProcessor to use when generating the site.")
    val paradoxProperties = taskKey[Map[String, String]]("Property map passed to paradox.")
    val paradoxSourceSuffix = settingKey[String]("Source file suffix for markdown files [default = \".md\"].")
    val paradoxTargetSuffix = settingKey[String]("Target file suffix for HTML files [default = \".html\"].")
    val paradoxTemplateDirectory = settingKey[File]("Location of templates.")
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
    paradoxProperties := Map.empty,
    paradoxSourceSuffix := ".md",
    paradoxTargetSuffix := ".html",

    paradoxTemplateDirectory := sourceDirectory.value / "templates",
    paradoxPageTemplate := new PageTemplate(paradoxTemplateDirectory.value),

    sourceDirectories in paradox := Seq(sourceDirectory.value / "paradox"),

    includeFilter in paradoxMarkdownToHtml := "*.md",
    excludeFilter in paradoxMarkdownToHtml := HiddenFileFilter,
    sources in paradoxMarkdownToHtml <<= Defaults.collectFiles(sourceDirectories in paradox, includeFilter in paradoxMarkdownToHtml, excludeFilter in paradoxMarkdownToHtml),
    mappings in paradoxMarkdownToHtml <<= Defaults.relativeMappings(sources in paradoxMarkdownToHtml, sourceDirectories in paradox),
    target in paradoxMarkdownToHtml := target.value / "paradox" / "html",

    paradoxMarkdownToHtml := {
      IO.delete((target in paradoxMarkdownToHtml).value)
      paradoxProcessor.value.process(
        (mappings in paradoxMarkdownToHtml).value,
        (target in paradoxMarkdownToHtml).value,
        paradoxSourceSuffix.value,
        paradoxTargetSuffix.value,
        paradoxProperties.value,
        paradoxNavigationDepth.value,
        paradoxPageTemplate.value,
        new PageTemplate.ErrorLogger(s => streams.value.log.error(s))
      )
    },

    includeFilter in paradox := AllPassFilter,
    excludeFilter in paradox := (includeFilter in paradoxMarkdownToHtml).value,
    sources in paradox <<= Defaults.collectFiles(sourceDirectories in paradox, includeFilter in paradox, excludeFilter in paradox),
    mappings in paradox <<= Defaults.relativeMappings(sources in paradox, sourceDirectories in paradox),
    mappings in paradox ++= paradoxMarkdownToHtml.value,
    mappings in paradox ++= (mappings in Assets).value,
    target in paradox := target.value / "paradox" / "site",

    watchSources in Defaults.ConfigGlobal ++= {
      (sources in paradoxMarkdownToHtml).value ++ paradoxTemplateDirectory.value.***.get ++ (sources in paradox).value
    },

    paradox := SbtWeb.syncMappings(streams.value.cacheDirectory, (mappings in paradox).value, (target in paradox).value)
  )

}
