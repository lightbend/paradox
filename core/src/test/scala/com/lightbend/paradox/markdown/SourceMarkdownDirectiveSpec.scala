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

class SourceMarkdownDirectiveSpec extends MarkdownBaseSpec {
  implicit val context = writerContextWithProperties(
    "github.base_url" -> "https://github.com/lightbend/paradox/tree/v0.2.1",
    "github.paradox_dir" -> "docs/manual")

  "SourceMarkdown Directive" should "create links with configured base URL and markdown directory on github" in {
    markdown("@source[link]()") shouldEqual
      html("""<p><a href="https://github.com/lightbend/paradox/tree/v0.2.1/docs/manual/src/main/paradox/test.md">link</a></p>""")
  }

  it should "support 'source:' as an alternative name" in {
    markdown("@source:[link]()") shouldEqual
      html("""<p><a href="https://github.com/lightbend/paradox/tree/v0.2.1/docs/manual/src/main/paradox/test.md">link</a></p>""")
  }

  it should "display markdown source url of the file specified in parameter" in {
    markdown("@source[link](index.md)") shouldEqual
      html("""<p><a href="https://github.com/lightbend/paradox/tree/v0.2.1/docs/manual/src/main/paradox/index.md">link</a></p>""")
  }

  it should "display correct url for 'in-directory' files" in {
    markdown("@source[link](some/index.md)") shouldEqual
      html("""<p><a href="https://github.com/lightbend/paradox/tree/v0.2.1/docs/manual/src/main/paradox/some/index.md">link</a></p>""")
  }

  it should "display correct url for relative path to other files" in {
    markdown("@source[link](../test.md)", "some/directory/index.md") shouldEqual
      html("""<p><a href="https://github.com/lightbend/paradox/tree/v0.2.1/docs/manual/src/main/paradox/some/test.md">link</a></p>""")
  }

  it should "throw an error if github.paradox_dir contains '/' duplicates" in {
    val duplicateSeparatorsContext = writerContextWithProperties(
      "github.base_url" -> "https://github.com/lightbend/paradox/tree/v0.2.1",
      "github.paradox_dir" -> "docs//dir")

    the[ExternalLinkDirective.LinkException] thrownBy {
      markdown("@source[link]()")(duplicateSeparatorsContext)
    } should have message "Failed to resolve [] referenced from [test.html] because [docs//dir] contains duplicate '/' separators"
  }

  it should "throw an error if the link contains '/' duplicates" in {
    the[ExternalLinkDirective.LinkException] thrownBy {
      markdown("@source[link](some//link.md)")
    } should have message "Failed to resolve [some//link.md] referenced from [test.html] because [some//link.md] contains duplicate '/' separators"
  }

  it should "throw an error if the link does not correspond to a markdown file" in {
    the[ExternalLinkDirective.LinkException] thrownBy {
      markdown("@source[link](some/link.mdi)")
    } should have message "Failed to resolve [some/link.mdi] referenced from [test.html] because [some/link.mdi] is not a markdown (.md) file"
  }

  it should "throw an error if the link can't be converted into URL" in {
    the[ExternalLinkDirective.LinkException] thrownBy {
      markdown("@source[link](some/dir|index.md)")
    } should have message "Failed to resolve [some/dir|index.md] referenced from [test.html] because link [some/dir|index.md] contains an invalid URL [/docs/manual/src/main/paradox/some/dir|index.md]"
  }
}