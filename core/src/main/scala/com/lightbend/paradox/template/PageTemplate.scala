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

package com.lightbend.paradox.template

import java.io.{ File, OutputStreamWriter, FileOutputStream }
import java.nio.charset.StandardCharsets
import java.util.{ Map => JMap }

import org.stringtemplate.v4.misc.STMessage
import org.stringtemplate.v4.{ STErrorListener, STRawGroupDir, ST, NoIndentWriter }

import collection.concurrent.TrieMap

/**
 * Page template writer.
 */
class PageTemplate(directory: File, val defaultName: String = "page", startDelimiter: Char = '$', stopDelimiter: Char = '$') {
  private val templates = new STRawGroupDir(directory.getAbsolutePath, startDelimiter, stopDelimiter)

  /**
   * Write a templated page to the target file.
   */
  def write(name: String, contents: PageTemplate.Contents, target: File, errorListener: STErrorListener): File = {
    import scala.collection.JavaConverters._

    val template = Option(templates.getInstanceOf(name)) match {
      case Some(t) => // TODO, only load page properties, not global ones
        for (content <- contents.getProperties.asScala.filterNot(_._1.contains("."))) { t.add(content._1, content._2) }
        t.add("page", contents)
      case None => sys.error(s"StringTemplate '$name' was not found for '$target'. Create a template or set a theme that contains one.")
    }
    val osWriter = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)
    val noIndentWriter = new NoIndentWriter(osWriter)
    template.write(noIndentWriter) // does not take into account the errorListener any more...
    osWriter.close
    target
  }

}

object PageTemplate {
  /**
   * All page information to give to the template.
   */
  trait Contents {
    def getTitle: String
    def getContent: String
    def getBase: String
    def getHome: Link
    def getPrev: Link
    def getNext: Link
    def getBreadcrumbs: String
    def getNavigation: String
    def hasSubheaders: Boolean
    def getToc: String
    def getSource_url: String
    def getProperties: JMap[String, String]
  }

  /**
   * Page link. Can be rendered as just the href or full HTML.
   */
  trait Link {
    def getHref: String
    def getHtml: String
    def getTitle: String
    def isActive: Boolean
  }

  /**
   * Error listener wrapper.
   */
  class ErrorLogger(error: String => Unit) extends STErrorListener {
    override def compileTimeError(stm: STMessage): Unit = error(stm.toString)
    override def runTimeError(stm: STMessage): Unit = error(stm.toString)
    override def IOError(stm: STMessage): Unit = error(stm.toString)
    override def internalError(stm: STMessage): Unit = error(stm.toString)
  }
}
