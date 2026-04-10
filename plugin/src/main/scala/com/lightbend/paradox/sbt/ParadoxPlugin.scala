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

import com.lightbend.paradox.markdown.Reader
import sbt._
import sbt.Keys._
import sbt.internal.io.Source
import sbt.Defaults.generate
import com.lightbend.paradox.{ParadoxLogger, ParadoxProcessor}
import com.lightbend.paradox.markdown.{GitHubResolver, SnipDirective, Writer}
import com.lightbend.paradox.template.PageTemplate
import com.typesafe.sbt.web.Import.{Assets, WebKeys}
import com.typesafe.sbt.web.SbtWeb
import sbtcompat.PluginCompat._

import scala.concurrent.duration._
import scala.sys.process.ProcessLogger

object ParadoxPlugin extends AutoPlugin {
  object autoImport extends ParadoxKeys {
    def builtinParadoxTheme(name: String): ModuleID =
      readProperty("paradox.properties", "paradox.organization") % s"paradox-theme-$name" % readProperty(
        "paradox.properties",
        "paradox.version"
      )
  }
  import autoImport._

  override def requires = SbtWeb

  override def trigger = noTrigger

  lazy val ParadoxTheme = config("paradox-theme").hide

  override def projectConfigurations: Seq[Configuration] = super.projectConfigurations :+ ParadoxTheme

  override def projectSettings: Seq[Setting[_]] = paradoxSettings(Compile)

  def paradoxGlobalSettings: Seq[Setting[_]] = Seq(
    paradoxOrganization             := readProperty("paradox.properties", "paradox.organization"),
    paradoxVersion                  := readProperty("paradox.properties", "paradox.version"),
    paradoxSourceSuffix             := Writer.DefaultSourceSuffix,
    paradoxTargetSuffix             := Writer.DefaultTargetSuffix,
    paradoxIllegalLinkPath          := Writer.DefaultIllegalLinkPath,
    paradoxNavigationDepth          := 2,
    paradoxNavigationExpandDepth    := None,
    paradoxNavigationIncludeHeaders := false,
    paradoxParsingTimeout           := 2.seconds,
    paradoxExpectedNumberOfRoots    := 1,
    paradoxRoots                    := List("index.html"),
    paradoxDirectives               := Def.uncached(Writer.defaultDirectives),
    paradoxProperties               := Def.uncached(Map.empty),
    paradoxTheme                    := Some(builtinParadoxTheme("generic")),
    paradoxDefaultTemplateName      := "page",
    paradoxLeadingBreadcrumbs       := Nil,
    paradoxGroups                   := Map.empty,
    libraryDependencies ++= paradoxTheme.value.toSeq map (_ % ParadoxTheme),
    paradoxValidateLinksRetryCount := 0,
    paradoxValidationIgnorePaths   := List("http://localhost.*".r),
    paradoxValidationSiteBasePath  := None
  )

  def paradoxSettings(config: Configuration): Seq[Setting[_]] = paradoxGlobalSettings ++
    inConfig(ParadoxTheme)(Defaults.configSettings) ++
    inConfig(config)(baseParadoxSettings)

  private def classLoader(classpath: Classpath)(implicit conv: xsbti.FileConverter): ClassLoader =
    new java.net.URLClassLoader(Path.toURLs(sbtcompat.PluginCompat.toFiles(classpath).toSeq).toArray, null)

