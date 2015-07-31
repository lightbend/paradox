/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

import org.parboiled.common.StringUtils
import org.pegdown.ast.VerbatimNode
import org.pegdown.{ Printer, VerbatimSerializer }

/**
 * Add markup around verbatim blocks.
 */
abstract class StyledVerbatimSerializer extends VerbatimSerializer {

  def printPreAttributes(printer: Printer): Unit

  def printCodeAttributes(printer: Printer, nodeType: String): Unit

  def serialize(node: VerbatimNode, printer: Printer) = {
    printer.println()

    printer.print("<pre")
    printPreAttributes(printer)
    printer.print(">")

    printer.print("<code")
    if (!StringUtils.isEmpty(node.getType)) {
      printCodeAttributes(printer, node.getType)
    }
    printer.print(">")

    val text = node.getText
    // print HTML breaks for all initial newlines
    text.takeWhile(_ == '\n').foreach { _ =>
      printer.print("<br/>")
    }
    printer.printEncoded(text.dropWhile(_ == '\n'))

    printer.print("</code></pre>");
  }

  def printClass(printer: Printer, value: String): Unit = printAttribute(printer, "class", value)

  def printAttribute(printer: Printer, name: String, value: String): Unit = {
    printer.print(' ').print(name).print('=').print('"').print(value).print('"')
  }

}

/**
 * Add prettify markup around verbatim blocks.
 */
object PrettifyVerbatimSerializer extends StyledVerbatimSerializer {
  override def printPreAttributes(printer: Printer): Unit = printClass(printer, "prettyprint")
  override def printCodeAttributes(printer: Printer, nodeType: String): Unit = printClass(printer, s"language-$nodeType")
}
