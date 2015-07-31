/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

import org.pegdown.ast._
import org.pegdown.plugins.ToHtmlSerializerPlugin
import org.pegdown.Printer
import scala.collection.JavaConverters._

/**
 * Serialize an AnchorLinkSuperNode, which can contain multiple children.
 */
class AnchorLinkSerializer extends ToHtmlSerializerPlugin {
  def visit(node: Node, visitor: Visitor, printer: Printer): Boolean = node match {
    case anchor: AnchorLinkSuperNode =>
      printer.print(s"""<a href="#${anchor.name}" name="${anchor.name}">""")
      anchor.getChildren.asScala.foreach(_.accept(visitor))
      printer.print("</a>")
      true
    case _ => false
  }
}