  def baseParadoxSettings: Seq[Setting[_]] = Seq(
    Assets / WebKeys.webJarsClassLoader := Def.uncached {
      implicit val conv: xsbti.FileConverter = fileConverter.value
      classLoader((ParadoxTheme / dependencyClasspath).value)
    },
    paradoxProcessor := Def.uncached(
      new ParadoxProcessor(
        reader = new Reader(maxParsingTime = paradoxParsingTimeout.value),
        writer = new Writer(
          linkRenderer = Writer.defaultLinks,
          verbatimSerializers = Writer.defaultVerbatims,
          serializerPlugins = Writer.defaultPlugins(paradoxDirectives.value)
        )
      )
    ),
    sourceDirectory := {
      val config = configuration.value
      if (config.name != Compile.name)
        sourceDirectory.value / config.name
      else
        sourceDirectory.value
    },
    paradox / name        := name.value,
    paradox / version     := version.value,
    paradox / description := description.value,
    paradox / licenses    := licenses.value,
    paradoxProperties ++= Def.uncached(
      Map(
        "project.name" -> (paradox / name).value,
        "project.version" -> (paradox / version).value,
        "project.version.short" -> shortVersion((paradox / version).value),
        "project.description" -> (paradox / description).value,
        "project.license" -> Compat.licenseNamesToCommaSeparated((paradox / licenses).value),
        "snip.root.base_dir" -> baseDirectory.value.toString,
        SnipDirective.buildBaseDir -> (ThisBuild / baseDirectory).value.toString,
        SnipDirective.showGithubLinks -> "true",
        "github.root.base_dir" -> (ThisBuild / baseDirectory).value.toString,
        "scala.version" -> scalaVersion.value,
        "scala.binary.version" -> scalaBinaryVersion.value
      )
    ),
    paradoxProperties ++= Def.uncached(
      homepage.value match {
        case Some(url) =>
          Map(
            "project.url" -> url.toString,
            "canonical.base_url" -> url.toString
          )
        case None => Map.empty
      }
    ),
    paradoxProperties ++= Def.uncached(dateProperties),
    paradoxProperties ++= Def.uncached(
      linkProperties(
        scalaVersion.value,
        Compat.apiUrlForLinkProperties(apiURL.value),
        scmInfo.value,
        isSnapshot.value,
        version.value,
        paradoxProperties.value.isDefinedAt(GitHubResolver.baseUrl)
      )
    ),
    paradoxOverlayDirectories            := Nil,
    paradox / sourceDirectory            := sourceDirectory.value / "paradox",
    paradox / unmanagedSourceDirectories := Seq((paradox / sourceDirectory).value) ++ paradoxOverlayDirectories.value,
    paradox / sourceManaged              := target.value / "paradox_managed",
    paradox / managedSourceDirectories   := Seq((paradox / sourceManaged).value),
    paradox / sourceDirectories          := Classpaths
      .concatSettings(paradox / unmanagedSourceDirectories, paradox / managedSourceDirectories)
      .value,
    paradoxMarkdownToHtml / includeFilter    := "*.md",
    paradoxMarkdownToHtml / excludeFilter    := HiddenFileFilter,
    paradoxMarkdownToHtml / unmanagedSources := Defaults
      .collectFiles(
        paradox / unmanagedSourceDirectories,
        paradoxMarkdownToHtml / includeFilter,
        paradoxMarkdownToHtml / excludeFilter
      )
      .value,
    paradoxMarkdownToHtml / sourceGenerators := Nil,
    paradoxMarkdownToHtml / managedSources   := generate(paradoxMarkdownToHtml / sourceGenerators).value,
    paradoxMarkdownToHtml / sources          := Classpaths
      .concatDistinct(paradoxMarkdownToHtml / unmanagedSources, paradoxMarkdownToHtml / managedSources)
      .value,
    paradoxMarkdownToHtml / mappings := Defaults
      .relativeMappings(paradoxMarkdownToHtml / sources, paradox / sourceDirectories)
      .value,
    paradoxSingleMarkdownToHtml / mappings := (paradoxMarkdownToHtml / mappings).value,
    paradoxPdfMarkdownToHtml / mappings    := (paradoxMarkdownToHtml / mappings).value,
    paradoxMarkdownToHtml / target         := target.value / "paradox" / "html" / configTarget(configuration.value),
    paradoxSingleMarkdownToHtml / target   := target.value / "paradox" / "single-html" / configTarget(
      configuration.value
    ),
    paradoxPdfMarkdownToHtml / target := target.value / "paradox" / "pdf-html" / configTarget(configuration.value),
    paradoxTheme / managedSourceDirectories := paradoxTheme.value.toSeq.map { theme =>
      (Assets / WebKeys.webJarsDirectory).value / (Assets / WebKeys.webModulesLib).value / theme.name
    },
    paradoxTheme / sourceDirectory := sourceDirectory.value / "paradox" / "_template",
    paradoxTheme / sourceDirectories := (paradoxTheme / managedSourceDirectories).value :+ (paradoxTheme / sourceDirectory).value,
    paradoxTheme / includeFilter := AllPassFilter,
    paradoxTheme / excludeFilter := HiddenFileFilter,
    paradoxTheme / sources       := (Defaults.collectFiles(
      paradoxTheme / sourceDirectories,
      paradoxTheme / includeFilter,
      paradoxTheme / excludeFilter
    ) dependsOn
      Assets / WebKeys.webJars // extract webjars first
    ).value,
    paradoxTheme / mappings := Defaults
      .relativeMappings(paradoxTheme / sources, paradoxTheme / sourceDirectories)
      .value,
    // if there are duplicates, select the file from the local template to allow overrides/extensions in themes
    paradoxTheme / WebKeys.deduplicators += Def.uncached(SbtWeb.selectFileFrom((paradoxTheme / sourceDirectory).value)),
    paradoxTheme / mappings := SbtWeb
      .deduplicateMappings(
        (paradoxTheme / mappings).value,
        (paradoxTheme / WebKeys.deduplicators).value,
        fileConverter.value
      ),
    paradoxTheme / target := baseDirectory.value / "target" / "paradox" / "theme" / configTarget(configuration.value),
    paradoxThemeDirectory := Def.uncached(
      SbtWeb.syncMappings(
        streams.value.cacheStoreFactory.make("paradox-theme"),
        (paradoxTheme / mappings).value,
        (paradoxTheme / target).value,
        fileConverter.value
      )
    ),
    paradoxTemplate := Def.uncached {
      val dir = paradoxThemeDirectory.value
      if (!dir.exists) {
        IO.createDirectory(dir)
      }
      new PageTemplate(dir, paradoxDefaultTemplateName.value)
    },
    paradoxTemplate / sourceDirectory := (paradoxTheme / target).value, // result of combining published theme and local theme template
    paradoxTemplate / sourceDirectories := Seq((paradoxTemplate / sourceDirectory).value),
    paradoxTemplate / includeFilter     := AllPassFilter,
    paradoxTemplate / excludeFilter     := "*.st" || "*.stg",
    paradoxTemplate / sources           := (Defaults.collectFiles(
      paradoxTemplate / sourceDirectories,
      paradoxTemplate / includeFilter,
      paradoxTemplate / excludeFilter
    ) dependsOn
      paradoxThemeDirectory // trigger theme extraction first
    ).value,
    paradoxTemplate / mappings := Defaults
      .relativeMappings(paradoxTemplate / sources, paradoxTemplate / sourceDirectories)
      .value,
    paradoxMarkdownToHtml := Def.uncached {
      val strms = streams.value
      IO.delete((paradoxMarkdownToHtml / target).value)
      implicit val conv: xsbti.FileConverter = fileConverter.value
      paradoxProcessor.value.process(
        Compat.mappingsToFiles((paradoxMarkdownToHtml / mappings).value),
        paradoxLeadingBreadcrumbs.value,
        (paradoxMarkdownToHtml / target).value,
        paradoxSourceSuffix.value,
        paradoxTargetSuffix.value,
        paradoxIllegalLinkPath.value,
        paradoxGroups.value,
        paradoxProperties.value,
        paradoxNavigationDepth.value,
        paradoxNavigationExpandDepth.value,
        paradoxNavigationIncludeHeaders.value,
        paradoxRoots.value,
        paradoxTemplate.value,
        new SbtParadoxLogger(strms.log)
      ) match {
        case Left(error) =>
          strms.log.error(error)
          throw new ParadoxException
        case Right(files) => files
      }
    },
    paradoxSingleMarkdownToHtml := Def.uncached {
      val strms = streams.value
      IO.delete((paradoxSingleMarkdownToHtml / target).value)
      implicit val conv: xsbti.FileConverter = fileConverter.value
      paradoxProcessor.value.processSinglePage(
        Compat.mappingsToFiles((paradoxSingleMarkdownToHtml / mappings).value),
        (paradoxSingleMarkdownToHtml / target).value,
        (paradoxSingle / paradoxSourceSuffix).value,
        (paradoxSingle / paradoxTargetSuffix).value,
        (paradoxSingle / paradoxIllegalLinkPath).value,
        (paradoxSingle / paradoxGroups).value,
        (paradoxSingle / paradoxProperties).value,
        (paradoxSingle / paradoxNavigationDepth).value,
        (paradoxSingle / paradoxNavigationExpandDepth).value,
        (paradoxSingle / paradoxRoots).value,
        (paradoxSingle / paradoxTemplate).value,
        false,
        new SbtParadoxLogger(strms.log)
      ) match {
        case Left(error) =>
          strms.log.error(error)
          throw new ParadoxException
        case Right(files) => files
      }
    },
    paradoxPdfMarkdownToHtml := Def.uncached {
      val strms = streams.value
      IO.delete((paradoxPdfMarkdownToHtml / target).value)
      implicit val conv: xsbti.FileConverter = fileConverter.value
      paradoxProcessor.value.processSinglePage(
        Compat.mappingsToFiles((paradoxPdfMarkdownToHtml / mappings).value),
        (paradoxPdfMarkdownToHtml / target).value,
        (paradoxPdf / paradoxSourceSuffix).value,
        (paradoxPdf / paradoxTargetSuffix).value,
        (paradoxPdf / paradoxIllegalLinkPath).value,
        (paradoxPdf / paradoxGroups).value,
        (paradoxPdf / paradoxProperties).value,
        (paradoxPdf / paradoxNavigationDepth).value,
        (paradoxPdf / paradoxNavigationExpandDepth).value,
        (paradoxPdf / paradoxRoots).value,
        (paradoxPdf / paradoxTemplate).value,
        true,
        new SbtParadoxLogger(strms.log)
      ) match {
        case Left(error) =>
          strms.log.error(error)
          throw new ParadoxException
        case Right(files) => files
      }
    },
    paradox / includeFilter := AllPassFilter,
    paradox / excludeFilter :=
      // exclude markdown sources and the _template directory sources
      (paradoxMarkdownToHtml / includeFilter).value || InDirectoryFilter((paradoxTheme / sourceDirectory).value),
    paradox / sources := Defaults
      .collectFiles(paradox / sourceDirectories, paradox / includeFilter, paradox / excludeFilter)
      .value,
    Global / watchSources ++= Def.uncached(
      (paradox / sourceDirectories).value.map(d => new Source(d, AllPassFilter, NothingFilter))
    ),
    paradoxBrowse                           := openInBrowser(paradox.value / "index.html", streams.value.log),
    paradoxValidateInternalLinks / mappings := {
      val paradoxMappings = (paradox / mappings).value
      paradoxValidationSiteBasePath.value match {
        case None           => paradoxMappings
        case Some(basePath) =>
          val basePathPrefix = if (basePath.endsWith("/")) basePath else basePath + "/"
          paradoxMappings.map { case (file, path) =>
            file -> (basePathPrefix + path)
          }
      }
    },
    paradoxValidateInternalLinks := validateLinksTask(false).value,
    paradoxValidateLinks         := validateLinksTask(true).value,
    paradoxPdfTocTemplate        := Some("print-toc.xslt"),
    // 0.12.4 works but is very old and CSS support isn't that great. 0.12.5 completely broke toc support, see:
    // https://github.com/wkhtmltopdf/wkhtmltopdf/issues/3953
    // 0.12.6 still hasn't been released, so we're forced to rely on this dev build published here:
    // https://builds.wkhtmltopdf.org/0.12.6-dev/
    paradoxPdfDockerImage := "jamesroper/wkhtmltopdf:0.12.6-0.20180618.3.dev.e6d6f54",
    paradoxPdfArgs        := Seq(
      "--dump-outline",
      "/opt/paradox/pdf/" + configTarget(configuration.value) + "/toc.xml",
      "--footer-right",
      "[page]",
      "--footer-left",
      (paradoxPdf / name).value,
      "--footer-font-size",
      "8",
      "--footer-spacing",
      "5"
    ),
    paradoxPdf := Def.uncached {
      val _              = paradoxPdfSite.value
      val outputFileName = (paradoxPdf / moduleName).value + ".pdf"
      val ct             = configTarget(configuration.value)
      val paradoxRoot    = baseDirectory.value / "target" / "paradox"
      val outputDir      = paradoxRoot / "pdf" / ct
      val root           = (paradoxPdf / paradoxRoots).value.head
      outputDir.mkdirs()

      val command = Seq(
        "docker",
        "run",
        "--rm",
        "-v",
        paradoxRoot.getAbsolutePath + ":/opt/paradox",
        // This can be accessed by the above mount, but needs to include the configuration name in it. The print-toc.xml
        // can only use absolute file:/// links to resources, so to ensure it doesn't have to include main/test in its
        // references to css files, we put this here so that it can reference any resources in the site using
        // file:///opt/paradoxsite
        "-v",
        (paradoxRoot / "site-pdf" / ct).getAbsolutePath + ":/opt/paradoxsite",
        paradoxPdfDockerImage.value
      ) ++
        paradoxPdfArgs.value ++
        Seq("cover", s"file:///opt/paradox/site-pdf/$ct/print-cover.html") ++
        paradoxPdfTocTemplate.value.fold(Seq.empty[String])(t =>
          Seq("toc", "--xsl-style-sheet", s"/opt/paradox/theme/$ct/$t")
        ) ++
        Seq(
          s"file:///opt/paradox/site-pdf/$ct/$root",
          "--javascript-delay",
          "5000",
          s"/opt/paradox/pdf/$ct/$outputFileName"
        )

      import sys.process._

      val log = streams.value.log
      log.info("Running " + command.mkString(" "))

      command.!(new WkHtmlToPdfLogger(log)) match {
        case 0 =>
          val outputFile = outputDir / outputFileName
          log.info(s"PDF successfully generated to ${outputFile.getAbsolutePath}")
          outputFile
        case other =>
          throw new AlreadyHandledException(new RuntimeException("wkhtmltopdf had non zero return code: " + other))
      }
    }
  ) ++ defineSiteMappings(paradox, paradox, paradoxMarkdownToHtml, "site") ++
    defineSiteMappings(paradoxSingle, paradoxSingle, paradoxSingleMarkdownToHtml, "site-single") ++
    defineSiteMappings(paradoxPdf, paradoxPdfSite, paradoxPdfMarkdownToHtml, "site-pdf")

