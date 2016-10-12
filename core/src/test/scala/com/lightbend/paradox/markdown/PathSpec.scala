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
import java.io.File

class PathSpec extends FlatSpec with Matchers {
  def provideRelativeMapping: (Map[String, String], String) = {
    val mappings = Map("index.md" -> "index.html",
      "a/A.md" -> "a/A.html",
      "b/B.md" -> "b/B.html",
      "a/a2/A2.md" -> "a/a2/A2.html",
      "a/b/sameFolder.md" -> "a/b/sameFolder.html",
      "a/b/c/ABC.md" -> "a/b/c/ABC.html")
    val sourcePath = "a/b/source.md"
    (mappings, sourcePath)
  }

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

  "Path.leaf" should "return the name of the file at the end of the path" in {
    Path.leaf("/path/for/test/index.html") shouldEqual "index.html"
  }

  "Path.relativeRootPath" should "return the relative root path given a full path and the end of the path" in {
    Path.relativeRootPath(new File("/a/b/c/d.md"), "c/d.md") shouldEqual "/a/b/"
  }

  "Path.relativeLocalPath" should "return the relative local path given the root path and its full path" in {
    Path.relativeLocalPath("/a/b/", "/a/b/c/d.md") shouldEqual "c/d.md"
  }

  "Path.refRelativePath" should "return the correct relative path" in {
    val root = List("a", "b")
    val path = List("a", "c", "d")
    Path.refRelativePath(root, path, "index.md") shouldEqual "../c/d/index.md"
  }

  "Path.relativeMapping" should "return the correct mapping given the current source file and the global Mappings" in {
    val (mappings, sourcePath) = provideRelativeMapping
    Path.relativeMapping(sourcePath, mappings) shouldEqual Map("../../index.md" -> "../../index.html",
      "../A.md" -> "../A.html",
      "../../b/B.md" -> "../../b/B.html",
      "../a2/A2.md" -> "../a2/A2.html",
      "sameFolder.md" -> "sameFolder.html",
      "c/ABC.md" -> "c/ABC.html")
  }

  "Path.generateTargetFile" should "return the corresponding target file given the relative mapping for the current file" in {
    val (mappings, sourcePath) = provideRelativeMapping
    val newMapping = Path.generateTargetFile(sourcePath, mappings)_

    newMapping("../../index.md") shouldEqual "../../index.html"
    the[RuntimeException] thrownBy {
      newMapping("A.md")
    } should have message "No reference link corresponding to A.md"
    newMapping("../a2/A2.md#someanchor") shouldEqual "../a2/A2.html#someanchor"
  }
}
