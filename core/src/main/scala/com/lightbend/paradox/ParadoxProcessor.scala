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

package com.lightbend.paradox

import com.lightbend.paradox.markdown.{ Breadcrumbs, Page, Path, Reader, TableOfContents, Writer, Frontin }
import com.lightbend.paradox.template.PageTemplate
import com.lightbend.paradox.tree.Tree.{ Forest, Location }
import java.io.File
import org.parboiled.common.FileUtils
import org.pegdown.ast.{ ActiveLinkNode, ExpLinkNode, Node, RootNode }
import org.stringtemplate.v4.STErrorListener
import scala.annotation.tailrec

/**
 * Markdown site processor.
 */
class ParadoxProcessor(reader: Reader = new Reader, writer: Writer = new Writer) {

  /**
   * Process all mappings to build the site.
   */
  def process(mappings: Seq[(File, String)],
              leadingBreadcrumbs: List[(String, String)],
              outputDirectory: File,
              sourceSuffix: String,
              targetSuffix: String,
              properties: Map[String, String],
              navigationDepth: Int,
              template: PageTemplate,
              errorListener: STErrorListener): Seq[(File, String)] = {
    val pages = parsePages(mappings, Path.replaceSuffix(sourceSuffix, targetSuffix))
    val paths = Page.allPaths(pages).toSet
    @tailrec
    def render(location: Option[Location[Page]], rendered: Seq[(File, String)] = Seq.empty): Seq[(File, String)] = location match {
      case Some(loc) =>
        val page = loc.tree.label
        val pageProperties: Map[String, String] = properties // ++ page.properties
        val writerContext = Writer.Context(loc, paths, sourceSuffix, targetSuffix, pageProperties)
        val pageToc = new TableOfContents(pages = true, headers = false, ordered = false, maxDepth = navigationDepth)
        val headerToc = new TableOfContents(pages = false, headers = true, ordered = false, maxDepth = navigationDepth)
        val pageContext = PageContents(leadingBreadcrumbs, loc, writer, writerContext, pageToc, headerToc)
        val outputFile = new File(outputDirectory, page.path)
        outputFile.getParentFile.mkdirs
        template.write(pageContext, outputFile, errorListener)
        render(loc.next, rendered :+ (outputFile, page.path))
      case None => rendered
    }
    pages flatMap { root => render(Some(root.location)) }
  }

  /**
   * Default template contents for a markdown page at a particular location.
   */
  case class PageContents(leadingBreadcrumbs: List[(String, String)], loc: Location[Page], writer: Writer, context: Writer.Context, pageToc: TableOfContents, headerToc: TableOfContents) extends PageTemplate.Contents {
    import scala.collection.JavaConverters._

    private val page = loc.tree.label

    val getTitle = page.title
    val getContent = writer.write(page.markdown, context)

    lazy val getBase = page.base
    lazy val getHome = link(Some(loc.root))
    lazy val getPrev = link(loc.prev)
    lazy val getNext = link(loc.next)
    lazy val getBreadcrumbs = writer.writeFragment(Breadcrumbs.markdown(leadingBreadcrumbs, loc.path), context)
    lazy val getNavigation = writer.writeFragment(pageToc.root(loc), context)
    lazy val hasSubheaders = page.headers.nonEmpty
    lazy val getToc = writer.writeFragment(headerToc.headers(loc), context)

    lazy val getProperties = context.properties.asJava

    private def link(location: Option[Location[Page]]): PageTemplate.Link = PageLink(location, page, writer, context)
  }

  /**
   * Default template links, rendered to just a relative uri and HTML for the link.
   */
  case class PageLink(location: Option[Location[Page]], current: Page, writer: Writer, context: Writer.Context) extends PageTemplate.Link {
    lazy val getHref: String = location.map(href).orNull
    lazy val getHtml: String = location.map(link).orNull
    lazy val getTitle: String = location.map(title).orNull
    lazy val isActive: Boolean = location.map(active).getOrElse(false)

    private def link(location: Location[Page]): String = {
      val node = if (active(location))
        new ActiveLinkNode(href(location), location.tree.label.label)
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
   * Parse markdown files (with paths) into a forest of linked pages.
   */
  def parsePages(mappings: Seq[(File, String)], convertPath: String => String): Forest[Page] = {
    Page.forest(parseMarkdown(mappings), convertPath)
  }

  /**
   * Parse markdown files into pegdown AST.
   */
  def parseMarkdown(mappings: Seq[(File, String)]): Seq[(File, String, RootNode, Map[String, String])] = {
    mappings map {
      case (file, path) =>
        val frontin = Frontin(file)
        (file, normalizePath(path), reader.read(frontin.body), frontin.header)
    }
  }

  /**
   * Normalize path to '/' separator.
   */
  def normalizePath(path: String, separator: Char = java.io.File.separatorChar): String = {
    if (separator == '/') path else path.replace(separator, '/')
  }

}