  private class WkHtmlToPdfLogger(log: Logger) extends ProcessLogger {
    override def out(s: => String): Unit = s match {
      case error if s.matches("^\\w+: .*") => log.error(error)
      case progress if s.startsWith("[")   => // ignore
      case info                            => log.info(info)
    }

    override def err(s: => String): Unit = out(s)

    override def buffer[T](f: => T): T = f
  }

  private def defineSiteMappings(
      scopeTask: TaskKey[_],
      siteTask: TaskKey[File],
      markdownToHtmlTask: TaskKey[Seq[(File, String)]],
      siteDir: String
  ) = Seq(
    scopeTask / mappings := Defaults.relativeMappings(paradox / sources, paradox / sourceDirectories).value,
    scopeTask / mappings ++= (paradoxTemplate / mappings).value,
    scopeTask / mappings ++= toFileRefsMapping(markdownToHtmlTask.value)(using fileConverter.value),
    scopeTask / mappings ++= {
      // include webjar assets, but not the assets from the theme
      val themeFilter =
        (paradoxTheme / managedSourceDirectories).value.headOption.map(InDirectoryFilter).getOrElse(NothingFilter)
      (Assets / mappings).value filterNot { case (file, path) =>
        themeFilter.accept(toFile(file)(using fileConverter.value))
      }
    },
    scopeTask / target := baseDirectory.value / "target" / "paradox" / siteDir / configTarget(configuration.value),
    siteTask           := Def.uncached(
      SbtWeb.syncMappings(
        streams.value.cacheStoreFactory.make("paradox-" + siteDir),
        (scopeTask / mappings).value,
        (scopeTask / target).value,
        fileConverter.value
      )
    )
  )

