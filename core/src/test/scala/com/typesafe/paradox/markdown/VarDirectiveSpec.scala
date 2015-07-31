/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

import com.typesafe.paradox.tree.Tree.Location

class VarDirectiveSpec extends MarkdownBaseSpec {

  val testProperties = Map("version" -> "1.2.3")

  implicit val context: Location[Page] => Writer.Context = { loc =>
    Writer.Context(loc, properties = testProperties)
  }

  "Var directive" should "insert property values" in {
    markdown("@var[version]") shouldEqual html("<p>1.2.3</p>")
  }

  it should "support 'var:' as an alternative name" in {
    markdown("@var:[version]") shouldEqual html("<p>1.2.3</p>")
  }

  it should "work within markdown inlines" in {
    markdown("Version is *@var[version]*.") shouldEqual html("<p>Version is <em>1.2.3</em>.</p>")
  }

  it should "work within markdown blocks" in {
    markdown("- @var[version]") shouldEqual html("<ul><li>1.2.3</li></ul>")
  }

  it should "work within link labels" in {
    markdown("[Version @var[version]](version.html)") shouldEqual html("""<p><a href="version.html">Version 1.2.3</a></p>""")
  }

  it should "work within other inline directives" in {
    markdown("@ref:[Version @var[version]](version.md)") shouldEqual html("""<p><a href="version.html">Version 1.2.3</a></p>""")
  }

}
