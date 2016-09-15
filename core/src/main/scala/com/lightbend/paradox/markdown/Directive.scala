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
import java.io.{ File, FileNotFoundException }

import org.pegdown.ast._
import org.pegdown.ast.DirectiveNode.Format._
import org.pegdown.plugins.ToHtmlSerializerPlugin
import org.pegdown.Printer

import scala.collection.JavaConverters._

/**
 * Serialize directives, checking the name and format against registered directives.
 */
class DirectiveSerializer(directives: Seq[Directive]) extends ToHtmlSerializerPlugin {
  val directiveMap = directives.flatMap(d => d.names.map(n => (n, d))).toMap

  def visit(node: Node, visitor: Visitor, printer: Printer): Boolean = node match {
    case dnode: DirectiveNode =>
      directiveMap.get(dnode.name) match {
        case Some(directive) if directive.format(dnode.format) =>
          directive.render(dnode, visitor, printer)
        case _ => // printer.print(s"<!-- $dnode -->")
      }
      true
    case _ => false
  }
}

// Directive plugins

/**
 * Base directive class, for directive specific serialization.
 */
abstract class Directive {
  def names: Seq[String]

  def format: Set[DirectiveNode.Format]

  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit
}

/**
 * Inline directive.
 */
abstract class InlineDirective(val names: String*) extends Directive {
  val format = Set(Inline)
}

/**
 * Leaf block directive.
 */
abstract class LeafBlockDirective(val names: String*) extends Directive {
  val format = Set(LeafBlock)
}

/**
 * Container block directive.
 */
abstract class ContainerBlockDirective(val names: String*) extends Directive {
  val format = Set(ContainerBlock)
}

// Default directives

/**
 * Ref directive.
 *
 * Refs are for links to internal pages. The file extension is replaced when rendering.
 * Links are validated to ensure they point to a known page.
 */
case class RefDirective(currentPath: String, pathExists: String => Boolean, convertPath: String => String) extends InlineDirective("ref", "ref:") {
  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    new ExpLinkNode("", check(convertPath(node.source)), node.contentsNode).accept(visitor)
  }

  private def check(path: String): String = {
    println("check path: " + path)
    if (!pathExists(Path.resolve(currentPath, path)))
      throw new RefDirective.LinkException(s"Unknown page [$path] referenced from [$currentPath]")
    path
  }
}

object RefDirective {

  /**
   * Exception thrown for unknown pages in reference links.
   */
  class LinkException(message: String) extends RuntimeException(message)

}

/**
 * Snip directive.
 *
 * Extracts snippets from source files into verbatim blocks.
 */
case class SnipDirective(page: Page) extends LeafBlockDirective("snip") {
  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    try {
      val labels = node.attributes.values("identifier").asScala
      val file = new File(page.file.getParentFile, node.source)
      val text = Snippet(file, labels)
      val lang = Option(node.attributes.value("type")).getOrElse(Snippet.language(file))
      new VerbatimNode(text, lang).accept(visitor)
    } catch {
      case e: FileNotFoundException =>
        throw new SnipDirective.LinkException(s"Unknown snippet [${e.getMessage}] referenced from [${page.path}]")
    }
  }
}

object SnipDirective {

  /**
   * Exception thrown for unknown snip links.
   */
  class LinkException(message: String) extends RuntimeException(message)

}

/**
 * Fiddle directive.
 *
 * Extracts fiddles from source files into fiddle blocks.
 */
case class FiddleDirective(page: Page) extends LeafBlockDirective("fiddle") {
  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    try {
      val labels = node.attributes.values("identifier").asScala

      val baseUrl = node.attributes.value("baseUrl", "https://embed.scalafiddle.io/embed")
      val cssClass = node.attributes.value("cssClass", "fiddle")
      val width = Option(node.attributes.value("width")).map("width=" + _).getOrElse("")
      val height = Option(node.attributes.value("height")).map("height=" + _).getOrElse("")
      val extraParams = node.attributes.value("extraParams", "theme=light")
      val cssStyle = node.attributes.value("cssStyle", "overflow: hidden;")

      val file = new File(page.file.getParentFile, node.source)
      val text = Snippet(file, labels)
      val lang = Option(node.attributes.value("type")).getOrElse(Snippet.language(file))

      val fiddleSource = java.net.URLEncoder.encode(
        """|
           | import fiddle.Fiddle, Fiddle.println
           | @scalajs.js.annotation.JSExport
           | object ScalaFiddle {
           |   // $FiddleStart
                  """ + text + """
           |   // $FiddleEnd
           | }
          """.stripMargin, "UTF-8")

      printer.println.print(s"""
        <iframe class="$cssClass" $width $height src="$baseUrl?$extraParams&source=$fiddleSource" frameborder="0" style="$cssStyle"></iframe>
        """
      )
    } catch {
      case e: FileNotFoundException =>
        throw new FiddleDirective.LinkException(s"Unknown fiddle [${e.getMessage}] referenced from [${page.path}]")
    }
  }
}

object FiddleDirective {

  /**
   * Exception thrown for unknown snip links.
   */
  class LinkException(message: String) extends RuntimeException(message)

}

/**
 * Table of contents directive.
 *
 * Placeholder to insert a serialized table of contents, using the page and header trees.
 * Depth and whether to include pages or headers can be specified in directive attributes.
 */
case class TocDirective(location: Location[Page]) extends LeafBlockDirective("toc") {
  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    val classes = node.attributes.classesString
    val depth = node.attributes.intValue("depth", 6)
    val pages = node.attributes.booleanValue("pages", true)
    val headers = node.attributes.booleanValue("headers", true)
    val ordered = node.attributes.booleanValue("ordered", false)
    val toc = new TableOfContents(pages, headers, ordered, depth)
    printer.println.print(s"""<div class="toc $classes">""")
    toc.markdown(location, node.getStartIndex).accept(visitor)
    printer.println.print("</div>")
  }
}

/**
 * Var directive.
 *
 * Looks up property values and renders escaped text.
 */
case class VarDirective(variables: Map[String, String]) extends InlineDirective("var", "var:") {
  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    new SpecialTextNode(variables.get(node.label).getOrElse(s"<${node.label}>")).accept(visitor)
  }
}

/**
 * Vars directive.
 *
 * Replaces property values in verbatim blocks.
 */
case class VarsDirective(variables: Map[String, String]) extends ContainerBlockDirective("vars") {
  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    import scala.collection.JavaConverters._
    node.contentsNode.getChildren.asScala.headOption match {
      case Some(verbatim: VerbatimNode) =>
        val startDelimiter = node.attributes.value("start-delimiter", "$")
        val stopDelimiter = node.attributes.value("stop-delimiter", "$")
        val text = variables.foldLeft(verbatim.getText) {
          case (str, (key, value)) =>
            str.replace(startDelimiter + key + stopDelimiter, value)
        }
        new VerbatimNode(text, verbatim.getType).accept(visitor)
      case _ => node.contentsNode.accept(visitor)
    }
  }
}
