/*
 * Copyright © 2015 - 2017 Lightbend, Inc. <http://www.lightbend.com>
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

}
