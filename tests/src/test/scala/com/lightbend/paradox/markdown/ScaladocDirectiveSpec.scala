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

class ScaladocDirectiveSpec extends MarkdownBaseSpec {

  implicit val context = writerContextWithProperties(
    "scaladoc.base_url" -> "http://example.org/api/0.1.2/",
    "scaladoc.scala.base_url" -> "http://www.scala-lang.org/api/2.11.12/",
    "scaladoc.akka.base_url" -> "http://doc.akka.io/api/akka/2.4.10",
    "scaladoc.akka.http.base_url" -> "http://doc.akka.io/api/akka-http/10.0.0",
    "scaladoc.akka.kafka.base_url" -> "https://doc.akka.io/api/alpakka-kafka/current",
    "scaladoc.root.relative.base_url" -> ".../scaladoc/api/",
    "scaladoc.broken.base_url" -> "https://c|",
    "scaladoc.org.example.base_url" -> "http://example.org/api/0.1.2/"
  )

  def renderedMd(url: String, title: String, name: String, prefix: String = "", suffix: String = "") =
    html(Seq(prefix, """<p><a href="""", url, """" title="""", title, """"><code>""", name, """</code></a></p>""", suffix).mkString(""))

  "Scaladoc directive" should "create links using configured URL templates" in {
    markdown("@scaladoc[Model](org.example.Model)") shouldEqual
      html("""<p><a href="http://example.org/api/0.1.2/org/example/Model.html" title="org.example.Model"><code>Model</code></a></p>""")
  }

  it should "create accept digits in package names" in {
    markdown("@scaladoc[ObjectMetadata](akka.s3.ObjectMetadata)") shouldEqual
      html("""<p><a href="http://doc.akka.io/api/akka/2.4.10/akka/s3/ObjectMetadata.html" title="akka.s3.ObjectMetadata"><code>ObjectMetadata</code></a></p>""")
  }

  it should "create accept also non ascii characters (java letters) in package names" in {
    markdown("@scaladoc[S0meTHing](org.example.some.stränµè.ıãß.S0meTHing)") shouldEqual
      renderedMd("http://example.org/api/0.1.2/org/example/some/stränµè/ıãß/S0meTHing.html", "org.example.some.stränµè.ıãß.S0meTHing", "S0meTHing")
  }

  it should "create accept also non ascii characters (java letters) in class names" in {
    markdown("@scaladoc[Grüße](org.example.some.Grüße)") shouldEqual
      renderedMd("http://example.org/api/0.1.2/org/example/some/Grüße.html", "org.example.some.Grüße", "Grüße")
  }

  it should "create accept uppercase in package names" in {
    markdown("@scaladoc[S0meTHing](org.example.soME.stränµè.ıãß.S0meTHing)") shouldEqual
      renderedMd("http://example.org/api/0.1.2/org/example/soME/stränµè/ıãß/S0meTHing.html", "org.example.soME.stränµè.ıãß.S0meTHing", "S0meTHing")
  }

  it should "create accept subpackages starting with uppercase" in {
    implicit val context = writerContextWithProperties(
      "scaladoc.package_name_style" -> "startWithAnycase",
      "scaladoc.org.example.base_url" -> "http://example.org/api/0.1.2/")
    markdown("@scaladoc[S0meTHing](org.example.soME.stränµè.ıãß.你好.S0meTHing)") shouldEqual
      renderedMd("http://example.org/api/0.1.2/org/example/soME/stränµè/ıãß/你好/S0meTHing.html", "org.example.soME.stränµè.ıãß.你好.S0meTHing", "S0meTHing")
  }

  it should "support 'scaladoc:' as an alternative name" in {
    markdown("@scaladoc:[Model](org.example.Model)") shouldEqual
      html("""<p><a href="http://example.org/api/0.1.2/org/example/Model.html" title="org.example.Model"><code>Model</code></a></p>""")
  }

  it should "support root relative '...' base urls" in {
    markdown("@scaladoc:[Url](root.relative.Url)") shouldEqual
      html("""<p><a href="scaladoc/api/root/relative/Url.html" title="root.relative.Url"><code>Url</code></a></p>""")
  }

  it should "handle method links correctly" in {
    markdown("@scaladoc[???](scala.Predef$#???:Nothing)") shouldEqual
      html("""<p><a href="http://www.scala-lang.org/api/2.11.12/scala/Predef$.html#???:Nothing" title="scala.Predef"><code>???</code></a></p>""")

    markdown(
      """@scaladoc:[Actor#preStart]
        |
        |  [Actor#preStart]: akka.actor.Actor#preStart():Unit""") shouldEqual
      html("""<p><a href="http://doc.akka.io/api/akka/2.4.10/akka/actor/Actor.html#preStart():Unit" title="akka.actor.Actor"><code>Actor#preStart</code></a></p>""")
  }

  it should "handle object links correctly" in {
    markdown("@scaladoc[Http](akka.http.scaladsl.Http$)") shouldEqual
      html("""<p><a href="http://doc.akka.io/api/akka-http/10.0.0/akka/http/scaladsl/Http$.html" title="akka.http.scaladsl.Http"><code>Http</code></a></p>""")
    markdown("@scaladoc[Actor](akka.actor.Actor)") shouldEqual
      html("""<p><a href="http://doc.akka.io/api/akka/2.4.10/akka/actor/Actor.html" title="akka.actor.Actor"><code>Actor</code></a></p>""")
  }

