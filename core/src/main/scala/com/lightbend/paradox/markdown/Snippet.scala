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

import java.io.File

import scala.annotation.tailrec
import scala.io.Source

object Snippet {

  class SnippetException(message: String) extends RuntimeException(message)

  def apply(file: File, labels: Seq[String], filterLabelLines: Boolean): (String, String) = {
    val source = Source.fromFile(file)("UTF-8")
    try {
      val lines = source.getLines.toSeq
      (extract(file, lines, labels, filterLabelLines), language(file))
    } finally {
      source.close()
    }
  }

  def extract(file: File, lines: Seq[String], labels: Seq[String], filterLabelLines: Boolean): String = {
    labels match {
      case Seq() =>
        val addLine = addFilteredLine(filterLabelLines)
        cutIndentation(lines.zipWithIndex.foldLeft(Seq.empty[(Int, String)]) {
          case (lines, (line, lineIndex)) => addLine(line, lines, lineIndex + 1)
        }.map(_._2))
      case _ =>
        labels.map { label =>
          val extractionState = extractState(file, lines, label, filterLabelLines)
          cutIndentation(extractionState.snippetLines)
        }.mkString("\n")
    }
  }

  private def cutIndentation(snippetLines: Seq[String]): String = {
    val minIndent =
      snippetLines.foldLeft(Integer.MAX_VALUE) {
        // ignore import lines when determining indentation
        case (in, ln) if !ln.startsWith("import") =>
          val idx = ln.indexWhere(_ != ' ')
          if (idx > -1)
            Math.min(in, idx)
          else in
        case (in, _) => in
      }
    snippetLines.map(ln => dropIndent(minIndent, ln)).mkString("\n")
  }

  def extractLabelRange(file: File, label: String, filterLabelLines: Boolean = true): Option[(Int, Int)] = {
    val source = Source.fromFile(file)("UTF-8")
    try {
      val lines = source.getLines.toSeq
      val lineNumbers = extractState(file, lines, label, filterLabelLines).lines.map(_._1)
      if (lineNumbers.isEmpty)
        None
      else
        Some((lineNumbers.min, lineNumbers.max))
    } finally {
      source.close()
    }
  }

  type Line = (Int, String)

  private def extractState(file: File, lines: Seq[String], label: String, filterLabelLines: Boolean): ExtractionState = {
    if (!verifyLabel(label)) throw new SnippetException(s"Label [$label] for [$file] contains illegal characters. " +
      "Only [a-zA-Z0-9_-] are allowed.")
    // A label can be followed by an end of line or one or more spaces followed by an
    // optional single sequence of contiguous (no whitespace) non-word characters
    // (anything not in the group [a-zA-Z0-9_])
    val labelPattern = ("""#\Q""" + label + """\E( +[^w \t]*)?$""").r
    val hasLabel = (s: String) => labelPattern.findFirstIn(s).nonEmpty
    val extractionState = extractFrom(lines, hasLabel, hasLabel, addFilteredLine(filterLabelLines))
    if (extractionState.snippetLines.isEmpty)
      throw new SnippetException(s"Label [$label] not found in [$file]")
    extractionState.block match {
      case InBlock => throw new SnippetException(s"Label [$label] block not closed in [$file]")
      case _       =>
    }
    extractionState
  }

  private def extractFrom(lines: Seq[String], blockStart: (String) => Boolean, blockEnd: (String) => Boolean, addLine: (String, Seq[Line], Int) => Seq[Line]): ExtractionState = {
    lines.zipWithIndex.foldLeft(ExtractionState(block = NoBlock, lines = Seq.empty)) {
      case (es, (l, lineIndex)) =>
        es.block match {
          case NoBlock if blockStart(l) => es.withBlock(InBlock)
          case NoBlock                  => es
          case InBlock if blockEnd(l)   => es.withBlock(NoBlock)
          case InBlock =>
            es.copy(lines = addLine(l, es.lines, lineIndex + 1))
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

  private case class ExtractionState(block: Block, lines: Seq[Line]) {
    def snippetLines = lines.map(_._2)
    def withBlock(block: Block) = copy(block = block)
  }

  private sealed trait Block
  private case object NoBlock extends Block
  private case object InBlock extends Block

  private val anyLabelRegex = """#[a-zA-Z_0-9\-]+( +[^w \t]*)?$""".r

  private def containsLabel(line: String): Option[String] =
    anyLabelRegex.findFirstIn(line)

  private def addFilteredLine(filterLabels: Boolean): (String, Seq[Line], Int) => Seq[Line] =
    if (filterLabels) addLineFilterLabels else addLineNonFiltered

  private def addLineFilterLabels(line: String, lines: Seq[Line], lineNumber: Int): Seq[Line] =
    containsLabel(line).map(_ => lines).getOrElse(lines :+ (lineNumber, line))

  private def addLineNonFiltered(line: String, lines: Seq[Line], lineNumber: Int): Seq[Line] =
    lines :+ (lineNumber, line)

  private def verifyLabel(label: String): Boolean = containsLabel(s"#$label").nonEmpty

  def language(file: File): String = {
    val name = file.getName
    val dot = name.lastIndexOf('.')
    if (dot < 0) "" else name.substring(dot + 1)
  }

}
