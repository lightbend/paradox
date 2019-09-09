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

import com.lightbend.paradox.ParadoxException

class ExtRefDirectiveSpec extends MarkdownBaseSpec {

  implicit val context = writerContextWithProperties(
    "extref.rfc.base_url" -> "http://tools.ietf.org/html/rfc%s",
    "extref.issue.base_url" -> "https://github.com/lightbend/paradox/issues/%s",
    "extref.docs.base_url" -> "https://docs.example.org/%s",
    "extref.root_relative.base_url" -> ".../root/relative/%s",
    "extref.broken.base_url" -> "https://c|%s")

  "ExtRef directive" should "create links using configured URL templates" in {
    markdown("@extref[RFC 1234](rfc:1234)") shouldEqual
      html("""<p><a href="http://tools.ietf.org/html/rfc1234">RFC 1234</a></p>""")
    markdown("@extref[#1](issue:1)") shouldEqual
      html("""<p><a href="https://github.com/lightbend/paradox/issues/1">#1</a></p>""")
  }

  it should "create normalize generated URLs" in {
    markdown("@extref[#1](issue:/1)") shouldEqual
      html("""<p><a href="https://github.com/lightbend/paradox/issues/1">#1</a></p>""")
    markdown("@extref[Intro](docs:/some/introduction)") shouldEqual
      html("""<p><a href="https://docs.example.org/some/introduction">Intro</a></p>""")
  }

  it should "support 'extref:' as an alternative name" in {
    markdown("@extref:[RFC 1234](rfc:1234)") shouldEqual
      html("""<p><a href="http://tools.ietf.org/html/rfc1234">RFC 1234</a></p>""")
  }

  it should "support root relative '...' base urls" in {
    markdown("@extref:[Root Relative 1729](root_relative:1729)") shouldEqual
      html("""<p><a href="root/relative/1729">Root Relative 1729</a></p>""")
  }

  it should "handle anchored links correctly" in {
    markdown("@extref[RFC 7230 section 3.3.3](rfc:7230#section-3.3.3)") shouldEqual
      html("""<p><a href="http://tools.ietf.org/html/rfc7230#section-3.3.3">RFC 7230 section 3.3.3</a></p>""")
  }

  it should "retain whitespace before or after" in {
    markdown("The @extref:[RFC 1234](rfc:1234) spec") shouldEqual
      html("""<p>The <a href="http://tools.ietf.org/html/rfc1234">RFC 1234</a> spec</p>""")
  }

  it should "parse but ignore directive attributes" in {
    markdown("The @extref:[RFC 1234](rfc:1234) { .extref a=1 } spec") shouldEqual
      html("""<p>The <a href="http://tools.ietf.org/html/rfc1234">RFC 1234</a> spec</p>""")
  }

  it should "throw link exceptions for unknown references" in {
    the[ParadoxException] thrownBy {
      markdown("@extref[NPM](npm:left-pad)")
    } should have message "Failed to resolve [npm:left-pad] because property [extref.npm.base_url] is not defined"
  }

  it should "throw link exceptions for URLs without scheme" in {
    the[ParadoxException] thrownBy {
      markdown("@extref[Link with](no.scheme)")
    } should have message "Failed to resolve [no.scheme] because URL has no scheme"
  }

  it should "throw link exceptions for invalid input URLs" in {
    the[ParadoxException] thrownBy {
      markdown("@extref[URL](issue:|)")
    } should have message "Failed to resolve [issue:|] because template resulted in an invalid URL [https://github.com/lightbend/paradox/issues/|]"
  }

  it should "throw link exceptions for invalid output URLs" in {
    the[ParadoxException] thrownBy {
      markdown("@extref[URL](broken:link)")
    } should have message "Failed to resolve [broken:link] because template resulted in an invalid URL [https://c|link]"
  }

  it should "support referenced links" in {
    markdown(
      """@extref[RFC 1234] says it all!
        |
        |  [rfc 1234]: rfc:1234
      """.stripMargin) shouldEqual html("""<p><a href="http://tools.ietf.org/html/rfc1234">RFC 1234</a> says it all!</p>""")
  }
}
