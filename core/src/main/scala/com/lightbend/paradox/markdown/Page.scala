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

package com.lightbend.paradox.markdown

import com.lightbend.paradox.tree.Tree.{ Forest, Location }
import com.lightbend.paradox.template.PageTemplate
import java.io.File
import java.net.URI
import java.nio.file.{ Path => NioPath, Paths => NioPaths }
import org.pegdown.ast.{ Node, RootNode, SpecialTextNode, TextNode }
import scala.annotation.tailrec

/**
 * Common interface for Page and Header, which are linkable.
 */
sealed abstract class Linkable {
  def path: String
  def label: Node
  def group: Option[String]
}

/**
 * Header in a page, with anchor path and markdown nodes.
 */
case class Header(path: String, label: Node, group: Option[String], includeIndexes: List[Int]) extends Linkable

case class Anchor(path: String)

/**
 * Markdown page with target path, parsed markdown, and headers.
 */
case class Page(file: File, path: String, rootSrcPage: String, label: Node, h1: Header, headers: Forest[Header], anchors: List[Anchor], markdown: RootNode, group: Option[String], properties: Page.Properties) extends Linkable {
  /**
   * Path to the root of the site.
   */
  val base: String = Path.basePath(path)

  /**
   * Extract a page title from text nodes in the label.
   */
  val title: String = {
    import scala.collection.JavaConverters._
    def textNodes(node: Node): Seq[String] = {
      node.getChildren.asScala.flatMap {
        case t: TextNode => Seq(t.getText)
        case other       => textNodes(other)
      }
    }
    textNodes(label).mkString
  }
}

object Page {
  /**
   * Create a single page from parsed markdown.
   */
  def apply(path: String, markdown: RootNode, properties: Map[String, String]): Page = {
    apply(path, markdown, identity, properties)
  }

  /**
   * Create a single page from parsed markdown.
   */
  def apply(path: String, markdown: RootNode, convertPath: String => String, properties: Map[String, String]): Page = {
    convertPage(convertPath)(Index.page(new File(path), path, markdown, properties))
  }

  /**
   * Convert parsed markdown pages into a linked forest of Page objects.
   */
  def forest(parsed: Seq[(File, String, RootNode, Map[String, String])], convertPath: String => String, properties: Map[String, String]): Forest[Page] = {
    Index.pages(parsed, properties) map (_ map convertPage(convertPath))
  }

  /**
   * Convert an Index.Page into the final Page and Headers.
   * The first h1 header is used for the page header and title.
   */
  def convertPage(convertPath: String => String)(page: Index.Page): Page = {
    // TODO: get default label node from page index link?
    val properties = Page.Properties(page.properties)
    val targetPath = properties.convertToTarget(convertPath)(page.path)
    val rootSrcPage = Path.relativeRootPath(page.file, page.path)
    val (h1, subheaders) = page.headers match {
      case h :: hs => (Header(h.label.path, h.label.markdown, h.label.group, h.label.includeIndexes), h.children ++ hs)
      case Nil     => (Header(targetPath, new SpecialTextNode(targetPath), None, Nil), Nil)
    }
    val anchors = page.anchors.map(a => Anchor(a.path))
    val headers = subheaders map (_ map (h => Header(h.path, h.markdown, h.group, h.includeIndexes)))
    Page(page.file, targetPath, rootSrcPage, h1.label, h1, headers, anchors, page.markdown, h1.group, properties)
  }

  /**
   * All pages, by path
   */
  def allPages(pages: Forest[Page]): Map[String, Page] = {
    @tailrec
    def collect(location: Option[Location[Page]], paths: List[(String, Page)] = Nil): List[(String, Page)] = location match {
      case Some(loc) => collect(loc.next, (loc.tree.label.path, loc.tree.label) :: paths)
      case None      => paths
    }
    (pages flatMap { root => collect(Some(root.location)) }).toMap
  }

  /**
   * Specific properties at page level for the current page
   */
  case class Properties(props: Map[String, String]) {
    def get: Map[String, String] = props

    /**
     * Give the property associated to the key given in input
     */
    def apply(property: String, default: String = ""): String = {
      props.getOrElse(property, default)
    }

    /**
     * Convert the source file path to the target file path according to the "out" property or not
     */
    def convertToTarget(convertPath: String => String): String => String =
      (path: String) => replaceFile(props.get(Properties.DefaultOutMdIndicator))(path) getOrElse convertPath(path)

    // TODO: give the target suffix ".html" in a more general way
    private def replaceFile(prop: Option[String], targetSuffix: String = ".html")(path: String): Option[String] = prop match {
      case Some(p) if (p.endsWith(targetSuffix)) => Some(path.dropRight(Path.leaf(path).length) + p)
      case _                                     => None
    }
  }

