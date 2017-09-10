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

import java.io.File
import org.parboiled.common.FileUtils
import scala.io.Source

object Snippet {

  class SnippetException(message: String) extends RuntimeException(message)

  def apply(propPrefix: String, source: String, labels: Seq[String], page: Page, variables: Map[String, String]): (String, String) = {
    val file = resolveFile(propPrefix, source, page, variables)
    (extract(file, labels), language(file))
  }

  def extract(file: File, labels: Seq[String]): String = labels match {
    case Seq() => extract(file, _ => true, _ => false, addFilteredLine).snippetLines.mkString("\n")
    case _     => labels.map(label => extract(file, label)).mkString("\n")
  }

  def extractLabelRange(file: File, label: String): Option[(Int, Int)] = {
    val lineNumbers = extractState(file, label).lines.map(_._1)
    if (lineNumbers.isEmpty)
      None
    else
      Some((lineNumbers.min, lineNumbers.max))
  }

  type Line = (Int, String)

  private def resolveFile(propPrefix: String, source: String, page: Page, variables: Map[String, String]): File = {
    if (source startsWith "$") {
      val baseKey = source.drop(1).takeWhile(_ != '$')
      val base = new File(PropertyUrl(s"$propPrefix.$baseKey.base_dir", variables.get).base.trim)
      val effectiveBase = if (base.isAbsolute) base else new File(page.file.getParentFile, base.toString)
      new File(effectiveBase, source.drop(baseKey.length + 2))
    } else new File(page.file.getParentFile, source)
  }

  private def extractState(file: File, label: String): ExtractionState = {
    if (!verifyLabel(label)) throw new SnippetException(s"Label [$label] for [$file] contains illegal characters. " +
      "Only [a-zA-Z0-9_-] are allowed.")
    // A label can be followed by an end of line or one or more spaces followed by an
    // optional single sequence of contiguous (no whitespace) non-word characters
    // (anything not in the group [a-zA-Z0-9_])
    val labelPattern = ("""#\Q""" + label + """\E( +[^w \t]*)?$""").r
    val hasLabel = (s: String) => labelPattern.findFirstIn(s).nonEmpty
    val extractionState = extract(file, hasLabel, hasLabel, addFilteredLine)
    if (extractionState.snippetLines.isEmpty)
      throw new SnippetException(s"Label [$label] not found in [$file]")
    if (extractionState.inBlock)
      throw new SnippetException(s"Label [$label] block not closed in [$file]")
    extractionState
  }

  private def extract(file: File, label: String): String = {
    val extractionState = extractState(file, label)
    val snippetLines = extractionState.snippetLines
    val indent = snippetLines.flatMap(l => Some(l.indexWhere(_ != ' ')).filter(_ >= 0)).min
    (snippetLines map (_ drop indent)).mkString("\n")
  }

  private def extract(file: File, blockStart: (String) => Boolean, blockEnd: (String) => Boolean, addLine: (String, Seq[Line], Int) => Seq[Line]): ExtractionState = {
    val lines = Source.fromFile(file)("UTF-8").getLines.toSeq
    lines.zipWithIndex.foldLeft(ExtractionState(inBlock = false, lines = Seq.empty)) {
      case (es, (l, lineIndex)) =>
        es.inBlock match {
          case false if (blockStart(l)) => es.copy(inBlock = true, lines = addLine(l, es.lines, lineIndex + 1))
          case false                    => es
          case true if (blockEnd(l))    => es.copy(inBlock = false, lines = addLine(l, es.lines, lineIndex + 1))
          case true                     => es.copy(lines = addLine(l, es.lines, lineIndex + 1))
        }
    }
  }

  private case class ExtractionState(inBlock: Boolean, lines: Seq[Line]) {
    def snippetLines = lines.map(_._2)
  }

  private val anyLabelRegex = """#[a-zA-Z_0-9\-]+( +[^w \t]*)?$""".r
  private def addFilteredLine(line: String, lines: Seq[Line], lineNumber: Int): Seq[Line] =
    anyLabelRegex.findFirstIn(line).map(_ => lines).getOrElse(lines :+ (lineNumber, line))
  private def verifyLabel(label: String): Boolean = anyLabelRegex.findFirstIn(s"#$label").nonEmpty

  def language(file: File): String = {
    val name = file.getName
    val dot = name.lastIndexOf('.')
    if (dot < 0) "" else name.substring(dot + 1)
  }

}
