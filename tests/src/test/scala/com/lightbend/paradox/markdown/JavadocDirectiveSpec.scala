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

  implicit val context = writerContextWithProperties(
    "javadoc.base_url" -> "http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/",
    "javadoc.link_style" -> "frames",
    "javadoc.java.base_url" -> "https://docs.oracle.com/javase/8/docs/api/",
    "javadoc.akka.base_url" -> "http://doc.akka.io/japi/akka/2.4.10",
    "javadoc.akka.http.base_url" -> "http://doc.akka.io/japi/akka-http/10.0.0/index.html",
    "javadoc.root.relative.base_url" -> ".../javadoc/api/",
    "javadoc.broken.base_url" -> "https://c|",
    "javadoc.org.example.base_url" -> "http://example.org/api/0.1.2/"
  )

  def renderedMd(url: String, title: String, name: String, prefix: String = "", suffix: String = "") =
    html(Seq(prefix, """<p><a href="""", url, """" title="""", title, """"><code>""", name, """</code></a></p>""", suffix).mkString(""))

  "javadoc directive" should "create links using configured URL templates" in {
    markdown("@javadoc[Publisher](org.reactivestreams.Publisher)") shouldEqual
      html("""<p><a href="http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/?org/reactivestreams/Publisher.html" title="org.reactivestreams.Publisher"><code>Publisher</code></a></p>""")
  }

  it should "create accept digits in package names" in {
    markdown("@javadoc[ObjectMetadata](akka.s3.ObjectMetadata)") shouldEqual
      renderedMd("http://doc.akka.io/japi/akka/2.4.10/?akka/s3/ObjectMetadata.html", "akka.s3.ObjectMetadata", "ObjectMetadata")
  }

  it should "create accept also non ascii characters (java letters) in package names" in {
    markdown("@javadoc[S0meTHing](org.example.some.stränµè.ıãß.S0meTHing)") shouldEqual
      renderedMd("http://example.org/api/0.1.2/?org/example/some/stränµè/ıãß/S0meTHing.html", "org.example.some.stränµè.ıãß.S0meTHing", "S0meTHing")
  }

  it should "create accept also non ascii characters (java letters) in class names" in {
    markdown("@javadoc[Grüße](org.example.some.Grüße)") shouldEqual
      renderedMd("http://example.org/api/0.1.2/?org/example/some/Grüße.html", "org.example.some.Grüße", "Grüße")
  }

  it should "create accept uppercase in package names" in {
    markdown("@javadoc[S0meTHing](org.example.soME.stränµè.ıãß.S0meTHing)") shouldEqual
      renderedMd("http://example.org/api/0.1.2/?org/example/soME/stränµè/ıãß/S0meTHing.html", "org.example.soME.stränµè.ıãß.S0meTHing", "S0meTHing")
  }

  it should "create accept subpackages starting with uppercase" in {
    implicit val context = writerContextWithProperties(
      "javadoc.package_name_style" -> "startWithAnycase",
      "javadoc.org.example.base_url" -> "http://example.org/api/0.1.2/")
    markdown("@javadoc[S0meTHing](org.example.soME.stränµè.ıãß.你好.S0meTHing)") shouldEqual
      renderedMd("http://example.org/api/0.1.2/?org/example/soME/stränµè/ıãß/你好/S0meTHing.html", "org.example.soME.stränµè.ıãß.你好.S0meTHing", "S0meTHing")
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
    val ctx = context.andThen(c => c.copy(properties = c.properties
      .updated("javadoc.java.link_style", "direct")
      .updated("javadoc.java.base_url", "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/")
    ))
    markdown("@javadoc:[Flow.Subscriber](java.util.concurrent.Flow.Subscriber)")(ctx) shouldEqual
      html("""<p><a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/Flow.Subscriber.html" title="java.util.concurrent.Flow.Subscriber"><code>Flow.Subscriber</code></a></p>""")
  }

  it should "correctly link to an inner Akka class" in {
    val ctx = context.andThen(c => c.copy(properties = c.properties
      .updated("javadoc.akka.link_style", "direct")
      .updated("javadoc.akka.base_url", "https://doc.akka.io/japi/akka/current/")
    ))
    markdown("@javadoc:[Effect.MessageAdapter](akka.actor.testkit.typed.Effect.MessageAdapter)")(ctx) shouldEqual
      html("""<p><a href="https://doc.akka.io/japi/akka/current/akka/actor/testkit/typed/Effect.MessageAdapter.html" title="akka.actor.testkit.typed.Effect.MessageAdapter"><code>Effect.MessageAdapter</code></a></p>""")
  }

  it should "correctly link to an inner class if a subpackage starts with an uppercase character" in {
    val ctx = context.andThen(c => c.copy(properties = c.properties
      .updated("javadoc.org.example.package_name_style", "startWithAnycase")
    ))
    markdown("@javadoc:[Outer.Inner](org.example.Lib.Outer$$Inner)")(ctx) shouldEqual
      renderedMd("http://example.org/api/0.1.2/?org/example/Lib/Outer.Inner.html", "org.example.Lib.Outer.Inner", "Outer.Inner")
  }

  it should "correctly link to an inner class if the outer class starts with a lowercase character" in {
    markdown("@javadoc:[outer.Inner](org.example.lib.outer$$Inner)") shouldEqual
      renderedMd("http://example.org/api/0.1.2/?org/example/lib/outer.Inner.html", "org.example.lib.outer.Inner", "outer.Inner")
  }

  it should "correctly link to an inner class if the inner class starts with a lowercase character" in {
    val ctx = context.andThen(c => c.copy(properties = c.properties
      .updated("javadoc.org.example.package_name_style", "startWithAnycase")
    ))
    markdown("@javadoc:[Outer.inner](org.example.lib.Outer$$inner)")(ctx) shouldEqual
      renderedMd("http://example.org/api/0.1.2/?org/example/lib/Outer.inner.html", "org.example.lib.Outer.inner", "Outer.inner")
  }

}
