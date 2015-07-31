/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

import org.scalatest.{ FlatSpec, Matchers }

class PathSpec extends FlatSpec with Matchers {

  "Path.basePath" should "handle root pages" in {
    Path.basePath("foo.html") shouldEqual ("")
  }

  it should "have correct number of ups" in {
    Path.basePath("a/b/foo.html") shouldEqual ("../../")
  }

  "Path.resolve" should "resolve a sibling page" in {
    Path.resolve("a/b/foo.html", "bar.html") shouldEqual "a/b/bar.html"
  }

  it should "resolve a deeper page" in {
    Path.resolve("a/foo.html", "b/bar.html") shouldEqual "a/b/bar.html"
  }

  it should "resolve a path with ups" in {
    Path.resolve("a/foo.html", "../a/b/bar.html") shouldEqual "a/b/bar.html"
  }

  "Path.replaceExtension" should "replace .md with .html" in {
    Path.replaceExtension(".md", ".html")("foo.md") shouldEqual "foo.html"
  }

  it should "support full paths" in {
    Path.replaceExtension(".md", ".html")("a/b/foo.md") shouldEqual "a/b/foo.html"
  }

  it should "support relative paths" in {
    Path.replaceExtension(".md", ".html")("../../a/b/foo.md") shouldEqual "../../a/b/foo.html"
  }

  it should "handle anchored paths" in {
    Path.replaceExtension(".md", ".html")("../../a/b/foo.md#anchor") shouldEqual "../../a/b/foo.html#anchor"
  }

}
