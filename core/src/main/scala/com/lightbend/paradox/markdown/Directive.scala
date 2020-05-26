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

import com.lightbend.paradox.tree.Tree.{ Forest, Location }
import java.io.{ File, FileNotFoundException }
import java.util.Optional

import com.lightbend.paradox.markdown.Snippet.SnippetException
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

object Directive {
  def filterLabels(prefix: String, attributes: DirectiveAttributes, labels: Seq[String], properties: Map[String, String]): Boolean = attributes.value("filterLabels", "") match {
    case "true" | "on" | "yes"  => true
    case "false" | "off" | "no" => false
    case ""                     => labels.nonEmpty && properties.get(s"$prefix.filterLabels").forall(_ == "true")
  }

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
trait SourceDirective {
  this: Directive =>
  def ctx: Writer.Context

  final def page: Page = ctx.location.tree.label

  final def variables: Map[String, String] = ctx.properties

  protected def title(node: DirectiveNode, page: Page): String = ""

  protected def resolvedSource(node: DirectiveNode, page: Page): String =
    extractLink(node, page)

  protected def extractLink(node: DirectiveNode, page: Page): String = {
    def ref(key: String) =
      referenceMap.get(key.filterNot(_.isWhitespace).toLowerCase).map(_.getUrl).getOrElse {
        ctx.error(s"Undefined reference key [$key]", node)
        ""
      }

    Writer.substituteVarsInString(node.source match {
      case x: DirectiveNode.Source.Direct => x.value
      case x: DirectiveNode.Source.Ref    => ref(x.value)
      case DirectiveNode.Source.Empty     => ref(node.label)
    }, variables)
  }

  protected def resolveFile(propPrefix: String, source: String, page: Page, variables: Map[String, String]): File =
    SourceDirective.resolveFile(propPrefix, source, page.file, variables)

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

object SourceDirective {
  def resolveFile(propPrefix: String, source: String, pageFile: File, variables: Map[String, String]): File =
    source match {
      case s if s startsWith "$" =>
        val baseKey = s.drop(1).takeWhile(_ != '$')
        val base = new File(PropertyUrl(s"$propPrefix.$baseKey.base_dir", variables.get).base.trim)
        val effectiveBase = if (base.isAbsolute) base else new File(pageFile.getParentFile, base.toString)
        new File(effectiveBase, s.drop(baseKey.length + 2))
      case s if s startsWith "/" =>
        val base = new File(PropertyUrl(SnipDirective.buildBaseDir, variables.get).base.trim)
        new File(base, s)
      case s =>
        new File(pageFile.getParentFile, s)
    }
}

// Default directives

/**
 * Ref directive.
 *
 * Refs are for links to internal pages. The file extension is replaced when rendering.
 * Links are validated to ensure they point to a known page.
 */
case class RefDirective(ctx: Writer.Context)
  extends InlineDirective("ref", "ref:") with SourceDirective {

  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    val source = resolvedSource(node, page)
    ctx.pageMappings(source).flatMap(path => check(node, path)) match {
      case Some(path) =>
        new ExpLinkNode("", path, node.contentsNode).accept(visitor)
      case None =>
        ctx.error(s"Unknown page [$source]", node)
    }
  }

  private def check(node: DirectiveNode, path: String): Option[String] = {
    ctx.paths.get(Path.resolve(page.path, path)).map { target =>
      if (path.contains("#")) {
        val anchor = path.substring(path.lastIndexOf('#'))
        val headers = (target.headers.flatMap(_.toSet) :+ target.h1).map(_.path) ++ target.anchors.map(_.path)
        if (!headers.contains(anchor)) {
          ctx.error(s"Unknown anchor [$path]", node)
        }
      }
      path
    }
  }
}

object RefDirective {

