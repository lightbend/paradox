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

import java.io.{PrintWriter, StringWriter}

import scala.collection.immutable.StringOps

trait ParadoxLogger {
  def debug(t: Throwable): Unit = {
    // we provide our own implementation because sbt doesn't offer any exception logging at debug
    val writer = new StringWriter
    t.printStackTrace(new PrintWriter(writer))
    new StringOps(writer.toString).lines.foreach(debug(_))
  }
  def debug(msg: => String): Unit
  def info(msg: => String): Unit
  def warn(msg: => String): Unit
  def error(msg: => String): Unit
}

object NullLogger extends ParadoxLogger {
  override def debug(msg: => String): Unit = ()
  override def info(msg: => String): Unit  = ()
  override def warn(msg: => String): Unit  = ()
  override def error(msg: => String): Unit = ()
}

object PrintlnLogger extends ParadoxLogger {
  override def debug(msg: => String): Unit = println(s"[debug] $msg")
  override def info(msg: => String): Unit  = println(s"[info] $msg")
  override def warn(msg: => String): Unit  = println(s"[warn] $msg")
  override def error(msg: => String): Unit = println(s"[error] $msg")
}
