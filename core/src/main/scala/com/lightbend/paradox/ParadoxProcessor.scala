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

package com.lightbend.paradox

import com.lightbend.paradox.template.PageTemplate
import com.lightbend.paradox.markdown._
import com.lightbend.paradox.tree.Tree.{ Forest, Location }
import java.io.{ File, FileOutputStream, OutputStreamWriter }
import java.nio.charset.StandardCharsets
import java.util

import org.pegdown.ast._
import org.stringtemplate.v4.STErrorListener

import scala.annotation.tailrec
import scala.collection.JavaConverters._

/**
 * Markdown site processor.
 */
class ParadoxProcessor(reader: Reader = new Reader, writer: Writer = new Writer) {

  /**
   * Process all mappings to build the site.
   */
  def process(
    mappings:           Seq[(File, String)],
    leadingBreadcrumbs: List[(String, String)],
    outputDirectory:    File,
    sourceSuffix:       String,
    targetSuffix:       String,
    groups:             Map[String, Seq[String]],
    properties:         Map[String, String],
    navDepth:           Int,
    navExpandDepth:     Option[Int],
    navIncludeHeaders:  Boolean,
    pageTemplate:       PageTemplate,
    errorListener:      STErrorListener): Seq[(File, String)] = {
    require(!groups.values.flatten.map(_.toLowerCase).groupBy(identity).values.exists(_.size > 1), "Group names may not overlap")

    val pages = parsePages(mappings, Path.replaceSuffix(sourceSuffix, targetSuffix), properties)
    val paths = Page.allPaths(pages).toSet
    val globalPageMappings = rootPageMappings(pages)

    val navToc = new TableOfContents(pages = true, headers = navIncludeHeaders, ordered = false, maxDepth = navDepth, maxExpandDepth = navExpandDepth)
    val pageToc = new TableOfContents(pages = false, headers = true, ordered = false, maxDepth = navDepth)

    @tailrec
    def render(location: Option[Location[Page]], rendered: Seq[(File, String)] = Seq.empty): Seq[(File, String)] = location match {
      case Some(loc) =>
        val page = loc.tree.label
        val pageProperties = properties ++ page.properties.get
        val currentMapping = Path.generateTargetFile(Path.relativeLocalPath(page.rootSrcPage, page.file.getPath), globalPageMappings)
        val writerContext = Writer.Context(loc, paths, reader, writer, currentMapping, sourceSuffix, targetSuffix, groups, pageProperties)
        val pageContext = PageContents(leadingBreadcrumbs, groups, loc, writer, writerContext, navToc, pageToc)
        val outputFile = new File(outputDirectory, page.path)
        outputFile.getParentFile.mkdirs
        pageTemplate.write(page.properties(Page.Properties.DefaultLayoutMdIndicator, pageTemplate.defaultName), pageContext, outputFile, errorListener)
        render(loc.next, rendered :+ (outputFile, page.path))
      case None => rendered
    }
    outputDirectory.mkdirs()
    createMetadata(outputDirectory, properties) :: (pages flatMap { root => render(Some(root.location)) })
  }

