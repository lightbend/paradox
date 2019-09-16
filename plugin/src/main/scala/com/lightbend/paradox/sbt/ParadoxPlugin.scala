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
import com.lightbend.paradox.{ ParadoxLogger, ParadoxProcessor }
import com.lightbend.paradox.markdown.{ GitHubResolver, SnipDirective, Writer }
import com.lightbend.paradox.template.PageTemplate
import com.typesafe.sbt.web.Import.{ Assets, WebKeys }
import com.typesafe.sbt.web.{ SbtWeb, Compat => WCompat }

import scala.sys.process.ProcessLogger

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
    paradoxSourceSuffix := Writer.DefaultSourceSuffix,
    paradoxTargetSuffix := Writer.DefaultTargetSuffix,
    paradoxIllegalLinkPath := Writer.DefaultIllegalLinkPath,
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
    libraryDependencies ++= paradoxTheme.value.toSeq map (_ % ParadoxTheme),
    paradoxValidationIgnorePaths := List("http://localhost.*".r),
    paradoxValidationSiteBasePath := None
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
    mappings in paradoxSingleMarkdownToHtml := (mappings in paradoxMarkdownToHtml).value,
    mappings in paradoxPdfMarkdownToHtml := (mappings in paradoxMarkdownToHtml).value,
    target in paradoxMarkdownToHtml := target.value / "paradox" / "html" / configTarget(configuration.value),
    target in paradoxSingleMarkdownToHtml := target.value / "paradox" / "single-html" / configTarget(configuration.value),
    target in paradoxPdfMarkdownToHtml := target.value / "paradox" / "pdf-html" / configTarget(configuration.value),

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

    paradoxMarkdownToHtml := Def.taskDyn {
      val strms = streams.value
      IO.delete((target in paradoxMarkdownToHtml).value)
      Def.task {
        paradoxProcessor.value.process(
          (mappings in paradoxMarkdownToHtml).value,
          paradoxLeadingBreadcrumbs.value,
          (target in paradoxMarkdownToHtml).value,
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
      }
    }.value,

    paradoxSingleMarkdownToHtml := Def.taskDyn {
      val strms = streams.value
      IO.delete((target in paradoxSingleMarkdownToHtml).value)
      Def.task {
        paradoxProcessor.value.processSinglePage(
          (mappings in paradoxSingleMarkdownToHtml).value,
          (target in paradoxSingleMarkdownToHtml).value,
          (paradoxSourceSuffix in paradoxSingle).value,
          (paradoxTargetSuffix in paradoxSingle).value,
          (paradoxGroups in paradoxSingle).value,
          (paradoxProperties in paradoxSingle).value,
          (paradoxNavigationDepth in paradoxSingle).value,
          (paradoxNavigationExpandDepth in paradoxSingle).value,
          (paradoxRoots in paradoxSingle).value,
          (paradoxTemplate in paradoxSingle).value,
          false,
          new SbtParadoxLogger(strms.log)
        ) match {
            case Left(error) =>
              strms.log.error(error)
              throw new ParadoxException
            case Right(files) => files
          }
      }
    }.value,

    paradoxPdfMarkdownToHtml := Def.taskDyn {
      val strms = streams.value
      IO.delete((target in paradoxPdfMarkdownToHtml).value)
      Def.task {
        paradoxProcessor.value.processSinglePage(
          (mappings in paradoxPdfMarkdownToHtml).value,
          (target in paradoxPdfMarkdownToHtml).value,
          (paradoxSourceSuffix in paradoxPdf).value,
          (paradoxTargetSuffix in paradoxPdf).value,
          (paradoxGroups in paradoxPdf).value,
          (paradoxProperties in paradoxPdf).value,
          (paradoxNavigationDepth in paradoxPdf).value,
          (paradoxNavigationExpandDepth in paradoxPdf).value,
          (paradoxRoots in paradoxPdf).value,
          (paradoxTemplate in paradoxPdf).value,
          true,
          new SbtParadoxLogger(strms.log)
        ) match {
            case Left(error) =>
              strms.log.error(error)
              throw new ParadoxException
            case Right(files) => files
          }
      }
    }.value,

    includeFilter in paradox := AllPassFilter,
    excludeFilter in paradox := {
      // exclude markdown sources and the _template directory sources
      (includeFilter in paradoxMarkdownToHtml).value || InDirectoryFilter((sourceDirectory in paradoxTheme).value)
    },
    sources in paradox := Defaults.collectFiles(sourceDirectories in paradox, includeFilter in paradox, excludeFilter in paradox).value,

    watchSources in Defaults.ConfigGlobal ++= Compat.sourcesFor((sourceDirectories in paradox).value),

    paradoxBrowse := openInBrowser(paradox.value / "index.html", streams.value.log),

    mappings in paradoxValidateInternalLinks := {
      val paradoxMappings = (mappings in paradox).value
      paradoxValidationSiteBasePath.value match {
        case None => paradoxMappings
        case Some(basePath) =>
          val basePathPrefix = if (basePath.endsWith("/")) basePath else basePath + "/"
          paradoxMappings.map {
            case (file, path) => file -> (basePathPrefix + path)
          }
      }
    },
    paradoxValidateInternalLinks := validateLinksTask(false).value,
    paradoxValidateLinks := validateLinksTask(true).value,

    paradoxPdfTocTemplate := Some("print-toc.xslt"),
    // 0.12.4 works but is very old and CSS support isn't that great. 0.12.5 completely broke toc support, see:
    // https://github.com/wkhtmltopdf/wkhtmltopdf/issues/3953
    // 0.12.6 still hasn't been released, so we're forced to rely on this dev build published here:
    // https://builds.wkhtmltopdf.org/0.12.6-dev/
    paradoxPdfDockerImage := "jamesroper/wkhtmltopdf:0.12.6-0.20180618.3.dev.e6d6f54",
    paradoxPdfArgs := Seq(
      "--dump-outline", "/opt/paradox/pdf/" + configTarget(configuration.value) + "/toc.xml",
      "--footer-right", "[page]",
      "--footer-left", (name in paradoxPdf).value,
      "--footer-font-size", "8",
      "--footer-spacing", "5"
    ),
    paradoxPdf := {
      val _ = paradoxPdfSite.value
      val outputFileName = (moduleName in paradoxPdf).value + ".pdf"
      val ct = configTarget(configuration.value)
      val outputDir = target.value / "paradox" / "pdf" / ct
      val root = (paradoxRoots in paradoxPdf).value.head
      outputDir.mkdirs()

      val command = Seq("docker", "run", "--rm",
        "-v", (target.value / "paradox").getAbsolutePath + ":/opt/paradox",
        // This can be accessed by the above mount, but needs to include the configuration name in it. The print-toc.xml
        // can only use absolute file:/// links to resources, so to ensure it doesn't have to include main/test in its
        // references to css files, we put this here so that it can reference any resources in the site using
        // file:///opt/paradoxsite
        "-v", (target.value / "paradox" / "site-pdf" / ct).getAbsolutePath + ":/opt/paradoxsite",
        paradoxPdfDockerImage.value
      ) ++
        paradoxPdfArgs.value ++
        Seq("cover", s"file:///opt/paradox/site-pdf/$ct/print-cover.html") ++
        paradoxPdfTocTemplate.value.fold(Seq.empty[String])(t => Seq("toc", "--xsl-style-sheet", s"/opt/paradox/theme/$ct/$t")) ++
        Seq(
          s"file:///opt/paradox/site-pdf/$ct/$root", "--javascript-delay", "5000",
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
        case other => throw new AlreadyHandledException(new RuntimeException("wkhtmltopdf had non zero return code: " + other))
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

  private def defineSiteMappings(scopeTask: TaskKey[_], siteTask: TaskKey[File], markdownToHtmlTask: TaskKey[Seq[(File, String)]], siteDir: String) = Seq(
    mappings in scopeTask := Defaults.relativeMappings(sources in paradox, sourceDirectories in paradox).value,
    mappings in scopeTask ++= (mappings in paradoxTemplate).value,
    mappings in scopeTask ++= markdownToHtmlTask.value,
    mappings in scopeTask ++= {
      // include webjar assets, but not the assets from the theme
      val themeFilter = (managedSourceDirectories in paradoxTheme).value.headOption.map(InDirectoryFilter).getOrElse(NothingFilter)
      (mappings in Assets).value filterNot { case (file, path) => themeFilter.accept(file) }
    },
    target in scopeTask := target.value / "paradox" / siteDir / configTarget(configuration.value),
    siteTask := SbtWeb.syncMappings(WCompat.cacheStore(streams.value, "paradox-" + siteDir), (mappings in scopeTask).value, (target in scopeTask).value)
  )

  private def validateLinksTask(validateAbsolute: Boolean) = Def.task {
    val strms = streams.value
    val basePathPrefix = paradoxValidationSiteBasePath.value.fold("") {
      case withSlash if withSlash.endsWith("/") => withSlash
      case withoutSlash                         => withoutSlash + "/"
    }
    val errors = paradoxProcessor.value.validate(
      (mappings in paradoxMarkdownToHtml).value.map {
        case (file, path) => file -> (basePathPrefix + path)
      },
      (mappings in paradoxValidateInternalLinks).value,
      paradoxGroups.value,
      paradoxProperties.value,
      paradoxValidationIgnorePaths.value,
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
    val now = sys.env.get("SOURCE_DATE_EPOCH")
      .map(sde => new java.util.Date(sde.toLong * 1000))
      .getOrElse(new java.util.Date())
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
        case JavaSpecVersion(v) => v.toInt
        case v                  => v.toInt
      }.map { v =>
        if (v < 11) url(s"https://docs.oracle.com/javase/$v/docs/api/")
        else url(s"https://docs.oracle.com/en/java/javase/$v/docs/api/java.base/")
      },
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

  class ParadoxException extends RuntimeException with FeedbackProvidedException

  class SbtParadoxLogger(logger: Logger) extends ParadoxLogger {
    override def debug(msg: => String): Unit = logger.debug(msg)
    override def info(msg: => String): Unit = logger.info(msg)
    override def warn(msg: => String): Unit = logger.warn(msg)
    override def error(msg: => String): Unit = logger.error(msg)
  }
}
