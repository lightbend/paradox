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

import com.lightbend.paradox.tree.Tree.{ Forest, Location }

class NavigationSpec extends MarkdownBaseSpec {

  val site = Location.forest(pages(
    "index.md" -> """
      |@@@ index
      |* [1](1.md)
      |* [2](2.md)
      |    - [a](2/a.md)
      |    - [b](2/b.md)
      |* [3](3.md)
      |    - [a](3/a.md)
      |        + [i](3/a/i.md)
      |        + [ii](3/a/ii.md)
      |    - [b](3/b.md)
      |        + [i](3/b/i.md)
      |        + [ii](3/b/ii.md)
      |@@@
    """,
    "1.md" -> """
      |# 1
    """,
    "2.md" -> """
      |# 2
    """,
    "2/a.md" -> """
      |@@@ div { .group-scala }
      |# 2/a
      |@@@
    """,
    "2/b.md" -> """
      |# 2/b
    """,
    "3.md" -> """
      |# 3
    """,
    "3/a.md" -> """
      |# 3/a
    """,
    "3/a/i.md" -> """
      |# 3/a/i
    """,
    "3/a/ii.md" -> """
      |# 3/a/ii
    """,
    "3/b.md" -> """
      |# 3/b
    """,
    "3/b/i.md" -> """
      |# 3/b/i
      |## A
      |### B
      |## C
      |### D
    """,
    "3/b/ii.md" -> """
      |# 3/b/ii
      |## A
      |### B
      |## C
      |### D
    """
  ))

  "TableOfContents" should "create full navigation including everything" in {
    navigation(
      new TableOfContents(pages = true, headers = true, ordered = false, maxDepth = 6, maxExpandDepth = None),
      site
    ) shouldEqual html("""
      |<ul>
      |<li><a href="1.html" class="page">1</a></li>
      |<li><a href="2.html" class="page">2</a>
      |<ul>
      |<li><a href="2/a.html" class="page group-scala">2/a</a></li>
      |<li><a href="2/b.html" class="page">2/b</a></li>
      |</ul>
      |</li>
      |<li><a href="3.html" class="page">3</a>
      |<ul>
      |<li><a href="3/a.html" class="page">3/a</a>
      |<ul>
      |<li><a href="3/a/i.html" class="page">3/a/i</a></li>
      |<li><a href="3/a/ii.html" class="page">3/a/ii</a></li>
      |</ul>
      |</li>
      |<li><a href="3/b.html" class="page">3/b</a>
      |<ul>
      |<li><a href="3/b/i.html" class="page">3/b/i</a>
      |<ul>
      |<li><a href="3/b/i.html#a" class="header">A</a>
      |<ul>
      |<li><a href="3/b/i.html#b" class="header">B</a></li>
      |</ul>
      |</li>
      |<li><a href="3/b/i.html#c" class="header">C</a>
      |<ul>
      |<li><a href="3/b/i.html#d" class="header">D</a></li>
      |</ul>
      |</li>
      |</ul>
      |</li>
      |<li><a href="3/b/ii.html" class="page">3/b/ii</a>
      |<ul>
      |<li><a href="3/b/ii.html#a" class="header">A</a>
      |<ul>
      |<li><a href="3/b/ii.html#b" class="header">B</a></li>
      |</ul>
      |</li>
      |<li><a href="3/b/ii.html#c" class="header">C</a>
      |<ul>
      |<li><a href="3/b/ii.html#d" class="header">D</a></li>
      |</ul>
      |</li>
      |</ul>
      |</li>
      |</ul>
      |</li>
      |</ul>
      |</li>
      |</ul>
    """)
  }

  it should "create navigation for pages up to max depth" in {
    navigation(
      new TableOfContents(pages = true, headers = false, ordered = false, maxDepth = 2, maxExpandDepth = None),
      site
    ) shouldEqual html("""
      |<ul>
      |<li><a href="1.html" class="page">1</a></li>
      |<li><a href="2.html" class="page">2</a>
      |<ul>
      |<li><a href="2/a.html" class="page group-scala">2/a</a></li>
      |<li><a href="2/b.html" class="page">2/b</a></li>
      |</ul>
      |</li>
      |<li><a href="3.html" class="page">3</a>
      |<ul>
      |<li><a href="3/a.html" class="page">3/a</a></li>
      |<li><a href="3/b.html" class="page">3/b</a></li>
      |</ul>
      |</li>
      |</ul>
    """)
  }

  it should "create auto-expanding navigation for pages up to max depths (at level one)" in {
    navigation(
      new TableOfContents(pages = true, headers = false, ordered = false, maxDepth = 1, maxExpandDepth = Some(1)),
      site
    ) shouldEqual html("""
      |<ul>
      |<li><a href="1.html" class="page">1</a></li>
      |<li><a href="2.html" class="page">2</a></li>
      |<li><a href="3.html" class="page">3</a></li>
      |</ul>
    """)
  }

