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

import scala.annotation.tailrec
import scala.io.Source

object Snippet {

  class SnippetException(message: String) extends RuntimeException(message)

  def apply(file: File, labels: Seq[String]): (String, String) = {
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

  def lineHasLabel(label: String): String => Boolean = {
    // A label can be followed by an end of line or one or more spaces followed by an
    // optional single sequence of contiguous (no whitespace) non-word characters
    // (anything not in the group [a-zA-Z0-9_])
    val labelPattern = ("""#\Q""" + label + """\E( +[^w \t]*)?$""").r
    val hasLabel = (s: String) => labelPattern.findFirstIn(s).nonEmpty
    hasLabel
  }

  private def extractState(file: File, label: String): ExtractionState = {
    if (!verifyLabel(label)) throw new SnippetException(s"Label [$label] for [$file] contains illegal characters. " +
      "Only [a-zA-Z0-9_-] are allowed.")
    val hasLabel = lineHasLabel(label)
    val extractionState = extract(file, hasLabel, hasLabel, addFilteredLine)
    if (extractionState.snippetLines.isEmpty)
      throw new SnippetException(s"Label [$label] not found in [$file]")
    extractionState.block match {
      case InBlock(_) => throw new SnippetException(s"Label [$label] block not closed in [$file]")
      case _          =>
    }
    extractionState
  }

  private def extract(file: File, label: String): String = {
    val extractionState = extractState(file, label)
    val snippetLines = extractionState.snippetLines
    snippetLines.mkString("\n")
  }

  private def extract(file: File, blockStart: (String) => Boolean, blockEnd: (String) => Boolean, addLine: (String, Seq[Line], Int) => Seq[Line]): ExtractionState = {
    val lines = Source.fromFile(file)("UTF-8").getLines.toSeq
    extractFrom(lines, blockStart, blockEnd, addLine)
  }

  def extractFrom(lines: Seq[String], blockStart: (String) => Boolean, blockEnd: (String) => Boolean, addLine: (String, Seq[Line], Int) => Seq[Line]): ExtractionState = {
    lines.zipWithIndex.foldLeft(ExtractionState(block = NoBlock, lines = Seq.empty)) {
      case (es, (l, lineIndex)) =>
        es.block match {
          case NoBlock if (blockStart(l)) =>
            val indent = l.indexWhere(_ != ' ')
            es.copy(block = InBlock(indent), lines = addLine(dropIndent(indent, l), es.lines, lineIndex + 1))
          case NoBlock => es
          case InBlock(indent) if (blockEnd(l)) =>
            es.copy(block = NoBlock, lines = addLine(dropIndent(indent, l), es.lines, lineIndex + 1))
          case InBlock(indent) =>
            es.copy(lines = addLine(dropIndent(indent, l), es.lines, lineIndex + 1))
        }
    }
  }

  // drop indent, but don't drop other characters than whitespace
  private def dropIndent(indent: Int, line: String): String = {
    @tailrec
    def loop(idx: Int): Int = {
      if (idx == indent || idx == line.length) idx
      else if (line(idx) == ' ' || line(idx) == '\t') loop(idx + 1)
      else idx
    }

    val charsToDrop = loop(0)
    line.substring(charsToDrop)
  }

  final case class ExtractionState(block: Block, lines: Seq[Line]) {
    def snippetLines = lines.map(_._2)
  }

  sealed trait Block
  case object NoBlock extends Block
  case class InBlock(indent: Int) extends Block

  private val anyLabelRegex = """#[a-zA-Z_0-9\-]+( +[^w \t]*)?$""".r
  def addFilteredLine(line: String, lines: Seq[Line], lineNumber: Int): Seq[Line] =
    anyLabelRegex.findFirstIn(line).map(_ => lines).getOrElse(lines :+ (lineNumber, line))
  private def verifyLabel(label: String): Boolean = anyLabelRegex.findFirstIn(s"#$label").nonEmpty

  def language(file: File): String = {
    val name = file.getName
    val dot = name.lastIndexOf('.')
    if (dot < 0) "" else name.substring(dot + 1)
  }

}
