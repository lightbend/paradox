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

import com.lightbend.paradox.tree.Tree.Location
import java.io.{ File, FileNotFoundException }

import org.pegdown.ast._
import org.pegdown.ast.DirectiveNode.Format._
import org.pegdown.plugins.ToHtmlSerializerPlugin
import org.pegdown.{ Printer, ToHtmlSerializer }

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

/**
 * Directives with defined "source" semantics.
 */
sealed trait SourceDirective { this: Directive =>
  def page: Page

  protected def resolvedSource(node: DirectiveNode, page: Page): String = {
    def ref(key: String) =
      referenceMap.get(key.filterNot(_.isWhitespace).toLowerCase).map(_.getUrl).getOrElse(
        throw new RefDirective.LinkException(s"Undefined reference key [$key] in [${page.path}]"))
    node.source match {
      case x: DirectiveNode.Source.Direct => x.value
      case x: DirectiveNode.Source.Ref    => ref(x.value)
      case DirectiveNode.Source.Empty     => ref(node.label)
    }
  }

  private lazy val referenceMap: Map[String, ReferenceNode] = {
    val tempRoot = new RootNode
    tempRoot.setReferences(page.markdown.getReferences)
    var result = Map.empty[String, ReferenceNode]
    new ToHtmlSerializer(null) {
      toHtml(tempRoot)
      result = references.asScala.toMap
    }
    result
  }
}

// Default directives

/**
 * Ref directive.
 *
 * Refs are for links to internal pages. The file extension is replaced when rendering.
 * Links are validated to ensure they point to a known page.
 */
