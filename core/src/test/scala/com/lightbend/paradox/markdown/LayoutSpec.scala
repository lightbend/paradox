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

package com.lightbend.paradox.template

import org.scalatest.{ FlatSpec, Matchers }
import java.io.File
import com.lightbend.paradox.markdown.{ MarkdownBaseSpec }

class LayoutSpec extends MarkdownBaseSpec {
  val defaultTemplate = """
    |<div>
    |page template
    |</div>
    """

  "Property 'layout'" should "change the default template of the page" in {
    layoutPages(
      "index.md" -> """
      |---
      |layout: dumbTemplate
      |---
      """
    )(
        "page.st" -> defaultTemplate,
        "dumbTemplate.st" -> """
      |<div>
      |content of the template
      |</div>
      """) shouldEqual htmlPages(
          "index.html" -> """
        |<div>
        |content of the template
        |</div>
        """)
  }

  it should "keep the default 'page' template if nothing is specified" in {
    layoutPages(
      "index.md" -> """
      """
    )("page.st" -> defaultTemplate) shouldEqual htmlPages(
        "index.html" -> """
        |<div>
        |page template
        |</div>
        """)
  }

  it should "only display the content of the template if no instance of the page is called" in {
    layoutPages(
      "index.md" -> """
      |---
      |layout: noPageRef
      |---
      |#Foo
      |some text
      """
    )("page.st" -> defaultTemplate,
        "noPageRef.st" -> """
      |<div>
      |No page ref
      |</div>
      """) shouldEqual htmlPages(
          "index.html" -> """
        |<div>
        |No page ref
        |</div>
        """)
  }

  it should "display the content of the page if specified" in {
    layoutPages(
      "index.md" -> """
      |---
      |layout: contentPage
      |---
      |#Foo
      |some page text
      """
    )(
        "page.st" -> defaultTemplate,
        "contentPage.st" -> """
      |<div>
      |$page.content$
      |</div>
      """
      ) shouldEqual htmlPages(
          "index.html" -> """
        |<div>
        |<h1><a href="#foo" name="foo" class="anchor"><span class="anchor-link"></span></a>Foo</h1>
        |<p>some page text</p>
        |</div>
        """)
  }

  it should "be able to display the properties of the page inside '$' delimiters" in {
    layoutPages(
      "index.md" -> """
      |---
      |layout: propPage
      |foo: bar
      |---
      """
    )(
        "page.st" -> defaultTemplate,
        "propPage.st" -> """
      |<div>
      |$layout$
      |$foo$
      |</div>
      """) shouldEqual htmlPages(
          "index.html" -> """
        |<div>
        |propPage
        |bar
        |</div>
        """)
  }

  it should "throw an exception if the specified template doesn't exist" in {
    the[RuntimeException] thrownBy {
      layoutPages(
        "index.md" -> """
      |---
      |layout: noTemplate
      |out: newIndex.html
      |---
      """)(
          "page.st" -> defaultTemplate
        )
    } should have message "StringTemplate 'noTemplate' was not found for 'newIndex.html'. Create a template or set a theme that contains one."
  }
}