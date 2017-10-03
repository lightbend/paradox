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

import com.lightbend.paradox.tree.Tree.Location

class JavadocDirectiveSpec extends MarkdownBaseSpec {

  implicit val context = writerContextWithProperties(
    "javadoc.base_url" -> "http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/",
    "javadoc.java.base_url" -> "https://docs.oracle.com/javase/8/docs/api/",
    "javadoc.akka.base_url" -> "http://doc.akka.io/japi/akka/2.4.10",
    "javadoc.akka.http.base_url" -> "http://doc.akka.io/japi/akka-http/10.0.0/index.html",
    "javadoc.root.relative.base_url" -> ".../javadoc/api/",
    "javadoc.broken.base_url" -> "https://c|"
  )

  "javadoc directive" should "create links using configured URL templates" in {
    markdown("@javadoc[Publisher](org.reactivestreams.Publisher)") shouldEqual
      html("""<p><a href="http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/?org/reactivestreams/Publisher.html">Publisher</a></p>""")
  }

  it should "support 'javadoc:' as an alternative name" in {
    markdown("@javadoc:[Publisher](org.reactivestreams.Publisher)") shouldEqual
      html("""<p><a href="http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/?org/reactivestreams/Publisher.html">Publisher</a></p>""")
  }

  it should "support root relative '...' base urls" in {
    markdown("@javadoc[Url](root.relative.Url)") shouldEqual
      html("""<p><a href="javadoc/api/?root/relative/Url.html">Url</a></p>""")
  }

  it should "handle method links correctly" in {
    markdown("@javadoc[File.pathSeparator](java.io.File#pathSeparator)") shouldEqual
      html("""<p><a href="https://docs.oracle.com/javase/8/docs/api/?java/io/File.html#pathSeparator">File.pathSeparator</a></p>""")
  }

  it should "handle class links correctly" in {
    markdown("@javadoc[Http](akka.http.javadsl.Http)") shouldEqual
      html("""<p><a href="http://doc.akka.io/japi/akka-http/10.0.0/index.html?akka/http/javadsl/Http.html">Http</a></p>""")
    markdown("@javadoc[Actor](akka.actor.Actor)") shouldEqual
      html("""<p><a href="http://doc.akka.io/japi/akka/2.4.10/?akka/actor/Actor.html">Actor</a></p>""")
  }

  it should "retain whitespace before or after" in {
    markdown("The @javadoc:[Actor](akka.actor.Actor) class") shouldEqual
      html("""<p>The <a href="http://doc.akka.io/japi/akka/2.4.10/?akka/actor/Actor.html">Actor</a> class</p>""")
  }

  it should "parse but ignore directive attributes" in {
    markdown("The @javadoc:[Publisher](org.reactivestreams.Publisher) { .javadoc a=1 } spec") shouldEqual
      html("""<p>The <a href="http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/?org/reactivestreams/Publisher.html">Publisher</a> spec</p>""")
  }

  it should "throw exceptions for unconfigured default base URL" in {
    the[ExternalLinkDirective.LinkException] thrownBy {
      markdown("@javadoc[Model](org.example.Model)")(writerContext)
    } should have message "Failed to resolve [org.example.Model] referenced from [test.html] because property [javadoc.base_url] is not defined"
  }

  it should "throw link exceptions for invalid output URLs" in {
    the[ExternalLinkDirective.LinkException] thrownBy {
      markdown("@javadoc[URL](broken.URL)")
    } should have message "Failed to resolve [broken.URL] referenced from [test.html] because property [javadoc.broken.base_url] contains an invalid URL [https://c|]"
  }

  it should "throw link exceptions for invalid link URLs" in {
    the[ExternalLinkDirective.LinkException] thrownBy {
      markdown("@javadoc[Oops](a.b|c)")
    } should have message "Failed to resolve [a.b|c] referenced from [test.html] because template resulted in an invalid URL [a.b|c]"
  }

  it should "support referenced links" in {
    markdown(
      """The @javadoc:[Publisher][Publisher] spec
        |
        |  [Publisher]: org.reactivestreams.Publisher
      """.stripMargin) shouldEqual
      html("""<p>The <a href="http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/?org/reactivestreams/Publisher.html">Publisher</a> spec</p>""")
  }

}