  def isRefDirective(node: DirectiveNode): Boolean = {
    node.format == DirectiveNode.Format.Inline && (node.name == "ref" || node.name == "ref:")
  }

}

case class LinkDirective(ctx: Writer.Context)
  extends InlineDirective("link", "link:") with SourceDirective {

  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit =
    new ExpLinkNodeExtended(node.label, resolvedSource(node, page), node.contentsNode, node.attributes).accept(visitor)

}

object LinkDirective {
  def isLinkDirective(node: DirectiveNode): Boolean = {
    node.format == DirectiveNode.Format.Inline && (node.name == "link" || node.name == "link:")
  }
}

/**
 * Link to external sites using URI templates.
 */
abstract class ExternalLinkDirective(names: String*)
  extends InlineDirective(names: _*) with SourceDirective {

  protected def resolveLink(node: DirectiveNode, location: String): Url

  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit =
    new ExpLinkNode(title(node, page), resolvedSource(node, page), linkContents(node)).accept(visitor)

  protected def linkContents(node: DirectiveNode): Node = node.contentsNode

  override protected def resolvedSource(node: DirectiveNode, page: Page): String = {
    val link = super.resolvedSource(node, page)
    try {
      val resolvedLink = resolveLink(node: DirectiveNode, link).base.normalize.toString
      if (resolvedLink startsWith (".../")) page.base + resolvedLink.drop(4) else resolvedLink
    } catch {
      case e @ Url.Error(reason) =>
        ctx.logger.debug(e)
        ctx.error(s"Failed to resolve [$link] because $reason", node)
        ""
      case e: FileNotFoundException =>
        ctx.logger.debug(e)
        ctx.error(s"Failed to resolve [$link] to a file: ${e.getMessage}", node)
        ""
      case e: Snippet.SnippetException =>
        ctx.logger.debug(e)
        ctx.error(s"Failed to resolve [$link]: ${e.getMessage}", node)
        ""
    }
  }
}

/**
 * ExtRef directive.
 *
 * Link to external pages using URL templates.
 */
case class ExtRefDirective(ctx: Writer.Context)
  extends ExternalLinkDirective("extref", "extref:") {

  def resolveLink(node: DirectiveNode, link: String): Url = {
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
abstract class ApiDocDirective(name: String)
  extends ExternalLinkDirective(name, name + ":") {

  protected def resolveApiLink(link: String): Url

  val defaultBaseUrl = PropertyUrl(name + ".base_url", variables.get)
  val ApiDocProperty = raw"""$name\.(.*)\.base_url""".r
  val baseUrls = variables.collect {
    case (property @ ApiDocProperty(pkg), url) => (pkg, PropertyUrl(property, variables.get))
  }

  override protected def linkContents(node: DirectiveNode): Node = new CodeNode(node.contents)

  override protected def title(node: DirectiveNode, page: Page): String = {
    val link = extractLink(node, page)
    try {
      val url = Url(link)
      val path = url.base.getPath
      if (path.endsWith("$")) path.substring(0, path.length - 1)
      else if (path.endsWith(".package")) path.substring(0, path.length - ".package".length)
      else if (path.endsWith(".index")) path.substring(0, path.length - ".index".length)
      // for inner-class notation with $$
      else path.replaceAll("\\$\\$", ".")
    } catch {
      case e @ Url.Error(reason) =>
        ctx.logger.debug(e)
        ctx.error(s"Failed to resolve [$link] because $reason", node)
        ""
    }
  }

  override protected def resolveLink(node: DirectiveNode, link: String): Url = {
    val resolvedLink = resolveApiLink(link)
    val resolvedPath = resolvedLink.base.getPath
    if (resolvedPath startsWith ".../") resolvedLink.copy(path = page.base + resolvedPath.drop(4)) else resolvedLink
  }

}

object ApiDocDirective {
  /** This relies on the naming convention of packages being all-ascii-lowercase (which is rarely broken), numbers and underscore. */
  def packageDotsToSlash(s: String) = s.replaceAll("(\\b[a-z][a-z0-9_]*)\\.", "$1/")
}

case class ScaladocDirective(ctx: Writer.Context)
  extends ApiDocDirective("scaladoc") {

  protected def resolveApiLink(link: String): Url = {
    val levels = link.split("[.]")
    val packages = (1 to levels.init.size).map(levels.take(_).mkString("."))
    val baseUrl = packages.reverse.collectFirst(baseUrls).getOrElse(defaultBaseUrl).resolve()
    url(link, baseUrl)
  }

  private def classDotsToDollarDollar(s: String) = s.replaceAll("(\\b[A-Z].+)\\.", "$1\\$\\$")

  private def url(link: String, baseUrl: Url): Url = {
    val url = Url(link).base
    val path = classDotsToDollarDollar(ApiDocDirective.packageDotsToSlash(url.getPath)) + ".html"
    (baseUrl / path).withFragment(url.getFragment)
  }

}

object JavadocDirective {

  type LinkStyle = String
  val LinkStyleFrames = "frames"
  val LinkStyleDirect = "direct"

  // If Java 9+ we default to linking directly to the file, since it doesn't support frames, otherwise we default
  // to linking to the frames version with the class in the query parameter. Also, the version of everything up to
  // and including 8 starts with 1., so that's an easy way to tell if it's 9+ or not.
  val jdkDependentLinkStyle = if (sys.props.get("java.specification.version").exists(_.startsWith("1."))) LinkStyleFrames else LinkStyleDirect

  final val JavadocLinkStyleProperty = raw"""javadoc\.(.*).link_style""".r

  private[markdown] def url(link: String, baseUrl: Url, linkStyle: LinkStyle): Url = {
    val url = Url(link).base
    val path = ApiDocDirective.packageDotsToSlash(url.getPath) + ".html"
    linkStyle match {
      case LinkStyleFrames => baseUrl.withEndingSlash.withQuery(path).withFragment(url.getFragment)
      case LinkStyleDirect => (baseUrl / path).withFragment(url.getFragment)
    }
  }

}

case class JavadocDirective(ctx: Writer.Context)
  extends ApiDocDirective("javadoc") {

  import JavadocDirective._

  val rootLinkStyle = variables.getOrElse("javadoc.link_style", LinkStyleFrames)
  val javaLinkStyle = variables.getOrElse("javadoc.link_style", jdkDependentLinkStyle)

  val packageLinkStyle: Map[String, String] = Map("java" -> javaLinkStyle) ++ variables.collect {
    case (property @ JavadocLinkStyleProperty(pkg), url) => (pkg, variables(property))
  }

  override protected def resolveApiLink(link: String): Url = {
    val levels = link.split("[.]")
    val packages = (1 to levels.init.size).map(levels.take(_).mkString("."))
    val packagesDeepestFirst = packages.reverse
    val baseUrl = packagesDeepestFirst.collectFirst(baseUrls).getOrElse(defaultBaseUrl).resolve()
    val linkStyle = packagesDeepestFirst.collectFirst(packageLinkStyle).getOrElse(rootLinkStyle)
    url(link, baseUrl, linkStyle)

  }
}

object GitHubResolver {
  val baseUrl = "github.base_url"
  val githubDomain = "github.domain"
}

trait GitHubResolver {

  def variables: Map[String, String]

  lazy val githubDomain = variables.get(GitHubResolver.githubDomain).getOrElse("github.com")
  val IssuesLink = """([^/]+/[^/]+)?#([0-9]+)""".r
  val CommitLink = """(([^/]+/[^/]+)?@)?(\p{XDigit}{5,40})""".r
  lazy val TreeUrl = (s"(.*$githubDomain/[^/]+/[^/]+/tree/[^/]+)").r
  lazy val ProjectUrl = (s"(.*$githubDomain/[^/]+/[^/]+).*").r

  val baseUrl = PropertyUrl(GitHubResolver.baseUrl, variables.get)

  protected def resolvePath(page: Page, source: String, labelOpt: Option[String]): Url = {
    val pathUrl = Url.parse(source, "path is invalid")
    val path = pathUrl.base.getPath
    val root = variables.get("github.root.base_dir") match {
      case None      => throw Url.Error("[github.root.base_dir] is not defined")
      case Some(dir) => new File(dir)
    }
    val file = path match {
      case p if p.startsWith(Path.toUnixStyleRootPath(root.getAbsolutePath)) => new File(p)
      case p if p.startsWith("/")                                            => new File(root, path.drop(1))
      case p                                                                 => new File(page.file.getParentFile, path)
    }
    val labelFragment =
      for {
        label <- labelOpt
        (min, max) <- Snippet.extractLabelRange(file, label)
      } yield {
        if (min == max)
          s"L$min"
        else
          s"L$min-L$max"
      }
    val fragment = labelFragment.getOrElse(pathUrl.base.getFragment)
    val treePath = Path.relativeLocalPath(root.getAbsolutePath, file.getAbsolutePath)

    (treeUrl / treePath) withFragment fragment
  }

  protected def resolveProject(project: String) = {
    Option(project) match {
      case Some(path) => Url(s"https://$githubDomain") / path
      case None       => projectUrl
    }
  }

  protected def projectUrl = baseUrl.collect {
    case ProjectUrl(url) => url
    case _               => throw Url.Error(s"[${GitHubResolver.baseUrl}] is not a project URL")
  }

  protected def treeUrl = baseUrl.collect {
    case TreeUrl(url)    => url
    case ProjectUrl(url) => url + "/tree/master"
    case _               => throw Url.Error(s"[${GitHubResolver.baseUrl}] is not a project or versioned tree URL")
  }

}

/**
 * GitHub directive.
 *
 * Link to GitHub project entities like issues, commits and source code.
 * Supports most of the references documented in:
 * https://help.github.com/articles/autolinked-references-and-urls/
 */
case class GitHubDirective(ctx: Writer.Context)
  extends ExternalLinkDirective("github", "github:") with GitHubResolver {

  def resolveLink(node: DirectiveNode, link: String): Url = {
    link match {
      case IssuesLink(project, issue)     => resolveProject(project) / "issues" / issue
      case CommitLink(_, project, commit) => resolveProject(project) / "commit" / commit
      case path                           => resolvePath(page, path, Option(node.attributes.identifier()))
    }
  }

}

/**
 * Snip directive.
 *
 * Extracts snippets from source files into verbatim blocks.
 */
case class SnipDirective(ctx: Writer.Context)
  extends LeafBlockDirective("snip") with SourceDirective with GitHubResolver {

  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    try {
      val labels = node.attributes.values("identifier").asScala
      val source = resolvedSource(node, page)
      val filterLabels = Directive.filterLabels("snip", node.attributes, labels.toSeq, variables)
      val file = resolveFile("snip", source, page, variables)
      val (text, snippetLang) = Snippet(file, labels.toSeq, filterLabels)
      val lang = Option(node.attributes.value("type")).getOrElse(snippetLang)
      val group = Option(node.attributes.value("group")).getOrElse("")
      val sourceUrl = if (variables.contains(GitHubResolver.baseUrl) && variables.getOrElse(SnipDirective.showGithubLinks, "false") == "true") {
        Optional.of(resolvePath(page, Path.toUnixStyleRootPath(file.getAbsolutePath), labels.headOption).base.normalize.toString)
      } else Optional.empty[String]()
      new VerbatimGroupNode(text, lang, group, node.attributes.classes, sourceUrl).accept(visitor)
    } catch {
      case e: FileNotFoundException =>
        ctx.logger.debug(e)
        ctx.error("Could not find file for snippet", node)
      case e: SnippetException =>
        ctx.logger.debug(e)
        ctx.error(e.getMessage, node)
    }
  }

}

object SnipDirective {

  val showGithubLinks = "snip.github_link"
  val buildBaseDir = "snip.build.base_dir"

}

/**
 * Fiddle directive.
 *
 * Extracts fiddles from source files into fiddle blocks.
 */
case class FiddleDirective(ctx: Writer.Context)
  extends LeafBlockDirective("fiddle") with SourceDirective {

  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    try {
      val labels = node.attributes.values("identifier").asScala

      val integrationScriptUrl =
        node.attributes.value("integrationScriptUrl", "https://embed.scalafiddle.io/integration.js")

      // integration params as listed here:
      // https://github.com/scalafiddle/scalafiddle-core/tree/master/integrations#scalafiddle-integration
      // 'selector' is excluded on purpose to not complicate logic and increase maintainability
      val validParams = Seq("prefix", "dependency", "scalaversion", "template", "theme", "minheight", "layout")

      val params = validParams.map(k => Option(node.attributes.value(k)).map { x =>
        if (x.startsWith("'") && x.endsWith("'")) // earlier explicit ' was required to quote attributes (now all are quoted with ")
          s"""data-$k="${x.substring(1, x.length - 1)}" """
        else
          s"""data-$k="$x" """
      }.getOrElse("")).mkString(" ")

      val source = resolvedSource(node, page)
      val file = resolveFile("fiddle", source, page, variables)
      val filterLabels = Directive.filterLabels("fiddle", node.attributes, labels.toSeq, variables)
      val (code, _) = Snippet(file, labels.toSeq, filterLabels)

      printer.println.print(
        s"""
        <div data-scalafiddle="true" $params>
          <pre class="prettyprint"><code class="language-scala">$code</code></pre>
        </div>
        <script defer="true" src="$integrationScriptUrl"></script>
        """
      )
    } catch {
      case e: FileNotFoundException =>
        ctx.logger.debug(e)
        ctx.error("Could not find file for fiddle", node)
      case e: SnippetException =>
        ctx.logger.debug(e)
        ctx.error(e.getMessage, node)
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
case class TocDirective(location: Location[Page], includeIndexes: List[Int]) extends LeafBlockDirective("toc") {
  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    val classes = node.attributes.classesString
    val depth = node.attributes.intValue("depth", 6)
    val pages = node.attributes.booleanValue("pages", true)
    val headers = node.attributes.booleanValue("headers", true)
    val ordered = node.attributes.booleanValue("ordered", false)
    val toc = new TableOfContents(pages, headers, ordered, depth)
    printer.println.print(s"""<div class="toc $classes">""")
    toc.markdown(location, node.getStartIndex, includeIndexes).accept(visitor)
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
case class InlineWrapDirective(typ: String) extends InlineDirective(Array(typ, typ.toUpperCase): _*) {

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

/**
 * Dependency directive.
 */
case class DependencyDirective(ctx: Writer.Context) extends LeafBlockDirective("dependency") {
  val VersionSymbol = "symbol"
  val VersionValue = "value"
  val ScalaBinaryVersionVar = "scala.binary.version"

  val variables = ctx.properties
  val ScalaVersion = variables.get("scala.version")
  val ScalaBinaryVersion = variables.get(ScalaBinaryVersionVar)

  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    node.contentsNode.getChildren.asScala.headOption match {
      case Some(text: TextNode) => renderDependency(text.getText, node, printer)
      case _                    => node.contentsNode.accept(visitor)
    }
  }

  def renderDependency(tools: String, node: DirectiveNode, printer: Printer): Unit = {
    val classes = Seq("dependency", node.attributes.classesString).filter(_.nonEmpty)

    val symbolPostfixes = node.attributes.keys().asScala.toSeq
      .filter(_.startsWith(VersionSymbol)).sorted.map(_.replace(VersionSymbol, ""))

    val dependencyPostfixes = node.attributes.keys().asScala.toSeq
      .filter(_.startsWith("group")).sorted.map(_.replace("group", ""))

    val startDelimiter = node.attributes.value("start-delimiter", "$")
    val stopDelimiter = node.attributes.value("stop-delimiter", "$")
    val scalaBinaryVersionVarUse = startDelimiter + ScalaBinaryVersionVar + stopDelimiter

    def coordinate(name: String): Option[String] =
      Option(node.attributes.value(name)).map { value =>
        variables.foldLeft(value) {
          case (str, (key, value)) =>
            str.replace(startDelimiter + key + stopDelimiter, value)
        }
      }

    def requiredCoordinate(name: String): String =
      coordinate(name).getOrElse {
        ctx.error(s"'$name' is not defined", node)
        ""
      }

    def requiredCoordinateRaw(name: String): String =
      Option(node.attributes.value(name)).getOrElse {
        ctx.error(s"'$name' is not defined", node)
        ""
      }

    val showSymbolScalaBinary =
      dependencyPostfixes.exists { dp =>
        requiredCoordinateRaw(s"artifact$dp").endsWith(scalaBinaryVersionVarUse)
      }

    val symbols = symbolPostfixes.map { sp =>
      requiredCoordinate(VersionSymbol + sp)
    }.toSet

    def sbt(group: String, artifact: String, version: String, scope: Option[String], classifier: Option[String]): String = {
      val scopeString = scope.map {
        case s @ ("runtime" | "compile" | "test") => " % " + s.capitalize
        case s                                    => s""" % "$s""""
      }
      val classifierString = classifier.map(" classifier " + '"' + _ + '"')
      val extra = (scopeString ++ classifierString).mkString
      val versionOrSymbol = if (symbols.contains(version)) version else s""""$version""""
      (ScalaVersion, ScalaBinaryVersion) match {
        case (Some(scalaVersion), _) if artifact.endsWith("_" + scalaVersion) =>
          val strippedArtifact = artifact.substring(0, artifact.length - 1 - scalaVersion.length)
          s""""$group" % "$strippedArtifact" % ${versionOrSymbol}$extra cross CrossVersion.full"""

        case (_, Some(scalaBinVersion)) if artifact.endsWith("_" + scalaBinVersion) =>
          val strippedArtifact = artifact.substring(0, artifact.length - 1 - scalaBinVersion.length)
          s""""$group" %% "$strippedArtifact" % ${versionOrSymbol}$extra"""

        case _ =>
          s""""$group" % "$artifact" % $versionOrSymbol$extra"""
      }
    }

    def gradle(group: String, artifact: String, rawArtifact: String, version: String, scope: Option[String], classifier: Option[String]): String = {
      val artifactName = ScalaBinaryVersion match {
        case Some(v) if (rawArtifact.endsWith(scalaBinaryVersionVarUse)) =>
          "\"" + artifact.substring(0, artifact.length - v.length) + "${versions.ScalaBinary}\""
        case _ => s"'$artifact'"
      }
      val conf = scope.getOrElse("compile")
      val extra = classifier.map(c => s", classifier: '$c'").getOrElse("")
      val v = if (symbols.contains(version)) s"versions.$version" else s"'$version'"
      s"""$conf group: '$group', name: $artifactName, version: $v$extra""".stripMargin
    }

    def mvn(group: String, artifact: String, rawArtifact: String, version: String, scope: Option[String], classifier: Option[String]): String = {
      val artifactName = ScalaBinaryVersion match {
        case Some(v) if rawArtifact.endsWith(scalaBinaryVersionVarUse) =>
          artifact.substring(0, artifact.length - v.length) + "${scala.binary.version}"
        case _ => artifact
      }

      val elements =
        Seq("groupId" -> group, "artifactId" -> artifactName, "version" -> {
          if (symbols.contains(version)) s"$${${dotted(version)}}" else version
        }) ++
          classifier.map("classifier" -> _) ++ scope.map("scope" -> _)
      elements.map {
        case (element, value) => s"  <$element>$value</$element>"
      }.mkString("<dependency>\n", "\n", "\n</dependency>").replace("<", "&lt;").replace(">", "&gt;")
    }

    printer.print(s"""<dl class="${classes.mkString(" ")}">""")
    tools.split("[,]").map(_.trim).filter(_.nonEmpty).foreach { tool =>
      val (lang, code) = tool match {
        case "sbt" =>
          val symbolVals = symbolPostfixes.map { sp =>
            s"""val ${requiredCoordinate(VersionSymbol + sp)} = "${requiredCoordinate(VersionValue + sp)}"\n"""
          }.mkString
          val artifacts = dependencyPostfixes.map { dp =>
            sbt(
              requiredCoordinate(s"group$dp"),
              requiredCoordinate(s"artifact$dp"),
              requiredCoordinate(s"version$dp"),
              coordinate(s"scope$dp"),
              coordinate(s"classifier$dp")
            )
          }

          val libraryDependencies = artifacts match {
            case Seq(artifact) => s"libraryDependencies += $artifact"
            case artifacts =>
              Seq("libraryDependencies ++= Seq(", artifacts.map(a => s"  $a").mkString(",\n"), ")").mkString("\n")
          }

          ("scala", symbolVals + libraryDependencies)

        case "gradle" | "Gradle" =>
          val scalaBinaryVersionProperties =
            if (showSymbolScalaBinary) ScalaBinaryVersion.flatMap { v =>
              Some(s"""  ScalaBinary: "$v"""")
            }
            else None
          val symbolProperties = if (scalaBinaryVersionProperties.isEmpty && symbols.isEmpty) "" else
            (symbolPostfixes.map { sp =>
              s"""  ${requiredCoordinate(VersionSymbol + sp)}: "${requiredCoordinate(VersionValue + sp)}""""
            } ++ scalaBinaryVersionProperties).mkString("versions += [\n", ",\n", "\n]\n")
          val artifacts = dependencyPostfixes.map { dp =>
            gradle(
              requiredCoordinate(s"group$dp"),
              requiredCoordinate(s"artifact$dp"),
              requiredCoordinateRaw(s"artifact$dp"),
              requiredCoordinate(s"version$dp"),
              coordinate(s"scope$dp"),
              coordinate(s"classifier$dp")
            )
          }

          val libraryDependencies =
            Seq("dependencies {", artifacts.map(a => s"  $a").mkString(",\n"), "}").mkString("\n")

          ("gradle", symbolProperties + libraryDependencies)

        case "maven" | "Maven" | "mvn" =>
          val scalaBinaryVersionProperties =
            if (showSymbolScalaBinary) ScalaBinaryVersion.flatMap { v =>
              Some(s"""  &lt;scala.binary.version&gt;$v&lt;/scala.binary.version&gt;""")
            }
            else None
          val symbolProperties = if (scalaBinaryVersionProperties.isEmpty && symbols.isEmpty) "" else
            (symbolPostfixes.map { sp =>
              val symb = s"""${dotted(requiredCoordinate(VersionSymbol + sp))}"""
              s"""  &lt;$symb&gt;${requiredCoordinate(VersionValue + sp)}&lt;/$symb&gt;"""
            } ++ scalaBinaryVersionProperties)
              .mkString("&lt;properties&gt;\n", "\n", "\n&lt;/properties&gt;\n")
          val artifacts = dependencyPostfixes.map { dp =>
            mvn(
              requiredCoordinate(s"group$dp"),
              requiredCoordinate(s"artifact$dp"),
              requiredCoordinateRaw(s"artifact$dp"),
              requiredCoordinate(s"version$dp"),
              coordinate(s"scope$dp"),
              coordinate(s"classifier$dp")
            )
          }

          ("xml", symbolProperties + artifacts.mkString("\n"))
      }

      printer.print(s"""<dt>$tool</dt>""")
      printer.print(s"""<dd>""")
      printer.print(s"""<pre class="prettyprint"><code class="language-$lang">$code</code></pre>""")
      printer.print(s"""</dd>""")
    }
    printer.print("""</dl>""")
  }

  def dotted(symbol: String): String = symbol.replaceAll("(.)([A-Z])", "$1.$2").toLowerCase
}

case class IncludeDirective(ctx: Writer.Context) extends LeafBlockDirective("include") with SourceDirective {

  override def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    throw new IllegalStateException("Include directive should have been handled in markdown preprocessing before render, but wasn't.")
  }
}
