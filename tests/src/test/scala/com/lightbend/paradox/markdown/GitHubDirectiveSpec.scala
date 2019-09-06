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

class GitHubDirectiveSpec extends MarkdownBaseSpec {

  implicit val context = writerContextWithProperties(
    "github.base_url" -> "https://github.com/lightbend/paradox/tree/v0.2.1",
    "github.root.base_dir" -> ".")

  "GitHub directive" should "create links using configured base URL" in {
    markdown("@github[#1](#1)") shouldEqual
      html("""<p><a href="https://github.com/lightbend/paradox/issues/1">#1</a></p>""")
  }

  it should "support 'github:' as an alternative name" in {
    markdown("@github:[#1](#1)") shouldEqual
      html("""<p><a href="https://github.com/lightbend/paradox/issues/1">#1</a></p>""")
  }

  it should "support github enterprise deployments" in {
    implicit val context = writerContextWithProperties(
      "github.base_url" -> "https://git.enterprise.net/lightbend/paradox/tree/v0.2.1",
      "github.root.base_dir" -> ".",
      "github.domain" -> "git.enterprise.net")
    markdown("@github:[#1](#1)") shouldEqual
      html("""<p><a href="https://git.enterprise.net/lightbend/paradox/issues/1">#1</a></p>""")
  }

  it should "retain whitespace before or after" in {
    markdown("The @github:[#1](#1) issue") shouldEqual
      html("""<p>The <a href="https://github.com/lightbend/paradox/issues/1">#1</a> issue</p>""")
  }

  it should "parse but ignore directive attributes" in {
    markdown("The @github:[#1](#1) { .github a=1 } issue") shouldEqual
      html("""<p>The <a href="https://github.com/lightbend/paradox/issues/1">#1</a> issue</p>""")
  }

  it should "handle issue links to other project" in {
    markdown("@github[akka/akka#1234](akka/akka#1234)") shouldEqual
      html("""<p><a href="https://github.com/akka/akka/issues/1234">akka/akka#1234</a></p>""")
  }

  it should "handle commits links to other project" in {
    markdown("@github[akka/akka@2da7b26b](akka/akka@2da7b26b)") shouldEqual
      html("""<p><a href="https://github.com/akka/akka/commit/2da7b26b">akka/akka@2da7b26b</a></p>""")
  }

  it should "handle tree links" in {
    markdown("@github[See build.sbt](/build.sbt)") shouldEqual
      html("""<p><a href="https://github.com/lightbend/paradox/tree/v0.2.1/build.sbt">See build.sbt</a></p>""")
  }

  it should "handle tree links with automatic versioning" in {
    val context = writerContextWithProperties(
      "github.base_url" -> "https://github.com/lightbend/paradox",
      "github.root.base_dir" -> ".")

    markdown("@github[See build.sbt](/build.sbt)")(context) shouldEqual
      html("""<p><a href="https://github.com/lightbend/paradox/tree/master/build.sbt">See build.sbt</a></p>""")
  }

  it should "throw exceptions for unconfigured GitHub URL" in {
    the[ParadoxException] thrownBy {
      markdown("@github[#1](#1)")(writerContext)
    } should have message "Failed to resolve [#1] because property [github.base_url] is not defined"
  }

  it should "throw exceptions for invalid GitHub URLs" in {
    val invalidContext = writerContextWithProperties(
      "github.base_url" -> "https://github.com/project",
      "github.root.base_dir" -> ".")

    the[ParadoxException] thrownBy {
      markdown("@github[#1](#1)")(invalidContext)
    } should have message "Failed to resolve [#1] because [github.base_url] is not a project URL"

    the[ParadoxException] thrownBy {
      markdown("@github[README.md](/README.md)")(invalidContext)
    } should have message "Failed to resolve [/README.md] because [github.base_url] is not a project or versioned tree URL"
  }

  it should "throw link exceptions for invalid GitHub URL" in {
    val brokenContext = writerContextWithProperties("github.base_url" -> "https://github.com/broken/project|")

    the[ParadoxException] thrownBy {
      markdown("@github[#1](#1)")(brokenContext)
    } should have message "Failed to resolve [#1] because property [github.base_url] contains an invalid URL [https://github.com/broken/project|]"
  }

  it should "support referenced links" in {
    markdown(
      """@github[#1234][1]
        |
        |  [1]: akka/akka#1234
      """.stripMargin) shouldEqual
      html("""<p><a href="https://github.com/akka/akka/issues/1234">#1234</a></p>""")
  }

  it should "support line numbers" in {
    markdown("""
      |@github[build.sbt]
      |@github[response test](/akka-http-core/src/test/scala/akka/http/impl/engine/rendering/ResponseRendererSpec.scala#L422)
      |
      |  [build.sbt]: /build.sbt#L5
      |""") shouldEqual html("""
      |<p><a href="https://github.com/lightbend/paradox/tree/v0.2.1/build.sbt#L5">build.sbt</a>
      |<a href="https://github.com/lightbend/paradox/tree/v0.2.1/akka-http-core/src/test/scala/akka/http/impl/engine/rendering/ResponseRendererSpec.scala#L422">response test</a></p>
      |""")
  }

  it should "throw exceptions for invalid GitHub tree path" in {
    the[ParadoxException] thrownBy {
      markdown("@github[path](/|)")
    } should have message "Failed to resolve [/|] because path is invalid"
  }

  it should "support line labels" in {
    markdown("""
      |@github[example.scala] { #github-path-link }
      |@github[neither](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #github-neither-path-link }
      |@github[neither](/tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #github-neither-path-link }
      |
      |  [example.scala]: tests/src/test/scala/com/lightbend/paradox/markdown/example.scala
      |""") shouldEqual html("""
      |<p><a href="https://github.com/lightbend/paradox/tree/v0.2.1/tests/src/test/scala/com/lightbend/paradox/markdown/example.scala#L20-L24">example.scala</a>
      |<a href="https://github.com/lightbend/paradox/tree/v0.2.1/tests/src/test/scala/com/lightbend/paradox/markdown/example.scala#L22">neither</a>
      |<a href="https://github.com/lightbend/paradox/tree/v0.2.1/tests/src/test/scala/com/lightbend/paradox/markdown/example.scala#L22">neither</a></p>
      |""")
  }

  it should "throw exceptions for non-existing GitHub tree path with label" in {
    val ex = the[ParadoxException] thrownBy {
      markdown("""
        |@github[oops](does/not/exist.scala) { #broken }
        |""")
    }

    ex.getMessage.startsWith("Failed to resolve [does/not/exist.scala] to a file") shouldBe true
  }

  it should "throw exceptions for non-existing GitHub tree path with invalid label" in {
    val ex = the[ParadoxException] thrownBy {
      markdown("""
        |@github[neither](tests/src/test/scala/com/lightbend/paradox/markdown/example.scala) { #does-not-exist }
        |""")
    }

    ex.getMessage.replace('\\', '/') shouldBe
      "Failed to resolve [tests/src/test/scala/com/lightbend/paradox/markdown/example.scala]: Label [does-not-exist] not found in [tests/src/test/scala/com/lightbend/paradox/markdown/example.scala]"
  }

}
