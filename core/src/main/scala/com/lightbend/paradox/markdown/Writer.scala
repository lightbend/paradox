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
import org.pegdown.plugins.ToHtmlSerializerPlugin
import org.pegdown.ast.{ ExpImageNode, Node, RefImageNode, RootNode }
import org.pegdown.{ LinkRenderer, ToHtmlSerializer, VerbatimSerializer }
import scala.collection.JavaConverters._

/**
 * A configured markdown to HTML serializer.
 */
class Writer(serializer: Writer.Context => ToHtmlSerializer) {

  def this(
    linkRenderer:        Writer.Context => LinkRenderer                = Writer.defaultLinks,
    verbatimSerializers: Map[String, VerbatimSerializer]               = Writer.defaultVerbatims,
    serializerPlugins:   Seq[Writer.Context => ToHtmlSerializerPlugin] = Writer.defaultPlugins(Writer.defaultDirectives)) =
    this((context: Writer.Context) => new ToHtmlSerializer(
      linkRenderer(context),
      verbatimSerializers.asJava,
      serializerPlugins.map(p => p(context)).asJava))

  /**
   * Write main content.
   */
  def writeContent(node: Node, context: Writer.Context): String =
    writeFragment(node, context)

  /**
   * Write breadcrumbs fragment.
   */
  def writeBreadcrumbs(node: Node, context: Writer.Context): String =
    writeFragment(node, context)

  /**
   * Write navigation fragment.
   */
  def writeNavigation(node: Node, context: Writer.Context): String =
    writeFragment(node, context)

  /**
   * Write groups fragment.
   */
  def writeGroups(node: Node, context: Writer.Context): String =
    writeFragment(node, context)

  /**
   * Write navigation fragment.
   */
  def writeToc(node: Node, context: Writer.Context): String =
    writeFragment(node, context)

  /**
   * Write markdown to HTML, in the context of a page.
   */
  def write(markdown: RootNode, context: Writer.Context): String = {
    serializer(context).toHtml(markdown)
  }

  /**
   * Write a markdown fragment to HTML, in the context of a page.
   */
  def writeFragment(node: Node, context: Writer.Context): String = {
    val rootNode = new RootNode
    rootNode.getChildren.add(node)
    write(rootNode, context)
  }

}

object Writer {

  val DefaultSourceSuffix = ".md"
  val DefaultTargetSuffix = ".html"

  /**
   * Write context which is passed through to directives.
   */
  case class Context(
      location:     Location[Page],
      paths:        Set[String],
      pageMappings: String => String         = Path.replaceExtension(DefaultSourceSuffix, DefaultTargetSuffix),
      sourceSuffix: String                   = DefaultSourceSuffix,
      targetSuffix: String                   = DefaultTargetSuffix,
      groups:       Map[String, Seq[String]] = Map.empty,
      properties:   Map[String, String]      = Map.empty)

  def defaultLinks(context: Context): LinkRenderer =
    new DefaultLinkRenderer(context)

  def defaultVerbatims: Map[String, VerbatimSerializer] = {
    Map(
      VerbatimSerializer.DEFAULT -> PrettifyVerbatimSerializer,
      RawVerbatimSerializer.tag -> RawVerbatimSerializer
    )
  }

  def defaultPlugins(directives: Seq[Context => Directive]): Seq[Context => ToHtmlSerializerPlugin] = Seq(
    context => new ClassyLinkSerializer,
    context => new AnchorLinkSerializer,
    context => new DirectiveSerializer(directives.map(d => d(context)))
  )

  def defaultDirectives: Seq[Context => Directive] = Seq(
    context => RefDirective(context.location.tree.label, context.paths, context.pageMappings),
    context => ExtRefDirective(context.location.tree.label, context.properties),
    context => ScaladocDirective(context.location.tree.label, context.properties),
    context => JavadocDirective(context.location.tree.label, context.properties),
    context => GitHubDirective(context.location.tree.label, context.properties),
    context => SnipDirective(context.location.tree.label, context.properties),
    context => FiddleDirective(context.location.tree.label, context.properties),
    context => TocDirective(context.location),
    context => VarDirective(context.properties),
    context => VarsDirective(context.properties),
    context => CalloutDirective("note", "Note"),
    context => CalloutDirective("warning", "Warning"),
    context => WrapDirective("div"),
    context => InlineWrapDirective("span"),
    context => InlineGroupDirective(context.groups.values.flatten.map(_.toLowerCase).toSeq),
    context => DependencyDirective(context.properties)
  )

  class DefaultLinkRenderer(context: Context) extends LinkRenderer {
    private lazy val imgBase = {
      val root = context.location.tree.label.base // ends with a slash
      val base = context.properties.getOrElse("image.base_url", sys.error("Property `image.base_url` is not defined"))
      val baseUrl = if (base startsWith ".../") root + base.drop(4) else base
      if (baseUrl endsWith "/") baseUrl dropRight 1 else baseUrl
    }

    override def render(node: ExpImageNode, text: String): LinkRenderer.Rendering =
      interpolatedUrl(node.url) map { url =>
        super.render(new ExpImageNode(node.title, url, node.getChildren.get(0)), text)
      } getOrElse super.render(node, text)

    override def render(node: RefImageNode, url: String, title: String, alt: String): LinkRenderer.Rendering =
      super.render(node, interpolatedUrl(url) getOrElse url, title, alt)

    private def interpolatedUrl(url: String): Option[String] =
      if (url startsWith ".../") Some(imgBase + url.drop(3)) else None
  }
}
