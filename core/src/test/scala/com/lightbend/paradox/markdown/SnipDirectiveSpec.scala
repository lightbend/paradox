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

class SnipDirectiveSpec extends MarkdownBaseSpec {

  val testProperties = Map("version" -> "1.2.3")

  implicit val context: Location[Page] => Writer.Context = { loc =>
    writerContext(loc).copy(properties = testProperties)
  }

  "The `snip` directive" should "render code snippets" in {
    markdown("""@@snip[example.scala](core/src/test/scala/com/lightbend/paradox/markdown/example.scala) {#example }""") shouldEqual html("""
      |<pre class="prettyprint">
      |<code class="language-scala">
      |object example extends App {
      |  println("Hello, World!")
      |}</code>
      |</pre>""")
  }

  it should "render code snippets in definition lists" in {
    markdown("""
      |Scala
      |:   @@snip[example.scala](core/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #example }
      |
      |Java
      |:   @@snip[example2.java](core/src/test/scala/com/lightbend/paradox/markdown/example2.java) { #example2 }
      |""") shouldEqual html("""
      |<dl>
      |<dt>Scala</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-scala">
      |object example extends App {
      |  println("Hello, World!")
      |}</code>
      |</pre>
      |</dd>
      |<dt>Java</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-java">
      |public class example2 {
      |    public static void main(String[] args) {
      |        System.out.println("Hello, World");
      |    }
      |}</code>
      |</pre>
      |</dd>
      |</dl>""")
  }

  it should "support a custom id and custom CSS classes at the same time" in {
    markdown("""
      |@@@ div { #yeah .red .blue }
      |Simple sentence here.
      |@@@""") shouldEqual html("""
      |<div id="yeah" class="red blue">
      |<p>Simple sentence here.</p>
      |</div>""")
  }

}
