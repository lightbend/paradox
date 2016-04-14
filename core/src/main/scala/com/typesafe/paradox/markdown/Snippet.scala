/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

import java.io.File
import org.parboiled.common.FileUtils
import scala.io.Source

object Snippet {

  class SnippetException(message: String) extends RuntimeException(message)

  def apply(file: File, label: Option[String]): String = label match {
    case Some(label) => extract(file, label)
    case None        => extract(file, _ => true, _ => false, addFilteredLine).snippetLines.mkString("\n")
  }

  def extract(file: File, label: String): String = {
    if (!verifyLabel(label)) throw new SnippetException(s"Label [$label] for [$file] contains illegal characters. " +
      "Only [a-zA-Z0-9_-] are allowed.")
    // A label can be followed by an end of line or a space followed by a single sequence of contiguous
    // (no whitespace) non-word characters (anything not in the group [a-zA-Z0-9_]
    val labelPattern = ("""#\Q""" + label + """\E( [^w \t]*)?$""").r
    val hasLabel = (s: String) => labelPattern.findFirstIn(s).nonEmpty
    val extractionState = extract(file, hasLabel, hasLabel, addFilteredLine)
    val snippetLines = extractionState.snippetLines
    if (snippetLines.isEmpty) throw new SnippetException(s"Label [$label] not found in [$file]")
    if (extractionState.inBlock) throw new SnippetException(s"Label [$label] block not closed in [$file]")
    val indent = snippetLines.flatMap(l => Some(l.indexWhere(_ != ' ')).filter(_ >= 0)).min
    (snippetLines map (_ drop indent)).mkString("\n")
  }

  private def extract(file: File, blockStart: (String) => Boolean, blockEnd: (String) => Boolean, addLine: (String, Seq[String]) => Seq[String]): ExtractionState = {
    val lines = Source.fromFile(file).getLines.toSeq
    lines.foldLeft(ExtractionState(inBlock = false, snippetLines = Seq.empty)) {
      case (es, l) =>
        es.inBlock match {
          case false if (blockStart(l)) => es.copy(inBlock = true, snippetLines = addLine(l, es.snippetLines))
          case false                    => es
          case true if (blockEnd(l))    => es.copy(inBlock = false, snippetLines = addLine(l, es.snippetLines))
          case true                     => es.copy(snippetLines = addLine(l, es.snippetLines))
        }
    }
  }

  private case class ExtractionState(inBlock: Boolean, snippetLines: Seq[String])

  private val anyLabelRegex = """#[a-zA-Z_0-9\-]+( [^w \t]*)?$""".r
  private def addFilteredLine(line: String, lines: Seq[String]): Seq[String] =
    anyLabelRegex.findFirstIn(line).map(_ => lines).getOrElse(lines :+ line)
  private def verifyLabel(label: String): Boolean = anyLabelRegex.findFirstIn(s"#$label").nonEmpty

  def language(file: File): String = {
    val name = file.getName
    val dot = name.lastIndexOf('.')
    if (dot < 0) "" else name.substring(dot + 1)
  }

}
