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

import com.lightbend.paradox.tree.Tree
import com.lightbend.paradox.tree.Tree.Location
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
    ExtRefDirective(context.location.tree.label.path, context.properties),
    SnipDirective(context.location.tree.label),
    FiddleDirective(context.location.tree.label),
    TocDirective(context.location),
    VarDirective(context.properties),
    VarsDirective(context.properties)
  )

}
