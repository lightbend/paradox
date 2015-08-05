/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.template

import java.io.File
import java.util.{ Map => JMap }
import org.stringtemplate.v4.misc.STMessage
import org.stringtemplate.v4.{ STErrorListener, STRawGroupDir }

/**
 * Page template writer.
 */
class PageTemplate(directory: File, startDelimiter: Char = '$', stopDelimiter: Char = '$', name: String = "page") {

  private val templates = new STRawGroupDir(directory.getAbsolutePath, startDelimiter, stopDelimiter)

  /**
   * Write a templated page to the target file.
   */
  def write(contents: PageTemplate.Contents, target: File, errorListener: STErrorListener): File = {
    val template = templates.getInstanceOf(name).add("page", contents)
    template.write(target, errorListener)
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
