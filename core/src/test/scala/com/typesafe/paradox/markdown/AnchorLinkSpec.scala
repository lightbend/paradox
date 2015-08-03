/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

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

}
