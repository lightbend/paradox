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

import java.net.{ URI, URISyntaxException }

/**
 * Small wrapper around URI to help update individual components.
 */
case class Url(base: URI) {
  def withEndingSlash = base.getPath match {
    case path if path.endsWith("/index.html") => this
    case path                                 => copy(path = path + "/")
  }
  def /(path: String): Url = copy(path = base.getPath + "/" + path)
  def withQuery(query: String): Url = copy(query = query)
  def withFragment(fragment: String): Url = copy(fragment = fragment)
  def copy(path: String = base.getPath, query: String = base.getQuery, fragment: String = base.getFragment) = {
    val uri = new URI(base.getScheme, base.getUserInfo, base.getHost, base.getPort, path, query, fragment)
    Url(uri.normalize)
  }
  override def toString: String = base.toString
}

object Url {
  /**
   * Exception thrown for unknown or invalid URLs.
   */
  case class Error(reason: String) extends RuntimeException(reason)

  def apply(base: String): Url = {
    parse(base, s"template resulted in an invalid URL [$base]")
  }

  def parse(base: String, msg: String): Url = {
    try Url(new URI(base)) catch {
      case e: URISyntaxException =>
        throw Url.Error(msg)
    }
  }
}

case class PropertyUrl(property: String, variables: String => Option[String]) {
  def base = variables(property) match {
    case Some(baseUrl) => baseUrl
    case None          => throw Url.Error(s"property [$property] is not defined")
  }

  def resolve(): Url = {
    Url.parse(base, s"property [$property] contains an invalid URL [$base]")
  }

  def format(args: String*) = Url(base.format(args: _*))

  def collect(f: PartialFunction[String, String]): Url = {
    PropertyUrl(property, variables(_).collect(f)).resolve
  }
}
