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

import java.net.URI

import com.lightbend.paradox.tree.Tree.Location

class LinkCapturerSpec extends MarkdownBaseSpec {

  private def capturerFor(pagePath: String, markdown: String): LinkCapturer = {
    val location = Location.forest(pages((pagePath, markdown))).get
    val context = writerContext(location)
    val linkCapturer = new LinkCapturer
    val serializer = linkCapturer.serializer(context)
    location.tree.label.markdown.accept(serializer)
    linkCapturer
  }

  private def linksFor(pagePath: String, markdown: String) =
    for {
      link <- capturerFor(pagePath, markdown).allLinks
      fragment <- link.fragments
    } yield (link.link.toString, fragment.fragment)

  "The LinkCapturer" should "classify relative links in same directory as relative" in {
    linksFor("foo/test.md", "[link](bar.html)") should ===(List(("foo/bar.html", None)))
  }

  it should "classify relative links in a child directory as relative" in {
    linksFor("foo/test.md", "[link](child/bar.html)") should ===(List(("foo/child/bar.html", None)))
  }

  it should "classify relative links in a parent directory as relative" in {
    linksFor("foo/test.md", "[link](../bar.html)") should ===(List(("bar.html", None)))
  }

  it should "classify relative links in a child of a parent directory as relative" in {
    linksFor("foo/test.md", "[link](../child/bar.html)") should ===(List(("child/bar.html", None)))
  }

  it should "ignore relative links outside of the docs directory when no base path is specified" in {
    linksFor("foo/test.md", "[link](../../bar.html)") shouldBe empty
  }

  it should "ignore absolute path links when no base path is specified" in {
    linksFor("foo/test.md", "[link](/bar.html)") shouldBe empty
  }

  it should "treat absolute path links as relative when a base path is specified" in {
    linksFor("/docs/foo/test.md", "[link](/bar.html)") should ===(List(("/bar.html", None)))
  }

  it should "accept relative links outside of the docs directory when a base path is specified" in {
    linksFor("/docs/foo/test.md", "[link](../../bar.html)") should ===(List(("/bar.html", None)))
  }

  it should "accept relative links in a child directory outside of the docs directory when a base path is specified" in {
    linksFor("/docs/foo/test.md", "[link](../../apidocs/bar.html)") should ===(List(("/apidocs/bar.html", None)))
  }

  it should "include the base path in links found in the docs tree when a base path is specified" in {
    linksFor("/docs/foo/test.md", "[link](bar.html)") should ===(List(("/docs/foo/bar.html", None)))
  }

  it should "not ignore invalid relative links (so they can be reported as missing later)" in {
    linksFor("/docs/foo/test.md", "[link](../../../bar.html)") should ===(List(("/../bar.html", None)))
  }

  it should "capture fragments" in {
    linksFor("foo/test.md", "[link](bar.html#frag)") should ===(List(("foo/bar.html", Some("frag"))))
  }

  it should "treat links with a hostname as absolute" in {
    linksFor("foo/test.md", "https://lightbend.com") should ===(List(("https://lightbend.com", None)))
  }

  it should "ignore ref links (because they are validated by the compiler)" in {
    linksFor("foo/test.md", "@ref[link](bar.md)") shouldBe empty
  }

  it should "append index.html to directory links" in {
    linksFor("foo/test.md", "[link](bar/)") should ===(List(("foo/bar/index.html", None)))
  }
}