  it should "create auto-expanding navigation for pages up to max depths (at level two)" in {
    navigation(
      new TableOfContents(pages = true, headers = false, ordered = false, maxDepth = 1, maxExpandDepth = Some(1)),
      site.get.rightmostChild
    ) shouldEqual html("""
      |<ul>
      |<li><a href="1.html" class="page">1</a></li>
      |<li><a href="2.html" class="page">2</a></li>
      |<li><a href="3.html" class="active page">3</a>
      |<ul>
      |<li><a href="3/a.html" class="page">3/a</a></li>
      |<li><a href="3/b.html" class="page">3/b</a></li>
      |</ul>
      |</li>
      |</ul>
    """)
  }

  it should "create auto-expanding navigation for pages up to max depths (at level three)" in {
    navigation(
      new TableOfContents(pages = true, headers = false, ordered = false, maxDepth = 1, maxExpandDepth = Some(1)),
      site.get.rightmostChild.get.rightmostChild
    ) shouldEqual html("""
      |<ul>
      |<li><a href="../1.html" class="page">1</a></li>
      |<li><a href="../2.html" class="page">2</a></li>
      |<li><a href="../3.html" class="page">3</a>
      |<ul>
      |<li><a href="../3/a.html" class="page">3/a</a></li>
      |<li><a href="../3/b.html" class="active page">3/b</a>
      |<ul>
      |<li><a href="../3/b/i.html" class="page">3/b/i</a></li>
      |<li><a href="../3/b/ii.html" class="page">3/b/ii</a></li>
      |</ul>
      |</li>
      |</ul>
      |</li>
      |</ul>
    """)
  }

  it should "create auto-expanding navigation for pages up to max depths (at level four)" in {
    navigation(
      new TableOfContents(pages = true, headers = false, ordered = false, maxDepth = 1, maxExpandDepth = Some(1)),
      site.get.rightmostChild.get.rightmostChild.get.rightmostChild
    ) shouldEqual html("""
      |<ul>
      |<li><a href="../../1.html" class="page">1</a></li>
      |<li><a href="../../2.html" class="page">2</a></li>
      |<li><a href="../../3.html" class="page">3</a>
      |<ul>
      |<li><a href="../../3/a.html" class="page">3/a</a></li>
      |<li><a href="../../3/b.html" class="page">3/b</a>
      |<ul>
      |<li><a href="../../3/b/i.html" class="page">3/b/i</a></li>
      |<li><a href="../../3/b/ii.html" class="active page">3/b/ii</a></li>
      |</ul>
      |</li>
      |</ul>
      |</li>
      |</ul>
    """)
  }

  it should "create auto-expanding navigation for pages and headers up to max depths (at level four)" in {
    navigation(
      new TableOfContents(pages = true, headers = true, ordered = false, maxDepth = 1, maxExpandDepth = Some(1)),
      site.get.rightmostChild.get.rightmostChild.get.rightmostChild
    ) shouldEqual html("""
      |<ul>
      |<li><a href="../../1.html" class="page">1</a></li>
      |<li><a href="../../2.html" class="page">2</a></li>
      |<li><a href="../../3.html" class="page">3</a>
      |<ul>
      |<li><a href="../../3/a.html" class="page">3/a</a></li>
      |<li><a href="../../3/b.html" class="page">3/b</a>
      |<ul>
      |<li><a href="../../3/b/i.html" class="page">3/b/i</a></li>
      |<li><a href="../../3/b/ii.html#3-b-ii" class=
      |"active page">3/b/ii</a>
      |<ul>
      |<li><a href="../../3/b/ii.html#a" class="header">A</a></li>
      |<li><a href="../../3/b/ii.html#c" class="header">C</a></li>
      |</ul>
      |</li>
      |</ul>
      |</li>
      |</ul>
      |</li>
      |</ul>
    """)
  }

  it should "create auto-expanding navigation for ancestor pages only if expand depth is 0" in {
    navigation(
      new TableOfContents(pages = true, headers = false, ordered = false, maxDepth = 1, maxExpandDepth = Some(0)),
      site.get.rightmostChild.get.rightmostChild
    ) shouldEqual html("""
      |<ul>
      |<li><a href="../1.html" class="page">1</a></li>
      |<li><a href="../2.html" class="page">2</a></li>
      |<li><a href="../3.html" class="page">3</a>
      |<ul>
      |<li><a href="../3/a.html" class="page">3/a</a></li>
      |<li><a href="../3/b.html" class="active page">3/b</a></li>
      |</ul>
      |</li>
      |</ul>
    """)
  }

  def navigation(toc: TableOfContents, location: Option[Location[Page]])(implicit context: Location[Page] => Writer.Context = writerContext): String = {
    location match {
      case Some(loc) => normalize(markdownWriter.writeToc(toc.root(loc), context(loc)))
      case None      => ""
    }
  }

}