  it should "handle inner classes correctly" in {
    markdown("@scaladoc[Consumer.Control](akka.kafka.scaladsl.Consumer.Control)") shouldEqual
      html("""<p><a href="https://doc.akka.io/api/alpakka-kafka/current/akka/kafka/scaladsl/Consumer$$Control.html" title="akka.kafka.scaladsl.Consumer.Control"><code>Consumer.Control</code></a></p>""")
  }

  it should "handle inner classes in $$ notation correctly if a subpackage starts with an uppercase character" in {
    val ctx = context.andThen(c => c.copy(properties = c.properties
      .updated("scaladoc.org.example.package_name_style", "startWithAnycase")
    ))
    markdown("@scaladoc:[Outer.Inner](org.example.Lib.Outer$$Inner)")(ctx) shouldEqual
      renderedMd("http://example.org/api/0.1.2/org/example/Lib/Outer$$Inner.html", "org.example.Lib.Outer.Inner", "Outer.Inner")
  }

  it should "handle inner classes in $$ notation correctly if the outer class starts with a lowercase character" in {
    markdown("@scaladoc:[outer.Inner](org.example.lib.outer$$Inner)") shouldEqual
      renderedMd("http://example.org/api/0.1.2/org/example/lib/outer$$Inner.html", "org.example.lib.outer.Inner", "outer.Inner")
  }

  it should "handle inner classes in $$ notation correctly if the inner class starts with a lowercase character" in {
    val ctx = context.andThen(c => c.copy(properties = c.properties
      .updated("scaladoc.org.example.package_name_style", "startWithAnycase")
    ))
    markdown("@scaladoc:[Outer.inner](org.example.lib.Outer$$inner)")(ctx) shouldEqual
      renderedMd("http://example.org/api/0.1.2/org/example/lib/Outer$$inner.html", "org.example.lib.Outer.inner", "Outer.inner")
  }

  it should "handle inner classes in $$ notation" in {
    markdown("@scaladoc[Consumer.Control](akka.kafka.scaladsl.Consumer$$Control)") shouldEqual
      html("""<p><a href="https://doc.akka.io/api/alpakka-kafka/current/akka/kafka/scaladsl/Consumer$$Control.html" title="akka.kafka.scaladsl.Consumer.Control"><code>Consumer.Control</code></a></p>""")
  }

  it should "retain whitespace before or after" in {
    markdown("The @scaladoc:[Model](org.example.Model) class") shouldEqual
      html("""<p>The <a href="http://example.org/api/0.1.2/org/example/Model.html" title="org.example.Model"><code>Model</code></a> class</p>""")
  }

  it should "parse but ignore directive attributes" in {
    markdown("The @scaladoc:[Model](org.example.Model) { .scaladoc a=1 } spec") shouldEqual
      html("""<p>The <a href="http://example.org/api/0.1.2/org/example/Model.html" title="org.example.Model"><code>Model</code></a> spec</p>""")
  }

  it should "throw exceptions for unconfigured default base URL" in {
    the[ParadoxException] thrownBy {
      markdown("@scaladoc[Model](org.example.Model)")(writerContext)
    } should have message "Failed to resolve [org.example.Model] because property [scaladoc.base_url] is not defined"
  }

  it should "throw link exceptions for invalid output URLs" in {
    the[ParadoxException] thrownBy {
      markdown("@scaladoc[URL](broken.URL)")
    } should have message "Failed to resolve [broken.URL] because property [scaladoc.broken.base_url] contains an invalid URL [https://c|]"
  }

  it should "support Scala 2.12 links" in {
    implicit val context = writerContextWithProperties(
      "scaladoc.scala.base_url" -> "http://www.scala-lang.org/api/2.12.0/",
      "scaladoc.version" -> "2.12.0")

    markdown("@scaladoc[Int](scala.Int)") shouldEqual
      html("""<p><a href="http://www.scala-lang.org/api/2.12.0/scala/Int.html" title="scala.Int"><code>Int</code></a></p>""")
    markdown("@scaladoc[Codec$](scala.io.Codec$)") shouldEqual
      html("""<p><a href="http://www.scala-lang.org/api/2.12.0/scala/io/Codec$.html" title="scala.io.Codec"><code>Codec$</code></a></p>""")
    markdown("@scaladoc[scala.io package](scala.io.index)") shouldEqual
      html("""<p><a href="http://www.scala-lang.org/api/2.12.0/scala/io/index.html" title="scala.io"><code>scala.io package</code></a></p>""")
  }

  it should "support Scala 2.11 links" in {
    implicit val context = writerContextWithProperties(
      "scaladoc.scala.base_url" -> "http://www.scala-lang.org/api/2.11.12/",
      "scaladoc.version" -> "2.11.12")

    markdown("@scaladoc[Int](scala.Int)") shouldEqual
      html("""<p><a href="http://www.scala-lang.org/api/2.11.12/scala/Int.html" title="scala.Int"><code>Int</code></a></p>""")
    markdown("@scaladoc[Codec$](scala.io.Codec$)") shouldEqual
      html("""<p><a href="http://www.scala-lang.org/api/2.11.12/scala/io/Codec$.html" title="scala.io.Codec"><code>Codec$</code></a></p>""")
    markdown("@scaladoc[scala.io package](scala.io.package)") shouldEqual
      html("""<p><a href="http://www.scala-lang.org/api/2.11.12/scala/io/package.html" title="scala.io"><code>scala.io package</code></a></p>""")
  }

  it should "support referenced links" in {
    markdown(
      """The @scaladoc:[Model][1] spec
        |
        |  [1]: org.example.Model
      """) shouldEqual
      html("""<p>The <a href="http://example.org/api/0.1.2/org/example/Model.html" title="org.example.Model"><code>Model</code></a> spec</p>""")
  }
}