  private def validateLinksTask(validateAbsolute: Boolean) = Def.task {
    implicit val conv: xsbti.FileConverter = fileConverter.value
    val strms                              = streams.value
    val basePathPrefix                     = paradoxValidationSiteBasePath.value.fold("") {
      case withSlash if withSlash.endsWith("/") => withSlash
      case withoutSlash                         => withoutSlash + "/"
    }
    val errors = paradoxProcessor.value.validate(
      Compat.mappingsToFiles((paradoxMarkdownToHtml / mappings).value).map { case (file, path) =>
        file -> (basePathPrefix + path)
      },
      Compat.mappingsToFiles((paradoxValidateInternalLinks / mappings).value),
      paradoxGroups.value,
      paradoxProperties.value,
      paradoxValidationIgnorePaths.value,
      paradoxValidateLinksRetryCount.value,
      validateAbsolute,
      new SbtParadoxLogger(strms.log)
    )
    if (errors > 0) {
      strms.log.error(s"Paradox validation found $errors errors")
      throw new ParadoxException
    }
  }

  private def configTarget(config: Configuration) =
    if (config.name == Compile.name) "main"
    else config.name

  def shortVersion(version: String): String = version.replace("-SNAPSHOT", "*")

  def dateProperties: Map[String, String] = {
    import java.text.SimpleDateFormat
    val now = sys.env
      .get("SOURCE_DATE_EPOCH")
      .map(sde => new java.util.Date(sde.toLong * 1000))
      .getOrElse(new java.util.Date())
    val day   = new SimpleDateFormat("dd").format(now)
    val month = new SimpleDateFormat("MMM").format(now)
    val year  = new SimpleDateFormat("yyyy").format(now)
    Map(
      "date" -> s"$month $day, $year",
      "date.day" -> day,
      "date.month" -> month,
      "date.year" -> year
    )
  }

