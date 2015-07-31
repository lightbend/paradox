/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

import com.typesafe.paradox.tree.Tree.Location
import org.pegdown.ast._

/**
 * Create markdown list for breadcrumbs path to a page location.
 */
object Breadcrumbs {

  /**
   * Convert a location path into a markdown list.
   * Note: locations are ordered from current location up to root.
   */
  def markdown(locations: List[Location[Page]]): BulletListNode = locations match {
    case current :: parents => crumbs(current.tree.label.base, current.tree.label.path, locations.reverse)
    case _                  => list(Nil)
  }

  private def crumbs(base: String, active: String, locations: List[Location[Page]]): BulletListNode = {
    list(locations map item(base, active))
  }

  private def list(items: List[ListItemNode]): BulletListNode = {
    val parent = new SuperNode
    items.foreach(parent.getChildren.add)
    new BulletListNode(parent)
  }

  private def item(base: String, active: String)(location: Location[Page]): ListItemNode = {
    val page = location.tree.label
    val label = link(base, page.path, page.label, active)
    val parent = new SuperNode
    parent.getChildren.add(label)
    new ListItemNode(parent)
  }

  private def link(base: String, path: String, label: Node, active: String): Node = {
    if (path == active) new ActiveLinkNode(base + path, label)
    else new ExpLinkNode("", base + path, label)
  }

}
