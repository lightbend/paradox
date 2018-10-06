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
