/*
 * Copyright © 2015 - 2016 Lightbend, Inc. <http://www.lightbend.com>
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
 * Resolves URLs from Paradox properties and Markdown links.
 */
sealed trait UrlResolver {
  def base: String

  def /(path: String): UrlResolver = Url(resolve.normalize + "/" + path)

  def withFragment(fragment: String): UrlResolver = {
    Url(new URI(resolve.getScheme, resolve.getRawSchemeSpecificPart, fragment).toString)
  }

  def format(args: String*) = Url(base.format(args: _*))

  def resolve: URI = {
    try new URI(base) catch {
      case e: URISyntaxException => this match {
        case Url(_) =>
          throw UrlResolver.Error(s"template resulted in an invalid URL [$base]")
        case PropertyUrl(property, _) =>
          throw UrlResolver.Error(s"property [$property] contains an invalid URL [$base]")
      }
    }
  }
}

object UrlResolver {

  /**
   * Exception thrown for unknown or invalid URLs.
   */
  case class Error(reason: String) extends RuntimeException(reason)

}

case class Url(base: String) extends UrlResolver

case class PropertyUrl(property: String, variables: String => Option[String]) extends UrlResolver {
  def base = variables(property) match {
    case Some(baseUrl) => baseUrl
    case None          => throw UrlResolver.Error(s"property [$property] is not defined")
  }

  def collect(f: PartialFunction[String, String]): PropertyUrl = {
    PropertyUrl(property, variables(_).collect(f))
  }
}
