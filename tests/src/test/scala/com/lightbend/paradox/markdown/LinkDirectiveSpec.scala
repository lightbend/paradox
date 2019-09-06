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

import com.lightbend.paradox.ParadoxException

class LinkDirectiveSpec extends MarkdownBaseSpec {

  private implicit val context = writerContextWithProperties("page.variable" -> "https://page")

  def testMarkdown(text: String, pagePath: String = "page.md", testPath: String = "test.md"): Map[String, String] = markdownPages(
    testPath -> text
  )

  def testHtml(text: String, pagePath: String = "page.html", testPath: String = "test.html"): Map[String, String] = {
    htmlPages(testPath -> text)
  }

  "Link directive" should "create links" in {
    testMarkdown("@link[External page](https://domain.com/page.html)") shouldEqual testHtml("""<p><a href="https://domain.com/page.html" title="External page">External page</a></p>""")
  }

  it should "support 'link:' as an alternative name" in {
    testMarkdown("@link:[External page](https://domain.com/page.html)") shouldEqual testHtml("""<p><a href="https://domain.com/page.html" title="External page">External page</a></p>""")
  }

  it should "handle anchored links correctly" in {
    testMarkdown("@link:[External page](https://domain.com/page.html#anchor)") shouldEqual testHtml("""<p><a href="https://domain.com/page.html#anchor" title="External page">External page</a></p>""")
  }

  it should "retain whitespace before or after" in {
    testMarkdown("This @link:[Page](external.pdf) is linked.") shouldEqual
      testHtml("""<p>This <a href="external.pdf" title="Page">Page</a> is linked.</p>""")
  }

  it should "use the `open` attributes" in {
    testMarkdown("This @link:[Page](page.pdf) { open=new } is linked.") shouldEqual
      testHtml("""<p>This <a href="page.pdf" title="Page" target="_blank" rel="noopener noreferrer">Page</a> is linked.</p>""")
  }

  it should "support referenced links with implicit key" in {
    testMarkdown(
      """This @link:[SBT] { .ref a=1 } is linked.
        |
        |  [SBT]: https://scala-sbt.org
      """.stripMargin) shouldEqual testHtml("""<p>This <a href="https://scala-sbt.org" title="SBT">SBT</a> is linked.</p>""")
  }

  it should "support referenced links with empty key" in {
    testMarkdown(
      """This @link:[SBT][] is linked.
        |
        |  [SBT]: https://scala-sbt.org
      """.stripMargin) shouldEqual testHtml("""<p>This <a href="https://scala-sbt.org" title="SBT">SBT</a> is linked.</p>""")
  }

  it should "support referenced links with defined key" in {
    testMarkdown(
      """This @link:[Page][123] { .ref a=1 } is linked.
        |
        |  [123]: https://scala-sbt.org
      """.stripMargin) shouldEqual testHtml("""<p>This <a href="https://scala-sbt.org" title="Page">Page</a> is linked.</p>""")
  }

  it should "throw link exceptions for invalid reference keys" in {
    the[ParadoxException] thrownBy {
      markdown("@ref[Page][123]")
    } should have message "Undefined reference key [123]"
  }

  it should "support variables in link paths" in {
    testMarkdown("@link[Page]($page.variable$.html)") shouldEqual testHtml("""<p><a href="https://page.html" title="Page">Page</a></p>""")
  }

}
