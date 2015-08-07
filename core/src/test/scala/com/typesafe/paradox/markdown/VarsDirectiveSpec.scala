/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

import com.typesafe.paradox.tree.Tree.Location

class VarsDirectiveSpec extends MarkdownBaseSpec {

  val testProperties = Map("version" -> "1.2.3")

  implicit val context: Location[Page] => Writer.Context = { loc =>
    writerContext(loc).copy(properties = testProperties)
  }

  "Vars directive" should "insert property values" in {
    markdown("""
      |@@@vars
      |```scala
      |val version = "$version$"
      |```
      |@@@
      """) shouldEqual html("""
      |<pre class="prettyprint">
      |<code class="language-scala">
      |val version = "1.2.3"
      |</code>
      |</pre>
      """)
  }

  it should "support delimiter attributes" in {
    markdown("""
      |@@@ vars { start-delimiter="${" stop-delimiter="}" }
      |``` scala
      |val version = "${version}"
      |```
      |@@@
      """) shouldEqual html("""
      |<pre class="prettyprint">
      |<code class="language-scala">
      |val version = "1.2.3"
      |</code>
      |</pre>
      """)
  }

}
