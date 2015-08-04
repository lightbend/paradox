/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

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
          |  <ol>
          |    <li><a href="foo.html#a">A</a>
          |      <ol>
          |        <li><a href="foo.html#b">B</a></li>
          |      </ol>
          |    </li>
          |    <li><a href="bar.html">Bar</a>
          |      <ol>
          |        <li><a href="bar.html#a">A</a>
          |          <ol>
          |            <li><a href="bar.html#b">B</a></li>
          |          </ol>
          |        </li>
          |      </ol>
          |    </li>
          |  </ol>
          |</div>
          |<h2><a href="#a" name="a" class="anchor"><span class="anchor-link"></span></a>A</h2>
          |<h3><a href="#b" name="b" class="anchor"><span class="anchor-link"></span></a>B</h3>
        """,
        "bar.html" -> """
          |<h1><a href="#bar" name="bar" class="anchor"><span class="anchor-link"></span></a>Bar</h1>
          |<h2><a href="#a" name="a" class="anchor"><span class="anchor-link"></span></a>A</h2>
          |<h3><a href="#b" name="b" class="anchor"><span class="anchor-link"></span></a>B</h3>
        """)
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
      |  <ol>
      |    <li><a href="a.html#b">B</a>
      |      <ol>
      |        <li><a href="a.html#c">C</a></li>
      |        <li><a href="a.html#e">E</a></li>
      |      </ol>
      |    </li>
      |    <li><a href="a.html#f">F</a>
      |      <ol>
      |        <li><a href="a.html#g">G</a></li>
      |      </ol>
      |    </li>
      |  </ol>
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
        |  <ol>
        |    <li><a href="a.html#c">C</a>
        |      <ol>
        |        <li><a href="a.html#d">D</a></li>
        |      </ol>
        |    </li>
        |    <li><a href="a.html#e">E</a></li>
        |  </ol>
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
      |  <ol>
      |    <li><a href="a.html#b">B</a>
      |      <ol>
      |        <li><a href="a.html#c">C</a></li>
      |      </ol>
      |    </li>
      |  </ol>
      |</div>
      |<h2><a href="#b" name="b" class="anchor"><span class="anchor-link"></span></a>B</h2>
      |<h3><a href="#c" name="c" class="anchor"><span class="anchor-link"></span></a>C</h3>
    """)
  }

  it should "support ordered attribute" in {
    markdownPages("a.md" -> """
      |# A
      |@@ toc { ordered=off }
      |## B
      |### C
    """) shouldEqual htmlPages("a.html" -> """
      |<h1><a href="#a" name="a" class="anchor"><span class="anchor-link"></span></a>A</h1>
      |<div class="toc">
      |  <ul>
      |    <li><a href="a.html#b">B</a>
      |      <ul>
      |        <li><a href="a.html#c">C</a></li>
      |      </ul>
      |    </li>
      |  </ul>
      |</div>
      |<h2><a href="#b" name="b" class="anchor"><span class="anchor-link"></span></a>B</h2>
      |<h3><a href="#c" name="c" class="anchor"><span class="anchor-link"></span></a>C</h3>
    """)
  }

}
