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

package com.lightbend.paradox

import java.io.File
import com.lightbend.paradox.markdown.Page
import org.pegdown.ast.Node

import scala.collection.mutable.ListBuffer
import scala.io.BufferedSource

trait ErrorContext {
  def apply(msg: String, index: Int): Unit

  def apply(msg: String, node: Node): Unit = apply(msg, node.getStartIndex)

  def apply(msg: String): Unit

  def apply(msg: String, file: File, index: Int): Unit

  def apply(msg: String, file: File, node: Node): Unit = apply(msg, file, node.getStartIndex)

  def apply(msg: String, file: File): Unit

  def apply(msg: String, page: Page, index: Int): Unit = apply(msg, page.file, index)

  def apply(msg: String, page: Page, node: Node): Unit = apply(msg, page.file, node.getStartIndex)

  def apply(msg: String, page: Page): Unit = apply(msg, page.file)
}

class ErrorCollector extends ErrorContext {
  private val errors = new ListBuffer[ParadoxError]

  private def addError(msg: String, page: Option[File], index: Option[Int]): Unit =
    errors.append(ParadoxError(msg, page, index))

  override def apply(msg: String, index: Int): Unit = throw new IllegalArgumentException(
    "Cannot report an indexed error without a page context"
  )

  override def apply(msg: String): Unit = addError(msg, None, None)

  override def apply(msg: String, file: File, index: Int): Unit = addError(msg, Some(file), Some(index))

  override def apply(msg: String, file: File): Unit = addError(msg, Some(file), None)

  def hasErrors: Boolean = errors.nonEmpty

  def errorCount: Int = errors.toList.distinct.size

  def logErrors(log: ParadoxLogger): Unit = {
    val totalErrors = errors.toList.distinct
    // First log general errors
    totalErrors.foreach {
      case ParadoxError(msg, None, _) => log.error(msg)
      case _                          =>
    }
    // Now handle page specific errors
    totalErrors
      .filter(_.page.isDefined)
      .groupBy(_.page.get)
      .toSeq
      .sortBy(_._1.getAbsolutePath)
      .foreach { case (page, errors) =>
        // Load contents of the page
        var source: BufferedSource = null
        val lines =
          try {
            source = scala.io.Source.fromFile(page)("UTF-8")
            source.getLines().toList
          } finally
            source.close()
        errors.sortBy(_.index.getOrElse(0)).foreach {
          case ParadoxError(error, _, Some(idx)) =>
            val (_, lineNo, colNo, line) = lines.foldLeft((0, 0, 0, None: Option[String])) { (state, line) =>
              state match {
                case (_, _, _, Some(_)) => state
                case (total, l, c, None) =>
                  if (total + line.length < idx) {
                    (total + line.length + 1, l + 1, c, None)
                  } else {
                    (0, l + 1, idx - total + 1, Some(line))
                  }
              }
            }

            log.error(s"$error at ${page.getAbsolutePath}:$lineNo")
            line.foreach { l =>
              log.error(l)
              log.error(l.take(colNo - 1).map { case '\t' => '\t'; case _ => ' ' } + "^")
            }
          case ParadoxError(error, _, _) =>
            log.error(s"$error at ${page.getAbsolutePath}")

        }
      }
  }
}

class PagedErrorContext(context: ErrorContext, page: Page) extends ErrorContext {
  override def apply(msg: String, index: Int): Unit = context.apply(msg, page, index)

  override def apply(msg: String): Unit = context.apply(msg, page)

  override def apply(msg: String, file: File, index: Int): Unit = context.apply(msg, file, index)

  override def apply(msg: String, file: File): Unit = context.apply(msg, file)
}

class ThrowingErrorContext extends ErrorContext {
  override def apply(msg: String, index: Int): Unit = throw ParadoxException(ParadoxError(msg, None, Some(index)))

  override def apply(msg: String): Unit = throw ParadoxException(ParadoxError(msg, None, None))

  override def apply(msg: String, file: File, index: Int): Unit = throw ParadoxException(
    ParadoxError(msg, Some(file), Some(index))
  )

  override def apply(msg: String, file: File): Unit = throw ParadoxException(ParadoxError(msg, Some(file), None))
}

case class ParadoxError(msg: String, page: Option[File], index: Option[Int])

case class ParadoxException(error: ParadoxError) extends RuntimeException(error.msg)
