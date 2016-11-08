/*
 * Copyright © 2015 - 2016 Lightbend, Inc. <http://www.lightbend.com>
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

class WrapDirectiveSpec extends MarkdownBaseSpec {

  val testProperties = Map("version" -> "1.2.3")

  implicit val context: Location[Page] => Writer.Context = { loc =>
    writerContext(loc).copy(properties = testProperties)
  }

  "The `wrap` directive" should "render wrapping `div`s" in {
    markdown("""
      |@@@ div
      |Simple sentence here.
      |@@@""") shouldEqual html("""
      |<div>
      |Simple sentence here.
      |</div>""")
  }

  it should "render wrapping `p`s with a custom `id`" in {
    markdown("""
      |@@@ p { #yeah }
      |Simple sentence here.
      |@@@""") shouldEqual html("""
      |<p id="yeah">
      |Simple sentence here.
      |</p>""")
  }

  it should "support a custom id and custom CSS classes at the same time" in {
    markdown("""
      |@@@ div { #yeah .red .blue }
      |Simple sentence here.
      |@@@""") shouldEqual html("""
      |<div id="yeah" class="red blue">
      |Simple sentence here.
      |</div>""")
  }

}
