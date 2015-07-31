/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

import com.typesafe.paradox.tree.Tree.Forest

class IndexSpec extends MarkdownBaseSpec {

  "Index" should "create header tree" in {
    indexed(
      "a.md" -> """
        |# A
        |## B
        |### C
        |#### D
        |### E
        |## F
        |### G
        |#### H
      """) shouldEqual index(
        """
        |- a.html
        |  - #b
        |    - #c
        |      - #d
        |    - #e
        |  - #f
        |    - #g
        |      - #h
      """)
  }

  it should "create page tree" in {
    indexed(
      "a.md" -> """
        |# A
        |@@@ index
        |* [b](b.md)
        |* [c](c.md)
        |@@@
        |## A2
        |### A3
      """,
      "b.md" -> """
        |# B
        |@@@ index
        |* [d](d.md)
        |@@@
        |## B2
      """,
      "c.md" -> """
        |# C
        |## C2
      """,
      "d.md" -> """
        |# D
        |## D2
        |### D3
      """) shouldEqual index(
        """
        |- a.html
        |  - #a2
        |    - #a3
        |  - b.html
        |    - #b2
        |    - d.html
        |      - #d2
        |        - #d3
        |  - c.html
        |    - #c2
      """)
  }

  it should "merge together indices" in {
    indexed(
      "a.md" -> """
        |# A
        |@@@ index
        |  - [b](b.md)
        |    - [c](c.md)
        |  - [d](d.md)
        |@@@
      """,
      "b.md" -> """
        |# B
        |@@@ index
        |  - [e](e.md)
        |    - [f](f.md)
        |@@@
      """,
      "c.md" -> """
        |# C
      """,
      "d.md" -> """
        |# D
      """,
      "e.md" -> """
        |# E
      """,
      "f.md" -> """
        |# F
      """) shouldEqual index(
        """
        |- a.html
        |  - b.html
        |    - c.html
        |    - e.html
        |      - f.html
        |  - d.html
      """)
  }

  def indexed(mappings: (String, String)*): String = {
    show(pages(mappings: _*))
  }

  def index(text: String): String = prepare(text)

  def show[A <: Linkable](forest: Forest[A]): String = {
    forest.map(_.map(a => show(a))).map(_.show).mkString("\n")
  }

  def show[A <: Linkable](linkable: A): String = linkable match {
    case page: Page     => page.path + "\n" + show(page.headers)
    case header: Header => header.path
  }

}
