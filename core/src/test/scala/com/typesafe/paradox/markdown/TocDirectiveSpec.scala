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
          |<h1><a href="#foo" name="foo">Foo</a></h1>
          |<div class="toc">
          |  <ul>
          |    <li><a href="foo.html#a">A</a>
          |      <ul>
          |        <li><a href="foo.html#b">B</a></li>
          |      </ul>
          |    </li>
          |    <li><a href="bar.html">Bar</a>
          |      <ul>
          |        <li><a href="bar.html#a">A</a>
          |          <ul>
          |            <li><a href="bar.html#b">B</a></li>
          |          </ul>
          |        </li>
          |      </ul>
          |    </li>
          |  </ul>
          |</div>
          |<h2><a href="#a" name="a">A</a></h2>
          |<h3><a href="#b" name="b">B</a></h3>
        """,
        "bar.html" -> """
          |<h1><a href="#bar" name="bar">Bar</a></h1>
          |<h2><a href="#a" name="a">A</a></h2>
          |<h3><a href="#b" name="b">B</a></h3>
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
      |<h1><a href="#a" name="a">A</a></h1>
      |<div class="toc">
      |  <ul>
      |    <li><a href="a.html#b">B</a>
      |      <ul>
      |        <li><a href="a.html#c">C</a></li>
      |        <li><a href="a.html#e">E</a></li>
      |      </ul>
      |    </li>
      |    <li><a href="a.html#f">F</a>
      |      <ul>
      |        <li><a href="a.html#g">G</a></li>
      |      </ul>
      |    </li>
      |  </ul>
      |</div>
      |<h2><a href="#b" name="b">B</a></h2>
      |<h3><a href="#c" name="c">C</a></h3>
      |<h4><a href="#d" name="d">D</a></h4>
      |<h3><a href="#e" name="e">E</a></h3>
      |<h2><a href="#f" name="f">F</a></h2>
      |<h3><a href="#g" name="g">G</a></h3>
      |<h4><a href="#h" name="h">H</a></h4>
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
        |<h1><a href="#a" name="a">A</a></h1>
        |<h2><a href="#b" name="b">B</a></h2>
        |<div class="toc">
        |  <ul>
        |    <li><a href="a.html#c">C</a>
        |      <ul>
        |        <li><a href="a.html#d">D</a></li>
        |      </ul>
        |    </li>
        |    <li><a href="a.html#e">E</a></li>
        |  </ul>
        |</div>
        |<h3><a href="#c" name="c">C</a></h3>
        |<h4><a href="#d" name="d">D</a></h4>
        |<h3><a href="#e" name="e">E</a></h3>
        |<h2><a href="#f" name="f">F</a></h2>
        |<h3><a href="#g" name="g">G</a></h3>
        |<h4><a href="#h" name="h">H</a></h4>
      """)
  }

  it should "add extra class attributes" in {
    markdownPages("a.md" -> """
      |# A
      |@@ toc { .foo .bar }
      |## B
      |### C
    """) shouldEqual htmlPages("a.html" -> """
      |<h1><a href="#a" name="a">A</a></h1>
      |<div class="toc foo bar">
      |  <ul>
      |    <li><a href="a.html#b">B</a>
      |      <ul>
      |        <li><a href="a.html#c">C</a></li>
      |      </ul>
      |    </li>
      |  </ul>
      |</div>
      |<h2><a href="#b" name="b">B</a></h2>
      |<h3><a href="#c" name="c">C</a></h3>
    """)
  }

}
