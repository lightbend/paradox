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

import java.net.URI

import com.lightbend.paradox.markdown.Writer.Context
import com.lightbend.paradox.tree.Tree
import org.pegdown.{ LinkRenderer, Printer, ToHtmlSerializer }
import org.pegdown.ast.{ AnchorLinkNode, AnchorLinkSuperNode, AutoLinkNode, DirectiveNode, ExpImageNode, ExpLinkNode, HeaderNode, MailLinkNode, Node, RefImageNode, RefLinkNode, TextNode, Visitor, WikiLinkNode }
import org.pegdown.plugins.ToHtmlSerializerPlugin

import scala.collection.JavaConverters._

object SinglePageSupport {

  def writer: Writer = new Writer(new SinglePageToHtmlSerializer(_))

  def defaultPlugins(directives: Seq[Context => Directive]): Seq[Context => ToHtmlSerializerPlugin] =
    Writer.defaultPlugins(directives).map { plugin =>
      { context: Context =>
        plugin(context) match {
          case _: AnchorLinkSerializer => new SinglePageAnchorLinkSerializer(context)
          case other                   => other
        }
      }
    }

  def defaultDirectives: Seq[Context => Directive] = Writer.defaultDirectives.map { directive =>
    { context: Context =>
      directive(context) match {
        case ref: RefDirective => new SinglePageRefDirective(ref)
        case toc: TocDirective => new SinglePageTocDirective(toc)
        case other             => other
      }
    }
  }

  def defaultLinks: Context => LinkRenderer = c => new SinglePageLinkRenderer(c, Writer.defaultLinks(c))

  class SinglePageRefDirective(refDirective: RefDirective) extends InlineDirective("ref", "ref:") with SourceDirective {

    override def ctx: Context = refDirective.ctx

    def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
      val source = resolvedSource(node, page)
      ctx.pageMappings(source).flatMap(path => check(node, path)) match {
        case Some(path) =>
          val resolved = URI.create(ctx.page.path).resolve(path).getPath
          val link = if (path.contains("#")) {
            val anchor = path.substring(path.lastIndexOf('#') + 1)
            s"#$resolved~$anchor"
          } else {
            s"#$resolved"
          }
          new ExpLinkNode("", link, node.contentsNode).accept(visitor)
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

  class SinglePageTocDirective(toc: TocDirective) extends Directive {
    override def names: Seq[String] = toc.names

    override def format: Set[DirectiveNode.Format] = toc.format

    override def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
      // Render nothing.
    }
  }

  class SinglePageLinkRenderer(ctx: Writer.Context, delegate: LinkRenderer) extends LinkRenderer {

    override def render(node: AutoLinkNode): LinkRenderer.Rendering = delegate.render(node)

    override def render(node: ExpImageNode, text: String): LinkRenderer.Rendering = {
      val uri = URI.create(node.url)
      val relativeToBase = if (uri.getAuthority == null) {
        val path = URI.create(ctx.page.path).resolve(uri).getPath
        new ExpImageNode(node.title, path, node.getChildren.get(0))
      } else node
      delegate.render(relativeToBase, text)
    }

    override def render(node: MailLinkNode): LinkRenderer.Rendering = delegate.render(node)

    override def render(node: RefLinkNode, url: String, title: String, text: String): LinkRenderer.Rendering =
      delegate.render(node, url, title, text)

    override def render(node: RefImageNode, url: String, title: String, alt: String): LinkRenderer.Rendering = {
      val uri = URI.create(url)
      println("Rendering image: " + uri)
      val relativeToBase = if (uri.getAuthority == null) {
        println("is relative")
        URI.create(ctx.page.path).resolve(uri).getPath
      } else url
      println("path: " + relativeToBase)
      delegate.render(node, relativeToBase, title, alt)
    }

    override def render(node: WikiLinkNode): LinkRenderer.Rendering = delegate.render(node)

    override def render(node: AnchorLinkNode): LinkRenderer.Rendering = {
      val name = s"${ctx.page.path}~${node.getName}"
      new LinkRenderer.Rendering(s"#$name", node.getText).withAttribute("name", name)
    }

    override def render(node: ExpLinkNode, text: String): LinkRenderer.Rendering = delegate.render(node, text)
  }

  class SinglePageAnchorLinkSerializer(ctx: Writer.Context) extends ToHtmlSerializerPlugin {
    def visit(node: Node, visitor: Visitor, printer: Printer): Boolean = node match {
      case anchor: AnchorLinkSuperNode =>
        val name = s"${ctx.page.path}~${anchor.name}"
        printer.print(s"""<a href="#$name" name="$name" class="anchor"><span class="anchor-link"></span></a><span class="header-title">""")
        anchor.getChildren.asScala.foreach(_.accept(visitor))
        printer.print("</span>")
        true
      case _ => false
    }
  }

  class SinglePageToHtmlSerializer(ctx: Writer.Context) extends ToHtmlSerializer(
    defaultLinks(ctx),
    Writer.defaultVerbatims.asJava,
    defaultPlugins(defaultDirectives).map(p => p(ctx)).asJava
  ) {

    override def visit(node: HeaderNode): Unit = {
      val offsetDepth = node.getLevel + ctx.location.depth

      def visitHeaderChildren(node: HeaderNode): Unit = {
        node.getChildren.asScala.toList match {
          case List(anchorLink: AnchorLinkNode, text: TextNode) =>
            linkRenderer.render(anchorLink)
            printer.print("""<span class="header-title">""")
            text.accept(this)
            printer.print("</span>")
          case List(anchorLink: AnchorLinkSuperNode) =>
            anchorLink.accept(this)
          case other =>
            ctx.logger.warn("Rendering header that isn't an anchor link followed by text, or anchor link supernode, it will not have its content wrapped in a header-title span, and so won't be numbered: " + other)
            visitChildren(node)
        }
      }
      if (offsetDepth > 6) {
        printer.println().print("<div class=\"h").print(offsetDepth.toString).print("\">")
        visitHeaderChildren(node)
        printer.print("</div>").println()
      } else {
        printer.println().print("<h").print(offsetDepth.toString).print('>')
        visitHeaderChildren(node)
        printer.print("</h").print(offsetDepth.toString).print('>').println()
      }
    }

  }

  class SinglePageTableOfContents(maxDepth: Int = 6, maxExpandDepth: Option[Int] = None) extends TableOfContents(true, true, false, maxDepth, maxExpandDepth) {
    override protected def link[A <: Linkable](base: String, linkable: A, active: Option[Tree.Location[Page]]): Node = {
      val path = linkable match {
        case page: Page     => page.path
        case header: Header => header.path.replace('#', '~')
      }

      new ExpLinkNode("", "#" + base + path, linkable.label)
    }
  }

}
