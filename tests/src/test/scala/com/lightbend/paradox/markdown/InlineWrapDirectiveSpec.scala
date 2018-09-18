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

class InlineWrapDirectiveSpec extends MarkdownBaseSpec {

  "The inline `wrap` directive" should "render wrapping `span`s" in {
    markdown("@span[Simple sentence here]") shouldEqual html("<p><span>Simple sentence here</span></p>")
  }

  it should "render the example from the docs" in {
    markdown("This is a @span[Scala variant containing ***markdown*** and @ref:[Linking](test.md)] { .group-scala } to show.") shouldEqual html("""
      |<p>This is a <span class="group-scala">Scala variant containing <strong><em>markdown</em></strong> and <a href="test.html">Linking</a></span> to show.</p>""")
  }

  it should "support a custom id and custom CSS classes at the same time" in {
    markdown("@span[Simple sentence here.] { #yeah .red .blue }") shouldEqual html("""<p><span id="yeah" class="red blue">Simple sentence here.</div></p>""")
  }

}