case class RefDirective(page: Page, pathExists: String => Boolean, convertPath: String => String)
    extends InlineDirective("ref", "ref:") with SourceDirective {

  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit =
    new ExpLinkNode("", check(convertPath(resolvedSource(node, page))), node.contentsNode).accept(visitor)

  private def check(path: String): String = {
    if (!pathExists(Path.resolve(page.path, path)))
      throw new RefDirective.LinkException(s"Unknown page [$path] referenced from [${page.path}]")
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
 * Link to external sites using URI templates.
 */
abstract class ExternalLinkDirective(names: String*) extends InlineDirective(names: _*) with SourceDirective {

  import ExternalLinkDirective._

  def resolveLink(location: String): Url

  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit =
    new ExpLinkNode("", resolvedSource(node, page), node.contentsNode).accept(visitor)

  override protected def resolvedSource(node: DirectiveNode, page: Page): String = {
    val link = super.resolvedSource(node, page)
    try {
      resolveLink(link).base.normalize.toString
    } catch {
      case Url.Error(reason) =>
        throw new LinkException(s"Failed to resolve [$link] referenced from [${page.path}] because $reason")
    }
  }
}

object ExternalLinkDirective {

  /**
   * Exception thrown for unknown or invalid links.
   */
  class LinkException(reason: String) extends RuntimeException(reason)

}

/**
 * ExtRef directive.
 *
 * Link to external pages using URL templates.
 */
case class ExtRefDirective(page: Page, variables: Map[String, String])
    extends ExternalLinkDirective("extref", "extref:") with SourceDirective {

  def resolveLink(link: String): Url = {
    link.split(":", 2) match {
      case Array(scheme, expr) => PropertyUrl(s"extref.$scheme.base_url", variables.get).format(expr)
      case _                   => throw Url.Error("URL has no scheme")
    }
  }

}

/**
 * API doc directive.
 *
 * Link to javadoc and scaladoc based on package prefix. Will match the
 * configured base URL with the longest package prefix. For example,
 * given:
 *
 * - `scaladoc.akka.base_url=http://doc.akka.io/api/akka/x.y.z`
 * - `scaladoc.akka.http.base_url=http://doc.akka.io/api/akka-http/x.y.z`
 *
 * Then `@scaladoc[Http](akka.http.scaladsl.Http)` will match the latter.
 */
abstract class ApiDocDirective(name: String, page: Page, variables: Map[String, String])
    extends ExternalLinkDirective(name, name + ":") {

  def resolveApiLink(base: Url, link: String): Url

  val defaultBaseUrl = PropertyUrl(name + ".base_url", variables.get)
  val ApiDocProperty = raw"""$name\.(.*)\.base_url""".r
  val baseUrls = variables.collect {
    case (property @ ApiDocProperty(pkg), url) => (pkg, PropertyUrl(property, variables.get))
  }

  def resolveLink(link: String): Url = {
    val levels = link.split("[.]")
    val packages = (1 to levels.init.size).map(levels.take(_).mkString("."))
    val baseUrl = packages.reverse.collectFirst(baseUrls).getOrElse(defaultBaseUrl)
    resolveApiLink(baseUrl.resolve, link)
  }

}

case class ScaladocDirective(page: Page, variables: Map[String, String])
    extends ApiDocDirective("scaladoc", page, variables) {

  def resolveApiLink(baseUrl: Url, link: String): Url = {
    variables.getOrElse("scaladoc.version", "???").split('.').take(2) match {
      case Array("2", "12") =>
        val url = Url(link).base
        val path = url.getPath.replace('.', '/') + ".html"
        baseUrl / path
      case _ =>
        baseUrl.withEndingSlash.withFragment(link)
    }
  }

}

case class JavadocDirective(page: Page, variables: Map[String, String])
    extends ApiDocDirective("javadoc", page, variables) {

  def resolveApiLink(baseUrl: Url, link: String): Url = {
    val url = Url(link).base
    val path = url.getPath.replace('.', '/') + ".html"
    baseUrl.withEndingSlash.withQuery(path).withFragment(url.getFragment)
  }

}

/**
 * GitHub directive.
 *
 * Link to GitHub project entities like issues, commits and source code.
 * Supports most of the references documented in:
 * https://help.github.com/articles/autolinked-references-and-urls/
 */
case class GitHubDirective(page: Page, variables: Map[String, String])
    extends ExternalLinkDirective("github", "github:") with SourceDirective {

  val IssuesLink = """([^/]+/[^/]+)?#([0-9]+)""".r
  val CommitLink = """(([^/]+/[^/]+)?@)?(\p{XDigit}{5,40})""".r
  val TreeUrl = """(.*github.com/[^/]+/[^/]+/tree/[^/]+)""".r
  val ProjectUrl = """(.*github.com/[^/]+/[^/]+).*""".r

  val baseUrl = PropertyUrl("github.base_url", variables.get)

  def resolveLink(link: String): Url = {
    link match {
      case IssuesLink(project, issue)     => resolveProject(project) / "issues" / issue
      case CommitLink(_, project, commit) => resolveProject(project) / "commit" / commit
      case _                              => treeUrl / link
    }
  }

  private def resolveProject(project: String) = {
    Option(project) match {
      case Some(path) => Url("https://github.com") / path
      case None       => projectUrl
    }
  }

  private def projectUrl = baseUrl.collect {
    case ProjectUrl(url) => url
    case _               => throw Url.Error("[github.base_url] is not a project URL")
  }

  private def treeUrl = baseUrl.collect {
    case TreeUrl(url)    => url
    case ProjectUrl(url) => url + "/tree/master"
    case _               => throw Url.Error("[github.base_url] is not a project or versioned tree URL")
  }

}

/**
 * Snip directive.
 *
 * Extracts snippets from source files into verbatim blocks.
 */
case class SnipDirective(page: Page, variables: Map[String, String])
    extends LeafBlockDirective("snip") with SourceDirective {

  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    try {
      val labels = node.attributes.values("identifier").asScala
      val source = resolvedSource(node, page)
      val file =
        if (source startsWith "$") {
          val baseKey = source.drop(1).takeWhile(_ != '$')
          val base = new File(PropertyUrl(s"snip.$baseKey.base_dir", variables.get).base.trim)
          val effectiveBase = if (base.isAbsolute) base else new File(page.file.getParentFile, base.toString)
          new File(effectiveBase, source.drop(baseKey.length + 2))
        } else new File(page.file.getParentFile, source)
      val text = Snippet(file, labels)
      val lang = Option(node.attributes.value("type")).getOrElse(Snippet.language(file))
      val group = Option(node.attributes.value("group")).getOrElse("")
      new VerbatimGroupNode(text, lang, group).accept(visitor)
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
case class FiddleDirective(page: Page, variables: Map[String, String])
    extends LeafBlockDirective("fiddle") with SourceDirective {

  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    try {
      val labels = node.attributes.values("identifier").asScala

      val baseUrl = node.attributes.value("baseUrl", "https://embed.scalafiddle.io/embed")
      val cssClass = node.attributes.value("cssClass", "fiddle")
      val width = Option(node.attributes.value("width")).map("width=" + _).getOrElse("")
      val height = Option(node.attributes.value("height")).map("height=" + _).getOrElse("")
      val extraParams = node.attributes.value("extraParams", "theme=light")
      val cssStyle = node.attributes.value("cssStyle", "overflow: hidden;")
      val source = resolvedSource(node, page)
      val file = if (source startsWith "$") {
        val baseKey = source.drop(1).takeWhile(_ != '$')
        val base = new File(PropertyUrl(s"fiddle.$baseKey.base_dir", variables.get).base.trim)
        val effectiveBase = if (base.isAbsolute) base else new File(page.file.getParentFile, base.toString)
        new File(effectiveBase, source.drop(baseKey.length + 2))
      } else new File(page.file.getParentFile, source)
      val text = Snippet(file, labels)
      val lang = Option(node.attributes.value("type")).getOrElse(Snippet.language(file))

      val fiddleSource = java.net.URLEncoder.encode(
        """
            |import fiddle.Fiddle, Fiddle.println
            | @scalajs.js.annotation.JSExport
            | object ScalaFiddle {
            |   // $FiddleStart
            |""".stripMargin + text + """
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

/**
 * Callout directive.
 *
 * Renders call-out divs.
 */
case class CalloutDirective(name: String, defaultTitle: String) extends ContainerBlockDirective(Array(name): _*) {
  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    val classes = node.attributes.classesString
    val title = node.attributes.value("title", defaultTitle)

    printer.print(s"""<div class="callout $name $classes">""")
    printer.print(s"""<div class="callout-title">$title</div>""")
    node.contentsNode.accept(visitor)
    printer.print("""</div>""")
  }
}

/**
 * Wrap directive.
 *
 * Wraps inner content in a `div` or `p`, optionally with custom `id` and/or `class` attributes.
 */
case class WrapDirective(typ: String) extends ContainerBlockDirective(Array(typ, typ.toUpperCase): _*) {
  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    val id =
      node.attributes.identifier match {
        case null => ""
        case x    => s""" id="$x""""
      }
    val classes =
      node.attributes.classesString match {
        case "" => ""
        case x  => s""" class="$x""""
      }
    printer.print(s"""<$typ$id$classes>""")
    node.contentsNode.accept(visitor)
    printer.print(s"</$typ>")
  }
}

/**
 * Inline wrap directive
 *
 * Wraps inner contents in a `span`, optionally with custom `id` and/or `class` attributes.
 */
case class InlineWrapDirective(typ: String) extends InlineDirective("span") {
  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    val id =
      node.attributes.identifier match {
        case null => ""
        case x    => s""" id="$x""""
      }
    val classes =
      node.attributes.classesString match {
        case "" => ""
        case x  => s""" class="$x""""
      }
    printer.print(s"""<$typ$id$classes>""")
    node.contentsNode.accept(visitor)
    printer.print(s"</$typ>")
  }
}

case class InlineGroupDirective(groups: Seq[String]) extends InlineDirective(groups: _*) {
  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    printer.print(s"""<span class="group-${node.name}">""")
    node.contentsNode.accept(visitor)
    printer.print(s"</span>")
  }
}