  def linkProperties(
      scalaVersion: String,
      apiURI: Option[java.net.URI],
      scmInfo: Option[ScmInfo],
      isSnapshot: Boolean,
      version: String,
      gitHubResolverBaseUrlDefined: Boolean
  ): Map[String, String] = {
    val JavaSpecVersion = """\d+\.(\d+)""".r
    Map(
      "javadoc.java.base_url" -> sys.props
        .get("java.specification.version")
        .map {
          case JavaSpecVersion(v) => v.toInt
          case v                  => v.toInt
        }
        .map { v =>
          if (v < 11) url(s"https://docs.oracle.com/javase/$v/docs/api/")
          else url(s"https://docs.oracle.com/en/java/javase/$v/docs/api/java.base/")
        },
      "scaladoc.version" -> Some(scalaVersion),
      "scaladoc.scala.base_url" -> Some(url(s"http://www.scala-lang.org/api/$scalaVersion")),
      "scaladoc.base_url" -> apiURI,
      GitHubResolver.baseUrl -> {
        if (!gitHubResolverBaseUrlDefined) {
          Compat.browseUrlString(scmInfo).collect {
            case url if !url.contains("/tree/") =>
              val branch = if (isSnapshot) "master" else s"v$version"
              s"$url/tree/$branch"
            case url => url
          }
        } else None
      }
    ).collect { case (prop, Some(value)) => (prop, value.toString) }
  }

