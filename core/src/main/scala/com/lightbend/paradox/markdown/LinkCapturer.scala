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
import java.net.URI

import org.pegdown.ast._
import org.pegdown.{ LinkRenderer, Printer, ToHtmlSerializer }

import scala.collection.JavaConverters._

/**
 * This captures links for validation.
 *
 * To validate, we parse all the markdown files again (we do this because we want to capture the original source of
 * the links so we can report meaningful source files and line numbers etc). Then, when converting it to HTML, we use
 * a special link renderer that captures links. The HTML itself generated during that process is never written
 * anywhere, it's just discarded.
 */
class LinkCapturer {

  /**
   * A number of paradox link nodes are synthetic - the paradox directives create them, consequently they don't have
   * the correct source file location information. So, when we render paradox link directives, we wrap them
   * in this, which overrides the node that the link capturer captures, so that we get the correct source file
   * location information.
   */
  private class NodeOverridingDirective(d: Directive) extends Directive {
    override def names: Seq[String] = d.names

    override def format: Set[DirectiveNode.Format] = d.format

    override def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
      withNodeOverride(node) {
        d.render(node, visitor, printer)
      }
    }
  }

  /**
   * This is used for ref links - we don't validate ref links, they are already validated by the paradox processor.
   */
  private class NonRenderingDirective(d: Directive) extends Directive {
    override def names: Seq[String] = d.names

    override def format: Set[DirectiveNode.Format] = d.format

    override def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = ()
  }

  private val plugins = Writer.defaultPlugins(Writer.defaultDirectives.map(_.andThen {
    case ext: ExternalLinkDirective => new NodeOverridingDirective(ext)
    case ref: RefDirective          => new NonRenderingDirective(ref)
    case other                      => other
  }))

  def serializer(context: Writer.Context): ToHtmlSerializer = new ToHtmlSerializer(
    new LinkCapturerRenderer(this, Writer.defaultLinks(context), context.page),
    Writer.defaultVerbatims.asJava,
    plugins.map(p => p(context)).asJava
  )

  private def withNodeOverride[T](node: Node)(block: => T): T = {
    nodeOverride match {
      case Some(existing) =>
        throw new IllegalStateException(s"Can't nest overridden nodes, existing: $existing, new: $node")
      case None =>
        nodeOverride = Some(node)
        try {
          block
        } finally {
          nodeOverride = None
        }
    }
  }

  def allLinks(siteBaseUrl: Option[URI]): List[CapturedLinkWithSources] = {
    links.collect {

      case (page, node, uri) if isPageInSite(page, uri) =>
        val path = node match {
          case d: DirectiveNode if d.name == "javadoc" && uri.getQuery != null =>
            uri.getPath + uri.getQuery
          case _ => uri.getPath
        }
        (CapturedRelativeLink(URI.create(page.path).resolve(path).getPath, Option(uri.getFragment)), page, node)

      case (page, node, uri) if uri.getAuthority != null =>
        val withScheme = if (uri.getScheme == null) {
          val scheme = siteBaseUrl.flatMap(u => Option(u.getScheme)).getOrElse("https")
          new URI(scheme, uri.getAuthority, uri.getPath, uri.getQuery, uri.getFragment)
        } else uri
        (CapturedAbsoluteLink(withScheme), page, node)

      case (page, node, uri) if uri.getPath != null && uri.getPath.startsWith("/") && siteBaseUrl.isDefined =>
        val bu = siteBaseUrl.get
        (CapturedAbsoluteLink(new URI(bu.getScheme, bu.getAuthority, uri.getPath, uri.getQuery, uri.getFragment)), page, node)

    }.groupBy(_._1).map {
      case (link, links) =>
        CapturedLinkWithSources(link, links.map {
          case (_, page, node) => (page.file, node)
        })
    }.toList
  }

  def isPageInSite(page: Page, uri: URI): Boolean = {
    if (uri.getAuthority == null && uri.getPath != null && !uri.getPath.startsWith("/")) {
      !URI.create(page.path).resolve(uri).getPath.startsWith("../")
    } else false
  }

  private var nodeOverride: Option[Node] = None
  private var links: List[(Page, Node, URI)] = Nil

  def capture(page: Page, node: Node, rendering: LinkRenderer.Rendering): LinkRenderer.Rendering = {
    links = (page, nodeOverride.getOrElse(node), URI.create(rendering.href)) :: links
    rendering
  }
}

case class CapturedLinkWithSources(link: CapturedLink, sources: List[(File, Node)])
sealed trait CapturedLink
case class CapturedAbsoluteLink(link: URI) extends CapturedLink
case class CapturedRelativeLink(path: String, fragment: Option[String]) extends CapturedLink

private class LinkCapturerRenderer(capturer: LinkCapturer, renderer: LinkRenderer, page: Page) extends LinkRenderer {
  private def capture(node: Node, rendering: LinkRenderer.Rendering) = capturer.capture(page, node, rendering)

  override def render(node: AutoLinkNode): LinkRenderer.Rendering = capture(node, renderer.render(node))

  override def render(node: ExpLinkNode, text: String): LinkRenderer.Rendering = capture(node, renderer.render(node, text))

  override def render(node: ExpImageNode, text: String): LinkRenderer.Rendering = capture(node, renderer.render(node, text))

  override def render(node: MailLinkNode): LinkRenderer.Rendering = capture(node, renderer.render(node))

  override def render(node: RefLinkNode, url: String, title: String, text: String): LinkRenderer.Rendering = capture(node, renderer.render(node, url, title, text))

  override def render(node: RefImageNode, url: String, title: String, alt: String): LinkRenderer.Rendering = capture(node, renderer.render(node, url, title, alt))

  override def render(node: WikiLinkNode): LinkRenderer.Rendering = capture(node, renderer.render(node))
}