/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

import com.typesafe.paradox.tree.Tree
import com.typesafe.paradox.tree.Tree.Location
import org.pegdown.ast.{ Node, RootNode }
import org.pegdown.plugins.ToHtmlSerializerPlugin
import org.pegdown.{ LinkRenderer, ToHtmlSerializer, VerbatimSerializer }
import scala.collection.JavaConverters._

/**
 * A configured markdown to HTML serializer.
 */
class Writer(serializer: Writer.Context => ToHtmlSerializer) {

  def this(linkRenderer: LinkRenderer = Writer.defaultLinks,
           verbatimSerializers: Map[String, VerbatimSerializer] = Writer.defaultVerbatims,
           serializerPlugins: Writer.Context => Seq[ToHtmlSerializerPlugin] = (context: Writer.Context) => Writer.defaultPlugins(context)) =
    this((context: Writer.Context) => new ToHtmlSerializer(
      linkRenderer,
      verbatimSerializers.asJava,
      serializerPlugins(context).asJava))

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
    location: Location[Page],
    paths: Set[String],
    sourceSuffix: String = DefaultSourceSuffix,
    targetSuffix: String = DefaultTargetSuffix,
    properties: Map[String, String] = Map.empty)

  def defaultLinks: LinkRenderer = {
    new LinkRenderer
  }

  def defaultVerbatims: Map[String, VerbatimSerializer] = {
    Map(VerbatimSerializer.DEFAULT -> PrettifyVerbatimSerializer)
  }

  def defaultPlugins(context: Context): Seq[ToHtmlSerializerPlugin] = Seq(
    new ActiveLinkSerializer,
    new AnchorLinkSerializer,
    new DirectiveSerializer(defaultDirectives(context))
  )

  def defaultDirectives(context: Context): Seq[Directive] = Seq(
    RefDirective(context.location.tree.label.path, context.paths, Path.replaceExtension(context.sourceSuffix, context.targetSuffix)),
    TocDirective(context.location),
    VarDirective(context.properties),
    VarsDirective(context.properties)
  )

}