  private def createMetadata(outputDirectory: File, properties: Map[String, String]): (File, String) = {
    val metadataFilename = "paradox.json"
    val target = new File(outputDirectory, metadataFilename)
    val osWriter = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)
    osWriter.write(
      s"""{
         |  "name" : "${properties("project.name")}",
         |  "version" : "${properties("project.version")}"
         |}""".stripMargin)
    osWriter.close()
    (target, metadataFilename)
  }

  /**
   * Default template contents for a markdown page at a particular location.
   */
  case class PageContents(leadingBreadcrumbs: List[(String, String)], groups: Map[String, Seq[String]], loc: Location[Page], writer: Writer, context: Writer.Context, navToc: TableOfContents, pageToc: TableOfContents) extends PageTemplate.Contents {
    import scala.collection.JavaConverters._

    private val page = loc.tree.label

    val getTitle = page.title
    val getContent =
      try writer.writeContent(page.markdown, context)
      catch {
        case e: Throwable => throw new RuntimeException(s"Error writing content for page ${page.path}: ${e.getMessage}", e)
      }

    lazy val getBase = page.base
    lazy val getHome = link(Some(loc.root))
    lazy val getPrev = link(loc.prev)
    lazy val getSelf = link(Some(loc))
    lazy val getNext = link(loc.next)
    lazy val getBreadcrumbs = writer.writeBreadcrumbs(Breadcrumbs.markdown(leadingBreadcrumbs, loc.path), context)
    lazy val getNavigation = writer.writeNavigation(navToc.root(loc), context)
    lazy val getGroups = Groups.html(groups)
    lazy val hasSubheaders = page.headers.nonEmpty
    lazy val getToc = writer.writeToc(pageToc.headers(loc), context)
    lazy val getSource_url = githubLink(Some(loc)).getHtml

    lazy val getProperties = context.properties.asJava

    private def link(location: Option[Location[Page]]): PageTemplate.Link = PageLink(location, page, writer, context)
    private def githubLink(location: Option[Location[Page]]): PageTemplate.Link = GithubLink(location, page, writer, context)
  }

  /**
   * Default template links, rendered to just a relative uri and HTML for the link.
   */
  case class PageLink(location: Option[Location[Page]], current: Page, writer: Writer, context: Writer.Context) extends PageTemplate.Link {
    lazy val getHref: String = location.map(href).orNull
    lazy val getHtml: String = location.map(link).orNull
    lazy val getTitle: String = location.map(title).orNull
    lazy val getAbsolute: PageLink = PageLink(location, location.map(_.root.tree.label).getOrElse(current), writer, context)
    lazy val isActive: Boolean = location.exists(active)

    private def link(location: Location[Page]): String = {
      val node = if (active(location))
        new ClassyLinkNode(href(location), "active", location.tree.label.label)
      else
        new ExpLinkNode("", href(location), location.tree.label.label)
      writer.writeFragment(node, context)
    }

    private def active(location: Location[Page]): Boolean = {
      location.tree.label.path == current.path
    }

    private def href(location: Location[Page]): String = current.base + location.tree.label.path

    private def title(location: Location[Page]): String = location.tree.label.title
  }

  /**
   * Github links, rendered to just a HTML for the link.
   */
  case class GithubLink(location: Option[Location[Page]], page: Page, writer: Writer, context: Writer.Context) extends PageTemplate.Link with GitHubResolver {
    lazy val getHref: String = location.map(href).orNull
    lazy val getHtml: String = getHref // TODO: temporary, should provide a link directly
    lazy val getTitle: String = location.map(title).orNull
    lazy val isActive: Boolean = false

    override def variables = context.properties

    private def href(location: Location[Page]): String = {
      try {
        val sourceFilePath = location.tree.label.file.toString
        val rootPath = new File(".").getCanonicalFile.toString
        (treeUrl / Path.relativeLocalPath(rootPath, sourceFilePath)).toString
      } catch {
        case e: Url.Error => null
      }
    }

    private def title(location: Location[Page]): String = location.tree.label.title
  }

  /**
   * Parse markdown files (with paths) into a forest of linked pages.
   */
  def parsePages(mappings: Seq[(File, String)], convertPath: String => String, properties: Map[String, String]): Forest[Page] = {
    Page.forest(parseMarkdown(mappings, properties), convertPath, properties)
  }

  /**
   * Parse markdown files into pegdown AST.
   */
  def parseMarkdown(mappings: Seq[(File, String)], properties: Map[String, String]): Seq[(File, String, RootNode, Map[String, String])] = {
    mappings map {
      case (file, path) =>
        val frontin = Frontin(file)
        val root = parseAndProcessMarkdown(file, frontin.body, properties ++ frontin.header)
        (file, normalizePath(path), root, frontin.header)
    }
  }

  def parseAndProcessMarkdown(file: File, markdown: String, properties: Map[String, String]): RootNode = {
    val root = reader.read(markdown)
    processIncludes(file, root, properties)
  }

  private def processIncludes(file: File, root: RootNode, properties: Map[String, String]): RootNode = {
    val newRoot = new RootNode
    // This is a mutable list, and is expected to be mutated by anything that wishes to add children
    val newChildren = newRoot.getChildren

    root.getChildren.asScala.foreach {
      case include: DirectiveNode if include.name == "include" =>
        val labels = include.attributes.values("identifier").asScala
        val source = include.source match {
          case direct: DirectiveNode.Source.Direct => direct.value
          case other                               => throw IncludeDirective.IncludeSourceException(other)
        }
        val includeFile = SourceDirective.resolveFile("include", source, file, properties)
        val frontin = Frontin(includeFile)
        val filterLabels = include.attributes.booleanValue(
          "filterLabels",
          properties.get("include.filterLabels").exists(_ == "true"))
        val (text, snippetLang) = Snippet(includeFile, labels, filterLabels)
        // I guess we could support multiple markup languages in future...
        if (snippetLang != "md" && snippetLang != "markdown") {
          throw IncludeDirective.IncludeFormatException(snippetLang)
        }
        val includedRoot = parseAndProcessMarkdown(includeFile, text, properties ++ frontin.header)
        val includeNode = IncludeNode(includedRoot, includeFile, source)
        includeNode.setStartIndex(include.getStartIndex)
        includeNode.setEndIndex(include.getEndIndex)
        newChildren.add(includeNode)

      case other => newChildren.add(other)
    }

    newRoot.setReferences(root.getReferences)
    newRoot.setAbbreviations(root.getAbbreviations)

    newRoot
  }

  /**
   * Normalize path to '/' separator.
   */
  def normalizePath(path: String, separator: Char = java.io.File.separatorChar): String = {
    if (separator == '/') path else path.replace(separator, '/')
  }

  /**
   * Create Mappings from page path to target file name
   */
  def rootPageMappings(pages: Forest[Page]): Map[String, String] = {
    @tailrec
    def mapping(location: Option[Location[Page]], fileMappings: List[(String, String)] = Nil): List[(String, String)] = location match {
      case Some(loc) =>
        val page = loc.tree.label
        val fullSrcPath = page.file.getPath
        val curTargetPath = page.path
        val curSrcPath = Path.relativeLocalPath(page.rootSrcPage, fullSrcPath)
        val curMappings = (curSrcPath, curTargetPath)
        mapping(loc.next, curMappings :: fileMappings)
      case None => fileMappings
    }
    pages.flatMap { root => mapping(Some(root.location)) }.toMap
  }

}
