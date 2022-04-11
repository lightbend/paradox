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

import com.lightbend.paradox.tree.Tree.Location

class IncludeDirectiveSpec extends MarkdownBaseSpec {

  val testProperties = Map("version" -> "1.2.3")

  implicit val context: Location[Page] => Writer.Context = { loc =>
    writerContext(loc).copy(properties = testProperties)
  }

  "The `include` directive" should "include and render full markdown files" in {
    markdown("""@@include(tests/src/test/resources/include.md)""") shouldEqual html("""
      |<p><strong>An include</strong></p>
      |<p>This file should be included by IncludeDirectiveSpec</p>""")
  }

  it should "include markdown files with a label" in {
    markdown("""@@include[include.md](tests/src/test/resources/include.md)""") shouldEqual html("""
      |<p><strong>An include</strong></p>
      |<p>This file should be included by IncludeDirectiveSpec</p>""")
  }

  it should "include partial files when specified" in {
    markdown("""@@include(tests/src/test/resources/include-snip.md) { #section } """) shouldEqual html("""
      |<p>Only this part should be included.</p>""")
  }

  it should "include nested snippets rendered in the context of the snippet" in {
    markdown("""@@include(tests/src/test/resources/include-nested.md)""") shouldEqual html("""
      |<p><strong>This should demonstrate nested includes</strong></p>
      |<p><strong>An include</strong></p>
      |<p>This file should be included by IncludeDirectiveSpec</p>""")
  }

  it should "include nested code snippets" in {
    markdown("""@@include(tests/src/test/resources/include-code-snip.md)""") shouldEqual html("""
      |<pre class="prettyprint">
      |<button class="snippet-button copy-snippet" title="Copy snippet to clipboard">copy</button><code class="language-conf">
      |a = b</code>
      |</pre>""")
  }

  it should "include headers from nested snippets in the toc" in {
    markdown("""
        |# Page heading
        |
        |This text appears here to push down the toc so to ensure that the headers below
        |calculation works.
        |
        |@@toc { depth=1 }
        |
        |@@include(tests/src/test/resources/headers.md)
        |
        |## Heading 3
        |
        |""") should include(html("""
           |<div class="toc">
           |  <ul>
           |    <li><a href="test.html#heading-1" class="header">Heading 1</a></li>
           |    <li><a href="test.html#heading-2" class="header">Heading 2</a></li>
           |    <li><a href="test.html#heading-3" class="header">Heading 3</a></li>
           |  </ul>
           |</div>"""))
  }

  it should "include headers from outer snippets in a nested toc" in {
    markdown("""
        |# Page heading
        |
        |## Above toc
        |
        |@@include(tests/src/test/resources/toc.md)
        |
        |## Heading 1
        |## Heading 2
        |## Heading 3
        |
        |""") should include(html("""
           |<div class="toc">
           |  <ul>
           |    <li><a href="test.html#heading-1" class="header">Heading 1</a></li>
           |    <li><a href="test.html#heading-2" class="header">Heading 2</a></li>
           |    <li><a href="test.html#heading-3" class="header">Heading 3</a></li>
           |  </ul>
           |</div>"""))
  }

}
