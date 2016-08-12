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
