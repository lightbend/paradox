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

class WrapDirectiveSpec extends MarkdownBaseSpec {

  val testProperties = Map("version" -> "1.2.3")

  implicit val context: Location[Page] => Writer.Context = { loc =>
    writerContext(loc).copy(properties = testProperties)
  }

  "The `wrap` directive" should "render wrapping `div`s" in {
    markdown(
      """
        |@@@ div
        |Simple sentence here.
        |@@@""") shouldEqual html(
        """
        |<div>
        |<p>Simple sentence here.</p>
        |</div>""")
  }

  it should "render the example from the docs" in {
    markdown(
      """
        |@@@ div { #foo .bar .baz }
        |
        |Inner **markdown** content.
        |
        |@@@""") shouldEqual html(
        """
        |<div id="foo" class="bar baz">
        |<p>Inner <strong>markdown</strong> content.</p>
        |</div>""")
  }

  it should "support a custom id and custom CSS classes at the same time" in {
    markdown(
      """
        |@@@ div { #yeah .red .blue }
        |Simple sentence here.
        |@@@""") shouldEqual html(
        """
        |<div id="yeah" class="red blue">
        |<p>Simple sentence here.</p>
        |</div>""")
  }

  it should "render nested blocks" in {
    markdown(
      """
        |@@@ div
        |Simple sentence here.
        |
        |@@@@ warning
        |
        |warning inside a div
        |
        |@@@@
        |
        |@@@""") shouldEqual html(
        """
        |<div>
        |<p>Simple sentence here.</p>
        |<div class="callout warning">
        |<div class="callout-title">Warning</div>
        |<p>warning inside a div</p>
        |</div>
        |</div>""")
  }

  it should "work with raw verbatim" in {
    markdown(
      """
        |@@@div { .divStyleClass }
        |```raw
        |<blink>Hello?</blink>
        |```
        |@@@
      """) shouldEqual html(
        """<div class="divStyleClass">
        |<blink>Hello?</blink>
        |</div>
      """)
    // for use in docs
    // format: off
    """
// #div-raw
@@@div { .divStyleClass }
```raw
<blink>Hello?</blink>
```
@@@
// #div-raw
             """
    // format: on
  }

  "Raw verbatim" should "work" in {
    markdown(
      """
        |```raw
        |<blink>Hello?</blink>
        |```
      """.stripMargin) shouldEqual html(
        """<blink>Hello?</blink>
        |""".stripMargin)

    // for use in docs
    // format: off
    """
// #raw
```raw
<blink>Hello?</blink>
```
// #raw
             """
    // format: on
  }

}
