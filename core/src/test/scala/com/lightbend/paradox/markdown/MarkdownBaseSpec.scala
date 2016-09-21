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

import com.lightbend.paradox.tree.Tree.{ Forest, Location }
import java.io.File
import org.scalatest.{ FlatSpec, Matchers }

abstract class MarkdownBaseSpec extends FlatSpec with Matchers {

  val markdownReader = new Reader
  val markdownWriter = new Writer

  def markdown(text: String)(implicit context: Location[Page] => Writer.Context = writerContext): String = {
    markdownPages("test.md" -> text).getOrElse("test.html", "")
  }

  def markdownPages(mappings: (String, String)*)(implicit context: Location[Page] => Writer.Context = writerContext): Map[String, String] = {
    def render(location: Option[Location[Page]], rendered: Seq[(String, String)] = Seq.empty): Seq[(String, String)] = location match {
      case Some(loc) =>
        val page = loc.tree.label
        val html = normalize(markdownWriter.write(page.markdown, context(loc)))
        render(loc.next, rendered :+ (page.path, html))
      case None => rendered
    }
    render(Location.forest(pages(mappings: _*))).toMap
  }

  // TODO: include pageMappings
  def writerContext(location: Location[Page]): Writer.Context = {
    Writer.Context(location, Page.allPaths(List(location.root.tree)).toSet)
  }

  def pages(mappings: (String, String)*): Forest[Page] = {
    val parsed = mappings map {
      case (path, text) =>
        val frontin = Frontin(prepare(text))
        (new File(path), path, markdownReader.read(frontin.body), frontin.header)
    }
    Page.forest(parsed, Path.replaceSuffix(Writer.DefaultSourceSuffix, Writer.DefaultTargetSuffix))
  }

  def html(text: String): String = {
    normalize(prepare(text))
  }

  def htmlPages(mappings: (String, String)*): Map[String, String] = {
    (mappings map { case (path, text) => (path, html(text)) }).toMap
  }

  def prepare(text: String): String = {
    text.stripMargin.trim
  }

  def normalize(html: String) = {
    val reader = new java.io.StringReader(html)
    val writer = new java.io.StringWriter
    val tidy = new org.w3c.tidy.Tidy
    tidy.setTabsize(2)
    tidy.setPrintBodyOnly(true)
    tidy.setTrimEmptyElements(false)
    tidy.setShowWarnings(false)
    tidy.setQuiet(true)
    tidy.parse(reader, writer)
    writer.toString.replace("\r\n", "\n").replace("\r", "\n")
  }

}
