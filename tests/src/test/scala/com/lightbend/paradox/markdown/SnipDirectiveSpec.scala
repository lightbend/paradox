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

import java.io.File

import com.lightbend.paradox.tree.Tree.Location

class SnipDirectiveSpec extends MarkdownBaseSpec {

  val testProperties = Map("version" -> "1.2.3")

  implicit val context: Location[Page] => Writer.Context = { loc =>
    writerContext(loc).copy(properties = testProperties)
  }

  "The `snip` directive" should "render code snippets" in {
    markdown("""@@snip[example.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #example }""") shouldEqual html("""
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
      |:   @@snip[example.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #example }
      |
      |Java
      |:   @@snip[example2.java](tests/src/test/scala/com/lightbend/paradox/markdown/example2.java) { #example2 }
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

  it should "support custom CSS classes" in {
    markdown("""
      |@@snip[example2.java](tests/src/test/scala/com/lightbend/paradox/markdown/example2.java){ #example2 .red .blue }
      |""") shouldEqual html("""
      |<pre class="prettyprint red blue">
      |<code class="language-java">
      |public class example2 {
      |    public static void main(String[] args) {
      |        System.out.println("Hello, World");
      |    }
      |}</code>
      |</pre>""")
  }

  it should "trim indentation from snippets" in {
    markdown("""@@snip[example.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #indented-example }""") shouldEqual html("""
      |<pre class="prettyprint">
      |<code class="language-scala">
      |case object Dent
      |  case object DoubleDent</code>
      |</pre>""")
  }

  it should "not truncate snippets" in {
    markdown("""@@snip[example.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #multi-indented-example }""") shouldEqual html("""
      |<pre class="prettyprint">
      |<code class="language-scala">
      |object AnotherIndentedExample {
      |  def rendered(): Unit = {
      |  }
      |}
      |class AnotherClass
      |</code>
      |</pre>""")
  }

  it should "add link to source" in {
    implicit val context = writerContextWithProperties(
      "github.base_url" -> "https://github.com/lightbend/paradox/tree/v0.2.1",
      "github.root.base_dir" -> new File(".").getAbsoluteFile.getParent,
      "snip.github_link" -> "true")

    markdown("""@@snip[example.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #example }""") shouldEqual html(
      """<pre class="prettyprint">
        |<a class="icon go-to-source" href="https://github.com/lightbend/paradox/tree/v0.2.1/tests/src/test/scala/com/lightbend/paradox/markdown/example.scala#L28-L30" target="_blank" title="Go to snippet source"></a><code class="language-scala">
        |object example extends App {
        |  println("Hello, World!")
        |}</code>
        |</pre>
        |""")
  }

  it should "add link to source with placeholders" in {
    implicit val context = writerContextWithProperties(
      "github.base_url" -> "https://github.com/lightbend/paradox/tree/v0.2.1",
      "github.root.base_dir" -> new File(".").getAbsoluteFile.getParent,
      "snip.github_link" -> "true",
      "snip.test.base_dir" -> "tests/src/test/scala/com/lightbend/paradox/markdown")

    markdown("""@@snip[example.scala]($test$/example.scala) { #example }""") shouldEqual html(
      """<pre class="prettyprint">
        |<a class="icon go-to-source" href="https://github.com/lightbend/paradox/tree/v0.2.1/tests/src/test/scala/com/lightbend/paradox/markdown/example.scala#L28-L30" target="_blank" title="Go to snippet source"></a><code class="language-scala">
        |object example extends App {
        |  println("Hello, World!")
        |}</code>
        |</pre>
        |""")
  }

  it should "not link to source if config says so" in {
    implicit val context = writerContextWithProperties(
      "github.base_url" -> "https://github.com/lightbend/paradox/tree/v0.2.1",
      "github.root.base_dir" -> new File(".").getAbsoluteFile.getParent,
      "snip.github_link" -> "false")

    markdown("""@@snip[example.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #example }""") shouldEqual html(
      """<pre class="prettyprint">
        |<code class="language-scala">
        |object example extends App {
        |  println("Hello, World!")
        |}</code>
        |</pre>""")
  }
}
