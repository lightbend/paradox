/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

import com.typesafe.paradox.tree.Tree.Location
import org.pegdown.ast._
import org.pegdown.ast.DirectiveNode.Format._
import org.pegdown.plugins.ToHtmlSerializerPlugin
import org.pegdown.Printer

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
    val ordered = node.attributes.booleanValue("ordered", true)
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
