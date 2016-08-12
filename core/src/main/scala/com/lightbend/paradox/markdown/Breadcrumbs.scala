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

package com.lightbend.paradox.markdown

import com.lightbend.paradox.tree.Tree.Location
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
    if (path == active) label // no link for current location
    else new ExpLinkNode("", base + path, label)
  }

}
