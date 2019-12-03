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

class JavadocDirectiveSpec extends MarkdownBaseSpec {

  import JavadocDirective._

  implicit val context = writerContextWithProperties(
    "javadoc.base_url" -> "http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/",
    "javadoc.link_style" -> "frames",
    "javadoc.java.base_url" -> "https://docs.oracle.com/javase/8/docs/api/",
    "javadoc.akka.base_url" -> "http://doc.akka.io/japi/akka/2.4.10",
    "javadoc.akka.http.base_url" -> "http://doc.akka.io/japi/akka-http/10.0.0/index.html",
    "javadoc.root.relative.base_url" -> ".../javadoc/api/",
    "javadoc.broken.base_url" -> "https://c|"
  )

  "javadoc directive" should "create links using configured URL templates" in {
    markdown("@javadoc[Publisher](org.reactivestreams.Publisher)") shouldEqual
      html("""<p><a href="http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/?org/reactivestreams/Publisher.html" title="org.reactivestreams.Publisher"><code>Publisher</code></a></p>""")
  }

  it should "support 'javadoc:' as an alternative name" in {
    markdown("@javadoc:[Publisher](org.reactivestreams.Publisher)") shouldEqual
      html("""<p><a href="http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/?org/reactivestreams/Publisher.html" title="org.reactivestreams.Publisher"><code>Publisher</code></a></p>""")
  }

  it should "support root relative '...' base urls" in {
    markdown("@javadoc[Url](root.relative.Url)") shouldEqual
      html("""<p><a href="javadoc/api/?root/relative/Url.html" title="root.relative.Url"><code>Url</code></a></p>""")
  }

  it should "handle method links correctly" in {
    markdown("@javadoc[File.pathSeparator](java.io.File#pathSeparator)") shouldEqual
      html("""<p><a href="https://docs.oracle.com/javase/8/docs/api/?java/io/File.html#pathSeparator" title="java.io.File"><code>File.pathSeparator</code></a></p>""")
  }

  it should "handle method links with parentheses correctly" in {
    markdown("@javadoc[File.pathSeparator](java.io.File#method())") shouldEqual
      html("""<p><a href="https://docs.oracle.com/javase/8/docs/api/?java/io/File.html#method()" title="java.io.File"><code>File.pathSeparator</code></a></p>""")
  }

  it should "handle class links correctly" in {
    markdown("@javadoc[Http](akka.http.javadsl.Http)") shouldEqual
      html("""<p><a href="http://doc.akka.io/japi/akka-http/10.0.0/index.html?akka/http/javadsl/Http.html" title="akka.http.javadsl.Http"><code>Http</code></a></p>""")
    markdown("@javadoc[Actor](akka.actor.Actor)") shouldEqual
      html("""<p><a href="http://doc.akka.io/japi/akka/2.4.10/?akka/actor/Actor.html" title="akka.actor.Actor"><code>Actor</code></a></p>""")
  }

  it should "retain whitespace before or after" in {
    markdown("The @javadoc:[Actor](akka.actor.Actor) class") shouldEqual
      html("""<p>The <a href="http://doc.akka.io/japi/akka/2.4.10/?akka/actor/Actor.html" title="akka.actor.Actor"><code>Actor</code></a> class</p>""")
  }

  it should "parse but ignore directive attributes" in {
    markdown("The @javadoc:[Publisher](org.reactivestreams.Publisher) { .javadoc a=1 } spec") shouldEqual
      html("""<p>The <a href="http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/?org/reactivestreams/Publisher.html" title="org.reactivestreams.Publisher"><code>Publisher</code></a> spec</p>""")
  }

  it should "throw exceptions for unconfigured default base URL" in {
    the[ParadoxException] thrownBy {
      markdown("@javadoc[Model](org.example.Model)")(writerContext)
    } should have message "Failed to resolve [org.example.Model] because property [javadoc.base_url] is not defined"
  }

  it should "throw link exceptions for invalid output URLs" in {
    the[ParadoxException] thrownBy {
      markdown("@javadoc[URL](broken.URL)")
    } should have message "Failed to resolve [broken.URL] because property [javadoc.broken.base_url] contains an invalid URL [https://c|]"
  }

  it should "throw link exceptions for invalid link URLs" in {
    the[ParadoxException] thrownBy {
      markdown("@javadoc[Oops](a.b|c)")
    } should have message "Failed to resolve [a.b|c] because template resulted in an invalid URL [a.b|c]"
  }

  it should "support referenced links" in {
    markdown(
      """The @javadoc:[Publisher][Publisher] spec
        |
        |  [Publisher]: org.reactivestreams.Publisher
      """.stripMargin) shouldEqual
      html("""<p>The <a href="http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/?org/reactivestreams/Publisher.html" title="org.reactivestreams.Publisher"><code>Publisher</code></a> spec</p>""")
  }

  it should "support creating non frame style links" in {
    val ctx = context.andThen(c => c.copy(properties = c.properties.updated("javadoc.link_style", "direct")))
    markdown("@javadoc[Publisher](org.reactivestreams.Publisher)")(ctx) shouldEqual
      html("""<p><a href="http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/org/reactivestreams/Publisher.html" title="org.reactivestreams.Publisher"><code>Publisher</code></a></p>""")
  }

  it should "support choosing 'direct links' per package" in {
    val ctx = context.andThen(c => c.copy(properties = c.properties.updated("javadoc.org.reactivestreams.link_style", "direct")))
    markdown(
      """Frames: @javadoc:[Actor](akka.actor.Actor)
        |Direct: @javadoc[Publisher](org.reactivestreams.Publisher)""".stripMargin)(ctx) shouldEqual
      html(
        """<p>Frames: <a href="http://doc.akka.io/japi/akka/2.4.10/?akka/actor/Actor.html" title="akka.actor.Actor"><code>Actor</code></a>
          |Direct: <a href="http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/org/reactivestreams/Publisher.html" title="org.reactivestreams.Publisher"><code>Publisher</code></a></p>""".stripMargin)
  }

  it should "correctly link to an inner JRE class" in {
    url(
      "java.util.concurrent.Flow.Subscriber",
      Url("https://docs.oracle.com/en/java/javase/11/docs/api/java.base/"),
      LinkStyleDirect
    ) should be(Url("https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/Flow.Subscriber.html"))
  }

  it should "correctly link to an inner Akka class" in {
    url(
      "akka.actor.testkit.typed.Effect.MessageAdapter",
      Url("https://doc.akka.io/japi/akka/current/"),
      LinkStyleDirect
    ) should be(Url("https://doc.akka.io/japi/akka/current/akka/actor/testkit/typed/Effect.MessageAdapter.html"))
  }

}
