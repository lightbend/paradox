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

class PropertiesLoadSpec extends MarkdownBaseSpec {

  "Property 'out'" should "be taken into account for file name at generation" in {
    markdownPages(
      "index.md" -> """
      |---
      |out: newIndex.html
      |---
      """
    ) shouldEqual htmlPages(
        "newIndex.html" -> "")
  }

  it should "display the correct content even if the file name has changed" in {
    markdownPages(
      "index.md" -> """
      |---
      |out: newIndex.html
      |---
      |# Foo
      """
    ) shouldEqual htmlPages(
        "newIndex.html" -> """
      |<h1><a href="#foo" name="foo" class="anchor"><span class="anchor-link"></span></a>Foo</h1>
      """)
  }
}