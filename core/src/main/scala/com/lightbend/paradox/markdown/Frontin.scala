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

import java.io.{ File, StringReader }
import collection.JavaConverters._

case class Frontin(header: Map[String, String], body: String)

object Frontin {
  val separator = "---"

  def separates(str: String): Boolean =
    (str.trim == separator) && (str startsWith separator)

  def apply(file: File): Frontin = {
    val source = scala.io.Source.fromFile(file)("UTF-8")
    val lines = source.getLines.mkString("\n")
    source.close()
    apply(lines)
  }

  def apply(str: String): Frontin =
    str.linesWithSeparators.toList match {
      case Nil => Frontin(Map.empty[String, String], "")
      case x :: xs if separates(x) =>
        xs span { !separates(_) } match {
          case (h, b) => Frontin(loadProperties(Some(h.mkString(""))), if (b.isEmpty) "" else b.tail.mkString(""))
        }
      case _ => Frontin(Map.empty[String, String], str)
    }

  def loadProperties(str: Option[String]): Map[String, String] = str match {
    case None => Map.empty[String, String]
    case Some(s) =>
      val p = new java.util.Properties
      p.load(new StringReader(s))
      p.asScala.toMap
  }
}