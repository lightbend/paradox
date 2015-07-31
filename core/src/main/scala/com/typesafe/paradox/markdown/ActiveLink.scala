/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

import org.pegdown.ast._
import org.pegdown.plugins.ToHtmlSerializerPlugin
import org.pegdown.Printer
import scala.collection.JavaConverters._

/**
 * Serialize an ActiveLinkNode, adding the active class attribute.
 */
class ActiveLinkSerializer extends ToHtmlSerializerPlugin {
  def visit(node: Node, visitor: Visitor, printer: Printer): Boolean = node match {
    case link: ActiveLinkNode =>
      printer.print(s"""<a href="${link.href}" class="active">""")
      link.getChildren.asScala.foreach(_.accept(visitor))
      printer.print("</a>")
      true
    case _ => false
  }
}
