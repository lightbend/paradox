/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

import com.typesafe.paradox.tree.Tree
import com.typesafe.paradox.tree.Tree.{ Forest, Location }
import org.pegdown.ast._

/**
 * Create markdown list for table of contents on a page.
 */
class TableOfContents(pages: Boolean = true, headers: Boolean = true, maxDepth: Int = 6) {

  /**
   * Create a TOC bullet list for a Page.
   */
  def markdown(location: Location[Page]): BulletListNode = {
    markdown(location.tree.label.base, location.tree.label.path, location.tree)
  }

  /**
   * Create a TOC bullet list for a TOC at a certain point within the section hierarchy.
   */
  def markdown(location: Location[Page], tocIndex: Int): BulletListNode = {
    markdown(location.tree.label.base, location.tree.label.path, nested(location.tree, tocIndex))
  }

  /**
   * Create a TOC bullet list for a Page tree, given the base and active paths.
   */
  def markdown(base: String, active: String, tree: Tree[Page]): BulletListNode = {
    subList(base, active, tree, depth = 0).getOrElse(list(Nil))
  }

  /**
   * Create a TOC bullet list from the root location.
   */
  def root(location: Location[Page]): BulletListNode = {
    markdown(location.tree.label.base, location.tree.label.path, location.root.tree)
  }

  /**
   * Create a new Page Tree for a TOC at a certain point within the section hierarchy.
   */
  private def nested(tree: Tree[Page], tocIndex: Int): Tree[Page] = {
    val page = tree.label
    val (level, headers) = headersBelow(Location.forest(page.headers), tocIndex)
    val subPages = if (level == 0) tree.children else Nil
    Tree(page.copy(headers = headers), subPages)
  }

  /**
   * Find the headers below the buffer index for a toc directive.
   * Return the level of the next header and sub-headers to render.
   */
  private def headersBelow(location: Option[Location[Header]], index: Int): (Int, Forest[Header]) = location match {
    case Some(loc) =>
      if (loc.tree.label.label.getStartIndex > index) (loc.depth, loc.tree :: loc.rights)
      else headersBelow(loc.next, index)
    case None => (0, Nil)
  }

  private def subList[A <: Linkable](base: String, active: String, tree: Tree[A], depth: Int): Option[BulletListNode] = {
    tree.label match {
      case page: Page =>
        val subHeaders = if (headers) items(base + page.path, active, page.headers, depth) else Nil
        val subPages = if (pages) items(base, active, tree.children, depth) else Nil
        optList(subHeaders ::: subPages)
      case header: Header =>
        val subHeaders = if (headers) items(base, active, tree.children, depth) else Nil
        optList(subHeaders)
    }
  }

  private def optList(items: List[ListItemNode]): Option[BulletListNode] = {
    if (items.isEmpty) None else Some(list(items))
  }

  private def list(items: List[ListItemNode]): BulletListNode = {
    val parent = new SuperNode
    items.foreach(parent.getChildren.add)
    new BulletListNode(parent)
  }

  private def items[A <: Linkable](base: String, active: String, forest: Forest[A], depth: Int): List[ListItemNode] = {
    forest map item(base, active, depth + 1)
  }

  private def item[A <: Linkable](base: String, active: String, depth: Int)(tree: Tree[A]): ListItemNode = {
    val linkable = tree.label
    val label = link(base, linkable.path, linkable.label, active)
    val parent = new SuperNode
    parent.getChildren.add(label)
    if (depth < maxDepth) subList(base, active, tree, depth).foreach(parent.getChildren.add)
    new ListItemNode(parent)
  }

  private def link(base: String, path: String, label: Node, active: String): Node = {
    if (path == active) new ActiveLinkNode(base + path, label)
    else new ExpLinkNode("", base + path, label)
  }

}
