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
import java.nio.file.{ Path => jPath }

/**
 * Small wrapper around URI to help update individual components.
 */
case class Url(base: URI) {
  def withEndingSlash = base.getPath match {
    case path if path.endsWith("/index.html") => this
    case path                                 => copy(path = path + "/")
  }
  def /(path: String): Url = copy(path = base.getPath + (if (path != "") "/" + path else ""))
  def withQuery(query: String): Url = copy(query = query)
  def withFragment(fragment: String): Url = copy(fragment = fragment)
  def copy(path: String = base.getPath, query: String = base.getQuery, fragment: String = base.getFragment) = {
    val uri = new URI(base.getScheme, base.getUserInfo, base.getHost, base.getPort, path, query, fragment)
    Url(uri.normalize)
  }
  override def toString(): String = base.toString
}

object Url {
  /**
   * Exception thrown for unknown or invalid URLs.
   */
  case class Error(reason: String) extends RuntimeException(reason)

  def apply(base: String): Url = {
    parse(base, "template resulted in an invalid URL")
  }

  def parse(base: String, msg: String): Url = {
    try Url(new URI(base)) catch {
      case e: URISyntaxException =>
        throw Url.Error(s"$msg [$base]")
    }
  }
}

class BasePropertyClass(property: String, variables: String => Option[String]) {
  def base: String = variables(property) match {
    case Some(baseUrl) => baseUrl
    case None          => throw Url.Error(s"property [$property] is not defined")
  }

  def resolve(): Url = {
    Url.parse(base, s"property [$property] contains an invalid URL")
  }
}

case class PropertyUrl(property: String, variables: String => Option[String]) extends BasePropertyClass(property, variables) {
  def format(args: String*) = Url(base.format(args: _*))

  def collect(f: PartialFunction[String, String]): Url = {
    PropertyUrl(property, variables(_).collect(f)).resolve
  }
}

case class PropertyDirectory(property: String, variables: String => Option[String]) extends BasePropertyClass(property, variables) {
  override def base: String = variables(property) match {
    case Some(baseUrl) => checkSeparatorDuplicates(normalizeBase(baseUrl))
    case None          => throw Url.Error(s"property [$property] is not defined")
  }

  def normalize(link: String, sourcePath: jPath): String = {
    val additionalLink = convertLink(link, sourcePath)
    val normalizeVal = "/" + PropertyDirectory(property, variables).resolve.toString + "/src/main/paradox/" + additionalLink
    println("--- Url: normalize = " + normalizeVal)
    Url.parse("/" + PropertyDirectory(property, variables).resolve.toString + "/src/main/paradox/" + additionalLink,
      s"link [$additionalLink] contains an invalid URL").toString
  }

  private def convertLink(link: String, sourcePath: jPath, extensionExpected: String = ".md"): String = link match {
    case ""                                    => sourcePath.toString
    case l if (!l.endsWith(extensionExpected)) => throw Url.Error(s"[$l] is not a markdown (.md) file")
    case l =>
      val finalLink = withoutLeaf(sourcePath.toString) + checkSeparatorDuplicates(normalizeLink(link))
      println("--- Url: finalLink = " + finalLink)
      return finalLink
  }

  private def withoutLeaf(path: String, separator: String = "/"): String = {
    path.split(separator).reverse.tail.reverse.mkString(separator) match {
      case "" => ""
      case p  => p + "/"
    }
  }

  private def checkSeparatorDuplicates(normalizedPath: String, separator: String = "/"): String = {
    normalizedPath.split(separator).contains("") match {
      case true  => throw Url.Error(s"[$normalizedPath] contains duplicate '/' separators")
      case false => normalizedPath
    }
  }

  private def normalizeBase(baseUrl: String): String = {
    (baseUrl.startsWith("/"), baseUrl.endsWith("/")) match {
      case (true, true)  => baseUrl.drop(1).dropRight(1)
      case (true, false) => baseUrl.drop(1)
      case (false, true) => baseUrl.dropRight(1)
      case _             => baseUrl
    }
  }

  private def normalizeLink(link: String): String = {
    link.startsWith("/") match {
      case true  => link.drop(1)
      case false => link
    }
  }
}
