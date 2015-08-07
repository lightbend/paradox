/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

class RefDirectiveSpec extends MarkdownBaseSpec {

  def testMarkdown(text: String, pagePath: String = "page.md", testPath: String = "test.md"): Map[String, String] = markdownPages(
    pagePath -> s"""
      |@@@ index
      |* [test](${Path.basePath(pagePath)}${testPath})
      |@@@
    """,
    testPath -> text
  )

  def testHtml(text: String, pagePath: String = "page.html", testPath: String = "test.html"): Map[String, String] = {
    htmlPages(pagePath -> "", testPath -> text)
  }

  "Ref directive" should "create links with html extension" in {
    testMarkdown("@ref[Page](page.md)") shouldEqual testHtml("""<p><a href="page.html">Page</a></p>""")
  }

  it should "support 'ref:' as an alternative name" in {
    testMarkdown("@ref:[Page](page.md)") shouldEqual testHtml("""<p><a href="page.html">Page</a></p>""")
  }

  it should "handle relative links correctly" in {
    testMarkdown("@ref:[Page](../a/page.md)", pagePath = "a/page.md", testPath = "b/test.md") shouldEqual
      testHtml("""<p><a href="../a/page.html">Page</a></p>""", pagePath = "a/page.html", testPath = "b/test.html")
  }

  it should "handle anchored links correctly" in {
    testMarkdown("@ref:[Page](page.md#header)") shouldEqual testHtml("""<p><a href="page.html#header">Page</a></p>""")
  }

  it should "retain whitespace before or after" in {
    testMarkdown("This @ref:[Page](page.md#header) is linked.") shouldEqual
      testHtml("""<p>This <a href="page.html#header">Page</a> is linked.</p>""")
  }

  it should "parse but ignore directive attributes" in {
    testMarkdown("This @ref:[Page](page.md#header) { .ref a=1 } is linked.") shouldEqual
      testHtml("""<p>This <a href="page.html#header">Page</a> is linked.</p>""")
  }

  it should "throw link exceptions for invalid references" in {
    the[RefDirective.LinkException] thrownBy {
      markdown("@ref[Page](page.md)")
    } should have message "Unknown page [page.html] referenced from [test.html]"
  }

}
