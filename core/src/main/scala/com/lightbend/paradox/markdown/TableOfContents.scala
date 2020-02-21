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

import com.lightbend.paradox.tree.Tree
import com.lightbend.paradox.tree.Tree.{ Forest, Location }
import org.pegdown.ast._

/**
 * Create markdown list for table of contents on a page.
 */
class TableOfContents(pages: Boolean = true, headers: Boolean = true, ordered: Boolean = true, maxDepth: Int = 6, maxExpandDepth: Option[Int] = None) {

  /**
   * Create a TOC bullet list for a Page.
   */
  def markdown(location: Location[Page]): Node = {
    markdown(location.tree.label.base, Some(location), location.tree)
  }

  /**
   * Create a TOC bullet list for a TOC at a certain point within the section hierarchy.
   */
  @deprecated("0.5.1", "Use the includeIndexes variant to ensure it works with include files")
  def markdown(location: Location[Page], tocIndex: Int): Node = markdown(location, tocIndex, Nil)

  /**
   * Create a TOC bullet list for a TOC at a certain point within the section hierarchy.
   */
  def markdown(location: Location[Page], tocIndex: Int, includeIndexes: List[Int]): Node = {
    markdown(location.tree.label.base, Some(location), nested(location.tree, tocIndex, includeIndexes))
  }

  /**
   * Create a TOC bullet list for a Page tree, given the base path and active location.
   */
  def markdown(base: String, active: Option[Location[Page]], tree: Tree[Page]): Node = {
    subList(base, active, tree, depth = 0, expandDepth = None).getOrElse(list(Nil))
  }

  /**
   * Create a TOC bullet list from the root location.
   */
  def root(location: Location[Page]): Node = {
    markdown(location.tree.label.base, Some(location), location.root.tree)
  }

  /**
   * Create a TOC bullet list for the headers of a Page only, including the top-level header.
   */
  def headers(location: Location[Page]): Node = {
    val page = location.tree.label
    val tree = Tree.leaf(page.copy(headers = List(Tree(page.h1, page.headers))))
    markdown(base = page.base, active = None, tree)
  }

  /**
   * Create a new Page Tree for a TOC at a certain point within the section hierarchy.
   */
  private def nested(tree: Tree[Page], tocIndex: Int, includeIndexes: List[Int]): Tree[Page] = {
    val page = tree.label
    val (level, headers) = headersBelow(Location.forest(page.headers), tocIndex, includeIndexes)
    val subPages = if (level == 0) tree.children else Nil
    Tree(page.copy(headers = headers), subPages)
  }

  /**
   * Find the headers below the buffer index for a toc directive.
   * Return the level of the next header and sub-headers to render.
   */
  private def headersBelow(location: Option[Location[Header]], index: Int, includeIndexes: List[Int]): (Int, Forest[Header]) = location match {
    case Some(loc) =>
      if (isBelow(index, includeIndexes, loc.tree.label.label.getStartIndex, loc.tree.label.includeIndexes))
        (loc.depth, loc.tree :: loc.rights)
      else headersBelow(loc.next, index, includeIndexes)
    case None => (0, Nil)
  }

  private def isBelow(tocIndex: Int, tocIncludeIndexes: List[Int], headerIndex: Int, headerIncludeIndexes: List[Int]): Boolean = {
    // If the current level of include indexes are equal, then we need to recursively check the next level.
    // Otherwise, we compare the current level of include indexes if they exist, or the current indexes themselves.
    (tocIncludeIndexes, headerIncludeIndexes) match {
      case (i :: itail, h :: htail) if i == h => isBelow(tocIndex, itail, headerIndex, htail)
      case _ =>
        headerIncludeIndexes.headOption.getOrElse(headerIndex) > tocIncludeIndexes.headOption.getOrElse(tocIndex)
    }
  }

  private def subList[A <: Linkable](base: String, active: Option[Location[Page]], tree: Tree[A], depth: Int, expandDepth: Option[Int]): Option[Node] = {
    tree.label match {
      case page: Page =>
        val subHeaders = if (headers) items(base + page.path, active, page.headers, depth, expandDepth) else Nil
        val subPages = if (pages) items(base, active, tree.children, depth, expandDepth) else Nil
        optList(subHeaders ::: subPages)
      case header: Header =>
        val subHeaders = if (headers) items(base, active, tree.children, depth, expandDepth) else Nil
        optList(subHeaders)
    }
  }

  private def optList(items: List[Node]): Option[Node] = {
    if (items.isEmpty) None else Some(list(items))
  }

  private def list(items: List[Node]): Node = {
    val parent = new SuperNode
    items.foreach(parent.getChildren.add)
    if (ordered) new OrderedListNode(parent)
    else new BulletListNode(parent)
  }

  private def items[A <: Linkable](base: String, active: Option[Location[Page]], forest: Forest[A], depth: Int, expandDepth: Option[Int]): List[Node] = {
    forest map item(base, active, depth + 1, expandDepth.map(_ + 1))
  }

  private def item[A <: Linkable](base: String, active: Option[Location[Page]], depth: Int, expandDepth: Option[Int])(tree: Tree[A]): Node = {
    val linkable = tree.label
    val label = link(base, linkable, active)
    val parent = new SuperNode
    parent.getChildren.add(label)
    val autoExpandDepth = autoExpand(linkable, active, expandDepth)
    if ((depth < maxDepth) || autoExpandDepth.isDefined)
      subList(base, active, tree, depth, autoExpandDepth).foreach(parent.getChildren.add)
    new ListItemNode(parent)
  }

  private def autoExpand[A <: Linkable](linkable: A, active: Option[Location[Page]], expandDepth: Option[Int]): Option[Int] = {
    maxExpandDepth flatMap { max =>
      expandDepth.filter(_ < max) orElse // currently expanding and still below max
        (if (active.exists(_.path.drop(1).exists(_.tree.label == linkable))) Some(max) else None) orElse // expand ancestors of the active page
        (if ((max > 0) && active.exists(_.tree.label == linkable)) Some(0) else None) // expand from the active page
    }
  }

  protected def link[A <: Linkable](base: String, linkable: A, active: Option[Location[Page]]): Node = {
    val (path, classAttributes) = linkable match {
      case page: Page =>
        val isActive = active.exists(_.tree.label.path == page.path)
        (if (headers && isActive) (page.path + page.h1.path) else page.path, (if (isActive) List("active") else Nil) :+ "page")
      case header: Header => (header.path, List("header"))
    }
    new ClassyLinkNode(base + path, (classAttributes ++ linkable.group.map("group-" + _)).mkString(" "), linkable.label)
  }

}
