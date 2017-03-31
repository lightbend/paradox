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
