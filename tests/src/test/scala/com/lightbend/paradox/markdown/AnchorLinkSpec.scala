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

import com.lightbend.paradox.ThrowingErrorContext

class AnchorLinkSpec extends MarkdownBaseSpec {

  "Anchor links" should "create anchor for headers" in {
    markdown("# Title") shouldEqual
      html("""
        |<h1><a href="#title" name="title" class="anchor">
        |<span class="anchor-link"></span></a>Title</h1>""")
  }

  it should "parse and render all header children" in {
    markdown("# Title with `code`...") shouldEqual
      html("""
        |<h1><a href="#title-with-code" name="title-with-code" class="anchor">
        |<span class="anchor-link"></span></a>Title with <code>code</code>&hellip;</h1>
        """)
  }

  private def h(level: Int, name: String, title: String) =
    s"""<h$level><a href="#$name" name="$name" class="anchor"><span class="anchor-link"></span></a>$title</h$level>"""

  it should "give repeated headers unique anchors, keeping the first one" in {
    markdown("""
      |## 1.0.2
      |### Bug Fixes
      |### Changes
      |## 1.0.1
      |### Bug Fixes
      |## 1.0.0
      |### Bug Fixes
      """) shouldEqual html(
      Seq(
        h(2, "1-0-2", "1.0.2"),
        h(3, "bug-fixes", "Bug Fixes"),
        h(3, "changes", "Changes"),
        h(2, "1-0-1", "1.0.1"),
        h(3, "bug-fixes-1", "Bug Fixes"),
        h(2, "1-0-0", "1.0.0"),
        h(3, "bug-fixes-2", "Bug Fixes")
      ).mkString("\n")
    )
  }

  it should "skip suffixes already used by another header" in {
    markdown("""
      |## Bug Fixes
      |## Bug Fixes 1
      |## Bug Fixes
      """) shouldEqual html(
      Seq(
        h(2, "bug-fixes", "Bug Fixes"),
        h(2, "bug-fixes-1", "Bug Fixes 1"),
        h(2, "bug-fixes-2", "Bug Fixes")
      ).mkString("\n")
    )
  }

  it should "use the unique anchors in the page toc" in {
    markdown("""
      |# Releases
      |@@ toc
      |## 1.0.1
      |### Bug Fixes
      |## 1.0.0
      |### Bug Fixes
      """) shouldEqual html(
      s"""
      |${h(1, "releases", "Releases")}
      |<div class="toc">
      |  <ul>
      |    <li><a href="test.html#1-0-1" class="header">1.0.1</a>
      |      <ul>
      |        <li><a href="test.html#bug-fixes" class="header">Bug Fixes</a></li>
      |      </ul>
      |    </li>
      |    <li><a href="test.html#1-0-0" class="header">1.0.0</a>
      |      <ul>
      |        <li><a href="test.html#bug-fixes-1" class="header">Bug Fixes</a></li>
      |      </ul>
      |    </li>
      |  </ul>
      |</div>
      |${h(2, "1-0-1", "1.0.1")}
      |${h(3, "bug-fixes", "Bug Fixes")}
      |${h(2, "1-0-0", "1.0.0")}
      |${h(3, "bug-fixes-1", "Bug Fixes")}"""
    )
  }

  it should "be idempotent when a page is indexed again" in {
    val root = paradoxProcessor.parseAndProcessMarkdown(
      new java.io.File("test.md"),
      prepare("""
        |## Bug Fixes
        |## Bug Fixes
        """),
      Map.empty,
      new ThrowingErrorContext
    )
    Index.uniqueHeaderAnchors(root)
    Index.uniqueHeaderAnchors(root)
    Index.headers(root).map(_.label.path) shouldEqual List("#bug-fixes", "#bug-fixes-1")
  }

  it should "not change anchors across pages" in {
    markdownPages(
      "a.md" -> "## Bug Fixes",
      "b.md" -> "## Bug Fixes"
    ) shouldEqual htmlPages(
      "a.html" -> h(2, "bug-fixes", "Bug Fixes"),
      "b.html" -> h(2, "bug-fixes", "Bug Fixes")
    )
  }

}
