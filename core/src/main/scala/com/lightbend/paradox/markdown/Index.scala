/*
 * Copyright © 2015 - 2017 Lightbend, Inc. <http://www.lightbend.com>
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

import com.lightbend.paradox.tree.Tree
import com.lightbend.paradox.tree.Tree.Forest
import java.io.File
import org.pegdown.ast._
import scala.annotation.tailrec
import scala.collection.JavaConverters._

/**
 * Create index of pages from parsed markdown.
 */
object Index {

  case class Ref(level: Int, path: String, markdown: Node, group: Option[String])

  case class Page(file: File, path: String, markdown: RootNode, properties: Map[String, String], indices: Forest[Ref], headers: Forest[Ref])

  def pages(parsed: Seq[(File, String, RootNode, Map[String, String])], properties: Map[String, String]): Forest[Page] = {
    link(parsed.map((page _).tupled).toList, properties)
  }

  /**
   * Create a new Index.Page with parsed indices and headers.
   */
  def page(file: File, path: String, markdown: RootNode, properties: Map[String, String]): Page =
    Page(file, path, markdown, properties, indices(markdown), headers(markdown))

  /**
   * Create a tree of header refs from a parsed markdown page.
   */
  def headers(root: RootNode): Forest[Ref] = {
    Tree.hierarchy(headerRefs(root, group = None))(Ordering[Int].on[Ref](_.level))
  }

  /**
   * Extract refs from markdown headers.
   */
  private def headerRefs(root: RootNode, group: Option[String]): List[Ref] = {
    root.getChildren.asScala.toList.flatMap {
      case header: HeaderNode =>
        header.getChildren.asScala.toList.flatMap {
          case anchor: AnchorLinkSuperNode => List(Ref(header.getLevel, "#" + anchor.name, anchor.contents, group))
          case anchor: AnchorLinkNode      => List(Ref(header.getLevel, "#" + anchor.getName, new TextNode(anchor.getText), group))
          case _                           => Nil
        }
      case node: DirectiveNode if node.format == DirectiveNode.Format.ContainerBlock =>
        // TODO check whether my assumption that Container DirectiveNode's always contain RootNode's holds,
        // if so maybe move that cast to DirectiveNode
        val newGroup = node.attributes.classes().asScala.find(_.startsWith("group-")).map(_.substring("group-".size))
        headerRefs(node.contentsNode.asInstanceOf[RootNode], newGroup)
      case _ => Nil
    }
  }

  /**
   * Create a tree of page refs from index directives in a parsed markdown page.
   */
  def indices(root: RootNode): Forest[Ref] = {
    Tree.hierarchy(indexRefs(root))(Ordering[Int].on[Ref](_.level))
  }

  /**
   * Extract refs from 'index' directives.
   */
  private def indexRefs(root: RootNode): List[Ref] = {
    root.getChildren.asScala.toList.flatMap {
      case node: DirectiveNode if isIndexDirective(node) => listedRefs(node)
      case _                                             => Nil
    }
  }

  /**
   * Determine whither this is an index directive, by name and format.
   */
  private def isIndexDirective(node: DirectiveNode): Boolean = {
    node.format == DirectiveNode.Format.ContainerBlock && node.name == "index"
  }

  /**
   * Extract refs from list items. Increment level at each list item.
   */
  private def listedRefs(node: Node, level: Int = 1): List[Ref] = {
    node.getChildren.asScala.toList.flatMap {
      case li: ListItemNode => linkRef(li, level).toList ++ listedRefs(li, level + 1)
      case other            => listedRefs(other, level)
    }
  }

  /**
   * Extract ref from nearest explicit link node.
   */
  @tailrec
  private def linkRef(node: Node, level: Int): Option[Ref] = {
    node match {
      case link: ExpLinkNode => Some(Ref(level, link.url, link.getChildren.get(0), group = None))
      case other => other.getChildren.asScala.toList match {
        // only check first children
        case first :: _ => linkRef(first, level)
        case _          => None
      }
    }
  }

  /**
   * Link together pages into trees using parsed indices.
   */
  def link(pages: List[Page], properties: Map[String, String]): Forest[Page] = {
    // Substitute all variables in index page links first
    val substituted = pages.map { page =>
      page.copy(indices = page.indices.map {
        case Tree.Node(label, children) =>
          val newPath = Writer.substituteVarsInString(label.path, properties ++ page.properties)
          Tree.Node(label.copy(path = newPath), children)
        case other => other
      })
    }
    Tree.link(substituted, links(substituted))
  }

  /**
   * Exception thrown for unknown pages in index links.
   */
  class LinkException(message: String) extends RuntimeException(message)

  /**
   * Find links between pages using parsed indices.
   */
  def links(pages: List[Page]): Map[Page, List[Page]] = {
    import scala.collection.mutable

    val edges = mutable.Map.empty[Page, List[Page]].withDefaultValue(Nil)
    val pageMap = (pages map { page => page.path -> page }).toMap

    def lookup(current: String, path: String) = {
      pageMap.get(Path.resolve(current, path)).getOrElse {
        throw new LinkException(s"Unknown page [$path] linked from [$current]")
      }
    }

    def add(path: String, page: Page, indices: Forest[Ref], nested: Boolean): Unit = {
      // if nested then prepending children, so process this level in reverse to retain order
      (if (nested) indices.reverse else indices) foreach { i =>
        val child = lookup(path, i.label.path)
        val current = edges(page)
        // nested links have priority (being further up the overall hierarchy)
        val added = if (nested) child :: current else current ::: List(child)
        edges += page -> added
        add(path, child, i.children, nested = true)
      }
    }

    pages foreach { page => add(page.path, page, page.indices, nested = false) }
    edges.toMap.withDefaultValue(Nil)
  }

}
