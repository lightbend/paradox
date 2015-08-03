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
 * Render as a separate anchor link for section markers on headers.
 */
class AnchorLinkSerializer extends ToHtmlSerializerPlugin {
  def visit(node: Node, visitor: Visitor, printer: Printer): Boolean = node match {
    case anchor: AnchorLinkSuperNode =>
      printer.print(s"""<a href="#${anchor.name}" name="${anchor.name}" class="anchor"><span class="anchor-link"></span></a>""")
      anchor.getChildren.asScala.foreach(_.accept(visitor))
      true
    case _ => false
  }
}
