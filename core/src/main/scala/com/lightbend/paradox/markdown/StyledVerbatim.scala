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

import java.util.function.Consumer

import scala.collection.mutable.Buffer
import scala.collection.JavaConverters._
import org.parboiled.common.StringUtils
import org.pegdown.ast.{ VerbatimGroupNode, VerbatimNode }
import org.pegdown.{ Printer, VerbatimSerializer }

/**
 * Add markup around verbatim blocks.
 */
abstract class StyledVerbatimSerializer extends VerbatimSerializer {

  def printPreAttributes(printer: Printer, nodeGroup: String = "", classes: Buffer[String] = Buffer.empty[String]): Unit

  def printCodeAttributes(printer: Printer, nodeType: String): Unit

  def serialize(node: VerbatimNode, printer: Printer) = {
    printer.println()

    printer.print("<pre")
    if (!StringUtils.isEmpty(node.getType)) {
      node match {
        case vgn: VerbatimGroupNode => printPreAttributes(printer, vgn.getGroup, vgn.getClasses.asScala)
        case vn: VerbatimNode       => printPreAttributes(printer)
      }
    }
    printer.print(">")

    node match {
      case vgn: VerbatimGroupNode =>
        printer.print("""<button class="snippet-button copy-snippet" title="Copy snippet to clipboard">copy</button>""")
        printer.print("\n")
        vgn.getSourceUrl.ifPresent(new Consumer[String] {
          override def accept(sourceUrl: String): Unit = {
            printer.print(s"""<a class="snippet-button go-to-source" href="$sourceUrl" target="_blank" title="Go to snippet source">source</a>""")
            printer.print("\n")
          }
        })
      case _ =>
    }

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

    printer.print("</code></pre>")
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
  override def printPreAttributes(printer: Printer, nodeGroup: String, classes: Buffer[String]): Unit = {
    val allClasses = "prettyprint" +: (nodeGroup match {
      case "" => classes
      case g  => ("group-" + g) +: classes
    })
    printClass(printer, allClasses.mkString(" "))
  }

  override def printCodeAttributes(printer: Printer, nodeType: String): Unit = nodeType match {
    case "text" | "nocode" => printClass(printer, "nocode")
    case _                 => printClass(printer, s"language-$nodeType")
  }
}

object RawVerbatimSerializer extends VerbatimSerializer {

  val tag = "raw"

  override def serialize(node: VerbatimNode, printer: Printer): Unit = {
    printer.println()
    printer.print(node.getText)
    printer.printchkln()
  }
}
