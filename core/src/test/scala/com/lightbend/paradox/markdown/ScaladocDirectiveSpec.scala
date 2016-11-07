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

class ScaladocDirectiveSpec extends MarkdownBaseSpec {

  implicit val context = writerContextWithProperties(
    "scaladoc.base_url" -> "http://example.org/api/0.1.2",
    "scaladoc.scala.base_url" -> "http://www.scala-lang.org/api/2.11.8",
    "scaladoc.akka.base_url" -> "http://doc.akka.io/api/akka/2.4.10",
    "scaladoc.akka.http.base_url" -> "http://doc.akka.io/api/akka-http/10.0.0",
    "scaladoc.broken.base_url" -> "https://c|"
  )

  "Scaladoc directive" should "create links using configured URL templates" in {
    markdown("@scaladoc[Model](org.example.Model)") shouldEqual
      html("""<p><a href="http://example.org/api/0.1.2/#org.example.Model">Model</a></p>""")
  }

  it should "support 'scaladoc:' as an alternative name" in {
    markdown("@scaladoc:[Model](org.example.Model)") shouldEqual
      html("""<p><a href="http://example.org/api/0.1.2/#org.example.Model">Model</a></p>""")
  }

  it should "handle method links correctly" in {
    markdown("@scaladoc[???](scala.Predef$@???:Nothing)") shouldEqual
      html("""<p><a href="http://www.scala-lang.org/api/2.11.8/#scala.Predef$@???:Nothing">???</a></p>""")
  }

  it should "handle object links correctly" in {
    markdown("@scaladoc[Http](akka.http.scaladsl.Http$)") shouldEqual
      html("""<p><a href="http://doc.akka.io/api/akka-http/10.0.0/#akka.http.scaladsl.Http$">Http</a></p>""")
  }

  it should "retain whitespace before or after" in {
    markdown("The @scaladoc:[Model](org.example.Model) class") shouldEqual
      html("""<p>The <a href="http://example.org/api/0.1.2/#org.example.Model">Model</a> class</p>""")
  }

  it should "parse but ignore directive attributes" in {
    markdown("The @scaladoc:[Model](org.example.Model) { .scaladoc a=1 } spec") shouldEqual
      html("""<p>The <a href="http://example.org/api/0.1.2/#org.example.Model">Model</a> spec</p>""")
  }

  it should "throw exceptions for unconfigured default base URL" in {
    the[ExternalLinkDirective.LinkException] thrownBy {
      markdown("@scaladoc[Model](org.example.Model)")(writerContext)
    } should have message "Failed to resolve [org.example.Model] referenced from [test.html] because property [scaladoc.base_url] is not defined"
  }

  it should "throw link exceptions for invalid output URLs" in {
    the[ExternalLinkDirective.LinkException] thrownBy {
      markdown("@scaladoc[URL](broken.URL)")
    } should have message "Failed to resolve [broken.URL] referenced from [test.html] because property [scaladoc.broken.base_url] contains an invalid URL [https://c|]"
  }

  it should "support referenced links" in {
    markdown(
      """The @scaladoc:[Model][1] spec
        |
        |  [1]: org.example.Model
      """.stripMargin) shouldEqual html("""<p>The <a href="http://example.org/api/0.1.2/#org.example.Model">Model</a> spec</p>""")
  }
}