  object Properties {
    val DefaultOutMdIndicator = "out"
    val DefaultLayoutMdIndicator = "layout"
    val DefaultSingleLayoutMdIndicator = "single-layout"
  }

  /**
   * Create an included page.
   */
  def included(file: File, includeFilePath: String, includedIn: Page, markdown: RootNode): Page = {
    val rootSrcPage = Path.relativeRootPath(file, includeFilePath)
    Page(file, includedIn.path, rootSrcPage, includedIn.h1.label, includedIn.h1, includedIn.headers, includedIn.anchors, markdown,
      includedIn.group, includedIn.properties)
  }
}

/**
 * Helper methods for paths.
 */
object Path {
  /**
   * Form a relative path to the root, based on the number of directories in a path.
   */
  def basePath(path: String): String = {
    "../" * path.count(_ == '/')
  }

  /**
   * Resolve a relative path against a base path.
   */
  def resolve(base: String, path: String): String = {
    new URI(base).resolve(path).getPath
  }

  /**
   * Replace the file extension in a path.
   */
  def replaceExtension(from: String, to: String)(link: String): Option[String] = {
    val uri = new URI(link)
    Some(replaceSuffix(from, to)(uri.getPath) + Option(uri.getFragment).fold("")("#".+))
  }

  /**
   * Replace the suffix of a path.
   */
  def replaceSuffix(from: String, to: String)(path: String): String = {
    if (path.endsWith(from)) path.dropRight(from.length) + to else path
  }

  /**
   * Provide the leaf (file) from a path
   */
  def leaf(path: String): String = {
    path.split('/').reverse.head
  }

  /**
   * Normalize the path to Unix style root path. Removes drive letter and appends the "/" symbol.
   * Also converts backslashes to slashes.
   */
  def toUnixStyleRootPath(pathString: String): String = toUnixStyleRootPath(NioPaths.get(pathString))

  /**
   * Normalize the path to Unix style root path. Removes drive letter and appends the "/" symbol.
   * Also converts backslashes to slashes.
   */
  def toUnixStyleRootPath(path: NioPath): String = {
    val fullPathWithDriveLetter = path.toAbsolutePath

    val fullPathString =
      if (fullPathWithDriveLetter.getRoot ne null)
        File.separator + fullPathWithDriveLetter.getRoot.relativize(fullPathWithDriveLetter).toString
      else
        fullPathWithDriveLetter.toString

    fullPathString.replace('\\', '/')
  }

  /**
   * Provide the relative root path from a local path related to a full path
   */
  def relativeRootPath(file: File, localPath: String): String = {
    val pathString = toUnixStyleRootPath(file.toPath)
    if (pathString.endsWith(localPath)) pathString.dropRight(localPath.length) else pathString
  }

  /**
   * Provide the local path given the root path and the full path
   */
  def relativeLocalPath(rootPath: String, fullPath: String): String = {
    val root = new URI(toUnixStyleRootPath(rootPath))
    val full = new URI(toUnixStyleRootPath(fullPath))
    root.relativize(full).toString
  }

  /**
   * Provide the final target file given a particular source file/link
   */
  def generateTargetFile(localPath: String, globalPageMappings: Map[String, String]): String => Option[String] = {
    val mappings = relativeMapping(localPath, globalPageMappings)

    { link =>
      val uri = new URI(localPath).resolve(new URI(link))
      mappings.get(uri.getPath).map { p =>
        p + Option(uri.getFragment).fold("")("#".+)
      }
    }
  }

  /**
   * Provide the mappings "source to target" files relative to the current file path given the root mappings
   */
  def relativeMapping(localPath: String, globalPageMappings: Map[String, String]): Map[String, String] = {
    def parentsPath(path: String): List[String] = path.split('/').toList.reverse.tail.reverse

    val rootPath = parentsPath(localPath)
    globalPageMappings map { mapping =>
      val rootMap = (parentsPath(mapping._1), parentsPath(mapping._2))
      mapping._1 -> (refRelativePath(rootPath, rootMap._2, leaf(mapping._2)))
    }
  }

  /**
   * Provide the modified path relative to the root path
   */
  def refRelativePath(root: List[String], path: List[String], leafFile: String): String = {
    def listPath(root: List[String], path: List[String]): List[String] = (root, path) match {
      case (Nil, ps)                      => ps
      case (rs, Nil)                      => rs map (_ => "..")
      case (r :: rs, p :: ps) if (r == p) => listPath(rs, ps)
      case _                              => root.map(_ => "..") ::: path
    }
    (listPath(root, path) ::: List(leafFile)).mkString("/")
  }
}
