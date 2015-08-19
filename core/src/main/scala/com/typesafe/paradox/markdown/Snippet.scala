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
    case None        => FileUtils.readAllText(file)
  }

  def extract(file: File, label: String): String = {
    val labelPattern = ("""#\Q""" + label + """\E""").r
    val notLabel = (s: String) => labelPattern.findFirstIn(s).isEmpty
    val lines = Source.fromFile(file).getLines.toSeq
    val snippetLines = lines dropWhile notLabel drop 1 takeWhile notLabel
    if (snippetLines.isEmpty) throw new SnippetException(s"Label [$label] not found in [$file]")
    val indent = snippetLines.flatMap(l => Some(l.indexWhere(_ != ' ')).filter(_ >= 0)).min
    (snippetLines map (_ drop indent)).mkString("\n")
  }

  def language(file: File): String = {
    val name = file.getName
    val dot = name.lastIndexOf('.')
    if (dot < 0) "" else name.substring(dot + 1)
  }

}
