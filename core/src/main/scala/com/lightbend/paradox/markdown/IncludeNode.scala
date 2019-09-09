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

import java.io.File
import java.util

import com.lightbend.paradox.PagedErrorContext
import com.lightbend.paradox.markdown.Writer.Context
import com.lightbend.paradox.tree.Tree
import com.lightbend.paradox.tree.Tree.Location
import org.pegdown.Printer
import org.pegdown.ast.{ AbstractNode, Node, RootNode, Visitor }
import org.pegdown.plugins.ToHtmlSerializerPlugin

case class IncludeNode(included: RootNode, includedFrom: File, includedFromPath: String) extends AbstractNode {
  override def accept(visitor: Visitor): Unit = visitor.visit(this)
  override def getChildren: util.List[Node] = included.getChildren
}

class IncludeNodeSerializer(context: Context) extends ToHtmlSerializerPlugin {
  override def visit(node: Node, visitor: Visitor, printer: Printer): Boolean = node match {
    case include @ IncludeNode(included, includedFrom, includedFromPath) =>
      // This location has no forest around it... which probably means that things like toc and navigation can't
      // be rendered inside snippets, which I'm ok with.
      val page = Page.included(includedFrom, includedFromPath, context.location.tree.label, included)
      val newLocation = Location(Tree.leaf(page), context.location.lefts, context.location.rights, context.location.parents)
      printer.print(context.writer.writeContent(included, context.copy(
        location = newLocation,
        includeIndexes = context.includeIndexes :+ include.getStartIndex,
        error = new PagedErrorContext(context.error, page)
      )))
      true
    case _ => false
  }
}