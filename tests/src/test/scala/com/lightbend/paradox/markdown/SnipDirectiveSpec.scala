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

import java.io.File

import com.lightbend.paradox.tree.Tree.Location

class SnipDirectiveSpec extends MarkdownBaseSpec {

  val testProperties = Map("version" -> "1.2.3")

  implicit val context: Location[Page] => Writer.Context = { loc =>
    writerContext(loc).copy(properties = testProperties)
  }

  "The `snip` directive" should "render code snippets" in {
    markdown(
      """@@snip[example.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #example }"""
    ) shouldEqual html("""
      |<pre class="prettyprint">
      |<button class="snippet-button copy-snippet" title="Copy snippet to clipboard">copy</button><code class="language-scala">
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
      |<button class="snippet-button copy-snippet" title="Copy snippet to clipboard">copy</button><code class="language-scala">
      |object example extends App {
      |  println("Hello, World!")
      |}</code>
      |</pre>
      |</dd>
      |<dt>Java</dt>
      |<dd>
      |<pre class="prettyprint">
      |<button class="snippet-button copy-snippet" title="Copy snippet to clipboard">copy</button><code class="language-java">
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
      |<button class="snippet-button copy-snippet" title="Copy snippet to clipboard">copy</button><code class="language-java">
      |public class example2 {
      |    public static void main(String[] args) {
      |        System.out.println("Hello, World");
      |    }
      |}</code>
      |</pre>""")
  }

  it should "trim indentation from snippets" in {
    markdown(
      """@@snip[example.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #indented-example }"""
    ) shouldEqual html("""
      |<pre class="prettyprint">
      |<button class="snippet-button copy-snippet" title="Copy snippet to clipboard">copy</button><code class="language-scala">
      |case object Dent
      |  case object DoubleDent</code>
      |</pre>""")
  }

  it should "not truncate snippets" in {
    markdown(
      """@@snip[example.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #multi-indented-example }"""
    ) shouldEqual html("""
      |<pre class="prettyprint">
      |<button class="snippet-button copy-snippet" title="Copy snippet to clipboard">copy</button><code class="language-scala">
      |object AnotherIndentedExample {
      |  def rendered(): Unit = {}
      |}
      |class AnotherClass
      |</code>
      |</pre>""")
  }

  it should "add link to source and copy button" in {
    implicit val context = writerContextWithProperties(
      "github.base_url" -> "https://github.com/lightbend/paradox/tree/v0.2.1",
      "github.root.base_dir" -> new File(".").getAbsoluteFile.getParent,
      "snip.github_link" -> "true"
    )

    markdown(
      """@@snip[example.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #example }"""
    ) shouldEqual html("""<pre class="prettyprint">
        |<button class="snippet-button copy-snippet" title="Copy snippet to clipboard">copy</button><a class="snippet-button go-to-source" href="https://github.com/lightbend/paradox/tree/v0.2.1/tests/src/test/scala/com/lightbend/paradox/markdown/example.scala#L28-L30" target="_blank" title="Go to snippet source">source</a><code class="language-scala">
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
      "snip.test.base_dir" -> "tests/src/test/scala/com/lightbend/paradox/markdown"
    )

    markdown("""@@snip[example.scala]($test$/example.scala) { #example }""") shouldEqual html("""<pre class="prettyprint">
        |<button class="snippet-button copy-snippet" title="Copy snippet to clipboard">copy</button><a class="snippet-button go-to-source" href="https://github.com/lightbend/paradox/tree/v0.2.1/tests/src/test/scala/com/lightbend/paradox/markdown/example.scala#L28-L30" target="_blank" title="Go to snippet source">source</a><code class="language-scala">
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
      "snip.github_link" -> "false"
    )

    markdown(
      """@@snip[example.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #example }"""
    ) shouldEqual html("""<pre class="prettyprint">
        |<button class="snippet-button copy-snippet" title="Copy snippet to clipboard">copy</button><code class="language-scala">
        |object example extends App {
        |  println("Hello, World!")
        |}</code>
        |</pre>""")
  }

  it should "include labels when including the whole file" in {
    markdown(
      """@@snip[example.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala)"""
    ) shouldEqual html(
      """<pre class="prettyprint">
        |<button class="snippet-button copy-snippet" title="Copy snippet to clipboard">copy</button><code class="language-scala">
        |/*
        | * Copyright &copy; 2015 - 2019 Lightbend, Inc. &lt;http://www.lightbend.com&gt;
        | *
        | * Licensed under the Apache License, Version 2.0 (the "License");
        | * you may not use this file except in compliance with the License.
        | * You may obtain a copy of the License at
        | *
        | * http://www.apache.org/licenses/LICENSE-2.0
        | *
        | * Unless required by applicable law or agreed to in writing, software
        | * distributed under the License is distributed on an "AS IS" BASIS,
        | * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        | * See the License for the specific language governing permissions and
        | * limitations under the License.
        | */
        |
        |package com.lightbend.paradox.markdown
        |
        |//#github-path-link
        |object GithubPathLink {
        |  // #github-neither-path-link
        |  type Neither[A, B] = Nothing
        |  // #github-neither-path-link
        |}
        |//#github-path-link
        |
        |//#example
        |object example extends App {
        |  println("Hello, World!")
        |}
        |//#example
        |
        |object IndentedExample {
        |  // #indented-example
        |  case object Dent
        |  // #indented-example
        |
        |  object EventMore {
        |    // #indented-example
        |    case object DoubleDent
        |    // #indented-example
        |  }
        |}
        |
        |//#multi-indented-example
        |//#some-other-anchor
        |object AnotherIndentedExample {
        |  // #multi-indented-example
        |
        |  def notRendered(): Unit = {}
        |
        |  // #multi-indented-example
        |  def rendered(): Unit = {}
        |  // #some-other-anchor
        |  // #multi-indented-example
        |
        |  def alsoNotRendered(): Unit = {}
        |  // #multi-indented-example
        |}
        |//#multi-indented-example
        |
        |//#multi-indented-example
        |class AnotherClass
        |//#multi-indented-example
        |
        |// check empty line with indented blocks!
        |// format: OFF
        |  //#multi-indented-example
        |
        |  //#multi-indented-example
        |// format: ON
        |
        |//#example-with-label
        |object Constants {
        |  val someString = " #foo "
        |}
        |//#example-with-label</code>
        |</pre>"""
    )
  }

  it should "filter labels by default" in {
    markdown(
      """@@snip[example.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #example-with-label }"""
    ) shouldEqual html(
      """<pre class="prettyprint">
        |<button class="snippet-button copy-snippet" title="Copy snippet to clipboard">copy</button><code class="language-scala">
        |object Constants {
        |}</code>
        |</pre>"""
    )
  }

  it should "allow including labels if specified" in {
    markdown(
      """@@snip[example.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #example-with-label filterLabels=false }"""
    ) shouldEqual html(
      """<pre class="prettyprint">
        |<button class="snippet-button copy-snippet" title="Copy snippet to clipboard">copy</button><code class="language-scala">
        |object Constants {
        |  val someString = " #foo "
        |}</code>
        |</pre>"""
    )
  }
}
