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

class TocDirectiveSpec extends MarkdownBaseSpec {

  "Toc directive" should "link to pages and headers" in {
    markdownPages(
      "foo.md" -> """
        |# Foo
        |@@ toc
        |## A
        |### B
        |@@@ index
        |* [bar](bar.md)
        |@@@
      """,
      "bar.md" -> """
        |# Bar
        |## A
        |### B
      """
    ) shouldEqual htmlPages(
      "foo.html" -> """
          |<h1><a href="#foo" name="foo" class="anchor"><span class="anchor-link"></span></a>Foo</h1>
          |<div class="toc">
          |  <ul>
          |    <li><a href="foo.html#a" class="header">A</a>
          |      <ul>
          |        <li><a href="foo.html#b" class="header">B</a></li>
          |      </ul>
          |    </li>
          |    <li><a href="bar.html" class="page">Bar</a>
          |      <ul>
          |        <li><a href="bar.html#a" class="header">A</a>
          |          <ul>
          |            <li><a href="bar.html#b" class="header">B</a></li>
          |          </ul>
          |        </li>
          |      </ul>
          |    </li>
          |  </ul>
          |</div>
          |<h2><a href="#a" name="a" class="anchor"><span class="anchor-link"></span></a>A</h2>
          |<h3><a href="#b" name="b" class="anchor"><span class="anchor-link"></span></a>B</h3>
        """,
      "bar.html" -> """
          |<h1><a href="#bar" name="bar" class="anchor"><span class="anchor-link"></span></a>Bar</h1>
          |<h2><a href="#a" name="a" class="anchor"><span class="anchor-link"></span></a>A</h2>
          |<h3><a href="#b" name="b" class="anchor"><span class="anchor-link"></span></a>B</h3>
        """
    )
  }

  it should "support depth attribute" in {
    markdownPages("a.md" -> """
      |# A
      |@@ toc { depth=2 }
      |## B
      |### C
      |#### D
      |### E
      |## F
      |### G
      |#### H
    """) shouldEqual htmlPages("a.html" -> """
      |<h1><a href="#a" name="a" class="anchor"><span class="anchor-link"></span></a>A</h1>
      |<div class="toc">
      |  <ul>
      |    <li><a href="a.html#b" class="header">B</a>
      |      <ul>
      |        <li><a href="a.html#c" class="header">C</a></li>
      |        <li><a href="a.html#e" class="header">E</a></li>
      |      </ul>
      |    </li>
      |    <li><a href="a.html#f" class="header">F</a>
      |      <ul>
      |        <li><a href="a.html#g" class="header">G</a></li>
      |      </ul>
      |    </li>
      |  </ul>
      |</div>
      |<h2><a href="#b" name="b" class="anchor"><span class="anchor-link"></span></a>B</h2>
      |<h3><a href="#c" name="c" class="anchor"><span class="anchor-link"></span></a>C</h3>
      |<h4><a href="#d" name="d" class="anchor"><span class="anchor-link"></span></a>D</h4>
      |<h3><a href="#e" name="e" class="anchor"><span class="anchor-link"></span></a>E</h3>
      |<h2><a href="#f" name="f" class="anchor"><span class="anchor-link"></span></a>F</h2>
      |<h3><a href="#g" name="g" class="anchor"><span class="anchor-link"></span></a>G</h3>
      |<h4><a href="#h" name="h" class="anchor"><span class="anchor-link"></span></a>H</h4>
    """)
  }

  it should "only display sub-headers when positioned deeper" in {
    markdownPages("a.md" -> """
        |# A
        |## B
        |@@ toc
        |### C
        |#### D
        |### E
        |## F
        |### G
        |#### H
      """) shouldEqual htmlPages("a.html" -> """
        |<h1><a href="#a" name="a" class="anchor"><span class="anchor-link"></span></a>A</h1>
        |<h2><a href="#b" name="b" class="anchor"><span class="anchor-link"></span></a>B</h2>
        |<div class="toc">
        |  <ul>
        |    <li><a href="a.html#c" class="header">C</a>
        |      <ul>
        |        <li><a href="a.html#d" class="header">D</a></li>
        |      </ul>
        |    </li>
        |    <li><a href="a.html#e" class="header">E</a></li>
        |  </ul>
        |</div>
        |<h3><a href="#c" name="c" class="anchor"><span class="anchor-link"></span></a>C</h3>
        |<h4><a href="#d" name="d" class="anchor"><span class="anchor-link"></span></a>D</h4>
        |<h3><a href="#e" name="e" class="anchor"><span class="anchor-link"></span></a>E</h3>
        |<h2><a href="#f" name="f" class="anchor"><span class="anchor-link"></span></a>F</h2>
        |<h3><a href="#g" name="g" class="anchor"><span class="anchor-link"></span></a>G</h3>
        |<h4><a href="#h" name="h" class="anchor"><span class="anchor-link"></span></a>H</h4>
      """)
  }

  it should "add extra class attributes" in {
    markdownPages("a.md" -> """
      |# A
      |@@ toc { .foo .bar }
      |## B
      |### C
    """) shouldEqual htmlPages("a.html" -> """
      |<h1><a href="#a" name="a" class="anchor"><span class="anchor-link"></span></a>A</h1>
      |<div class="toc foo bar">
      |  <ul>
      |    <li><a href="a.html#b" class="header">B</a>
      |      <ul>
      |        <li><a href="a.html#c" class="header">C</a></li>
      |      </ul>
      |    </li>
      |  </ul>
      |</div>
      |<h2><a href="#b" name="b" class="anchor"><span class="anchor-link"></span></a>B</h2>
      |<h3><a href="#c" name="c" class="anchor"><span class="anchor-link"></span></a>C</h3>
    """)
  }

  it should "support ordered attribute" in {
    markdownPages("a.md" -> """
      |# A
      |@@ toc { ordered=on }
      |## B
      |### C
    """) shouldEqual htmlPages("a.html" -> """
      |<h1><a href="#a" name="a" class="anchor"><span class="anchor-link"></span></a>A</h1>
      |<div class="toc">
      |  <ol>
      |    <li><a href="a.html#b" class="header">B</a>
      |      <ol>
      |        <li><a href="a.html#c" class="header">C</a></li>
      |      </ol>
      |    </li>
      |  </ol>
      |</div>
      |<h2><a href="#b" name="b" class="anchor"><span class="anchor-link"></span></a>B</h2>
      |<h3><a href="#c" name="c" class="anchor"><span class="anchor-link"></span></a>C</h3>
    """)
  }

}