  def readProperty(resource: String, property: String): String = {
    val props  = new java.util.Properties
    val stream = getClass.getClassLoader.getResourceAsStream(resource)
    try props.load(stream)
    catch { case e: Exception => }
    finally if (stream ne null) stream.close
    props.getProperty(property)
  }

  def InDirectoryFilter(base: File): FileFilter =
    new SimpleFileFilter(_.getAbsolutePath.startsWith(base.getAbsolutePath))

  def openInBrowser(rootDocFile: File, log: Logger): Unit = {
    import java.awt.Desktop
    def logCouldntOpen() = log.info(s"Couldn't open default browser, but docs are at $rootDocFile")
    if (Desktop.isDesktopSupported && Desktop.getDesktop.isSupported(Desktop.Action.BROWSE))
      Desktop.getDesktop.browse(rootDocFile.toURI)
    // This should work for all XDG compliant desktop environments
    else if (sys.env.contains("XDG_CURRENT_DESKTOP")) {
      import sys.process._
      if (Seq("xdg-open", rootDocFile.getAbsolutePath).! != 0) {
        logCouldntOpen()
      }
    } else logCouldntOpen()
  }

  class ParadoxException extends RuntimeException with FeedbackProvidedException

  class SbtParadoxLogger(logger: Logger) extends ParadoxLogger {
    override def debug(msg: => String): Unit = logger.debug(msg)
    override def info(msg: => String): Unit  = logger.info(msg)
    override def warn(msg: => String): Unit  = logger.warn(msg)
    override def error(msg: => String): Unit = logger.error(msg)
  }
}
