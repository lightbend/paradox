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

class CalloutDirectiveSpec extends MarkdownBaseSpec {

  val testProperties = Map("version" -> "1.2.3")

  implicit val context: Location[Page] => Writer.Context = { loc =>
    writerContext(loc).copy(properties = testProperties)
  }

  "Note directive" should "render notes" in {
    markdown("""
      |@@@ note
      |Latest version is @var[version]
      |@@@
      """) shouldEqual html("""
      |<div class="callout note">
      |<div class="callout-title">Note</div>
      |<p>Latest version is 1.2.3</p>
      |</div>
      """)

    markdown("""
      |@@@ note
      |
      |Latest version is @var[version]
      |
      |@@@
      """) shouldEqual html("""
      |<div class="callout note">
      |<div class="callout-title">Note</div>
      |<p>Latest version is 1.2.3</p>
      |</div>
      """)
  }

  it should "render notes regardless of new lines at start/end" in {
    markdown("""
      |@@@ note
      |Latest version is @var[version]
      |
      |Get it while it is hot!
      |@@@
      """) shouldEqual html("""
      |<div class="callout note">
      |<div class="callout-title">Note</div>
      |<p>Latest version is 1.2.3</p>
      |<p>Get it while it is hot!</p>
      |</div>
      """)
  }

  it should "support class and title attributes" in {
    markdown("""
      |@@@ note { title="Release Notes" .release-note }
      |
      |New features:
      |
      | - Support X
      | - Make Y configurable
      |
      |Bug fixes:
      |
      | - Fix segfault in Z
      |
      |@@@
      """) shouldEqual html("""
      |<div class="callout note release-note">
      |<div class="callout-title">Release Notes</div>
      |<p>New features:</p>
      |<ul>
      |<li>Support X</li>
      |<li>Make Y configurable</li>
      |</ul>
      |<p>Bug fixes:</p>
      |<ul>
      |<li>Fix segfault in Z</li>
      |</ul>
      |</div>
      """)
  }

  "Warning directive" should "render warnings" in {
    markdown("""
      |@@@ warning
      |Version @var[version] is deprecated!
      |@@@
      """) shouldEqual html("""
      |<div class="callout warning">
      |<div class="callout-title">Warning</div>
      |<p>Version 1.2.3 is deprecated!</p>
      |</div>
      """)
  }

  it should "support class and title attributes" in {
    markdown("""
      |@@@ warning { title='Caution!' .caution }
      |
      |Version @var[version] is experimental!
      |
      |Expect breaking API changes.
      |
      |@@@
      """) shouldEqual html("""
      |<div class="callout warning caution">
      |<div class="callout-title">Caution!</div>
      |<p>Version 1.2.3 is experimental!</p>
      |<p>Expect breaking API changes.</p>
      |</div>
      """)
  }

}
