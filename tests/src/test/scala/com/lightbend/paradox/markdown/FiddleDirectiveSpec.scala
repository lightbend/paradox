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

class FiddleDirectiveSpec extends MarkdownBaseSpec {

  "Fiddle directive" should "generate fiddle integration code" in {
    markdown(
      """@@fiddle[FiddelDirectiveSpec.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #example }"""
    ) shouldEqual html("""
      |<div data-scalafiddle="true">
      |<pre class="prettyprint">
      |<code class="language-scala">
      |object example extends App {
      |  println("Hello, World!")
      |}</code>
      |</pre>
      |</div>
      |<script defer="true" src="https://embed.scalafiddle.io/integration.js">
      |</script>""")
  }

  it should "parse and apply optional parameters" in {
    val params = Seq(
      "prefix" -> "'import io.circe._,io.circe.generic.auto._,io.circe.parser._,io.circe.syntax._'",
      "dependency" -> "'io.circe %%% circe-core % 0.8.0,io.circe %%% circe-generic % 0.8.0,io.circe %%% circe-parser % 0.8.0'",
      "scalaversion" -> "2.11",
      "template" -> "Circe",
      "theme" -> "dark",
      "minheight" -> "300",
      "layout" -> "v75"
    )

    val markdownParams =
      params
        .map { case (k, v) =>
          s"""$k="$v""""
        }
        .mkString(" ")

    val htmlParams =
      params
        .map { case (k, v) =>
          if (v.startsWith("'")) s"""data-$k="${v.substring(1, v.length - 1)}" """ else s"""data-$k="$v" """
        }
        .mkString(" ")

    markdown(
      s"""@@fiddle[FiddelDirectiveSpec.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #example $markdownParams}"""
    ) shouldEqual html(s"""
      |<div data-scalafiddle="true" $htmlParams>
      |<pre class="prettyprint">
      |<code class="language-scala">
      |object example extends App {
      |  println("Hello, World!")
      |}</code>
      |</pre>
      |</div>
      |<script defer="true" src="https://embed.scalafiddle.io/integration.js">
      |</script>""")
  }

  it should "include multiple fiddles" in {
    markdown(
      """@@fiddle[FiddelDirectiveSpec.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #example }
      |@@fiddle[FiddelDirectiveSpec.scala](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #indented-example }"""
    ) shouldEqual html("""
      |<div data-scalafiddle="true">
      |<pre class="prettyprint">
      |<code class="language-scala">
      |object example extends App {
      |  println("Hello, World!")
      |}</code>
      |</pre>
      |</div>
      |<script defer="true" src="https://embed.scalafiddle.io/integration.js">
      |</script>
      |<div data-scalafiddle="true">
      |<pre class="prettyprint">
      |<code class="language-scala">
      |case object Dent
      |  case object DoubleDent</code>
      |</pre>
      |</div>
      |<script defer="true" src="https://embed.scalafiddle.io/integration.js">
      |</script>""")
  }

}
