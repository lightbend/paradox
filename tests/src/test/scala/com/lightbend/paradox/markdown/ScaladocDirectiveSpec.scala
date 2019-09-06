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
    "scaladoc.root.relative.base_url" -> ".../scaladoc/api/",
    "scaladoc.broken.base_url" -> "https://c|"
  )

  "Scaladoc directive" should "create links using configured URL templates" in {
    markdown("@scaladoc[Model](org.example.Model)") shouldEqual
      html("""<p><a href="http://example.org/api/0.1.2/org/example/Model.html">Model</a></p>""")
  }

  it should "support 'scaladoc:' as an alternative name" in {
    markdown("@scaladoc:[Model](org.example.Model)") shouldEqual
      html("""<p><a href="http://example.org/api/0.1.2/org/example/Model.html">Model</a></p>""")
  }

  it should "support root relative '...' base urls" in {
    markdown("@scaladoc:[Url](root.relative.Url)") shouldEqual
      html("""<p><a href="scaladoc/api/root/relative/Url.html">Url</a></p>""")
  }

  it should "handle method links correctly" in {
    markdown("@scaladoc[???](scala.Predef$#???:Nothing)") shouldEqual
      html("""<p><a href="http://www.scala-lang.org/api/2.11.12/scala/Predef$.html#???:Nothing">???</a></p>""")

    markdown(
      """@scaladoc:[Actor#preStart]
        |
        |  [Actor#preStart]: akka.actor.Actor#preStart():Unit""") shouldEqual
      html("""<p><a href="http://doc.akka.io/api/akka/2.4.10/akka/actor/Actor.html#preStart():Unit">Actor#preStart</a></p>""")
  }

  it should "handle object links correctly" in {
    markdown("@scaladoc[Http](akka.http.scaladsl.Http$)") shouldEqual
      html("""<p><a href="http://doc.akka.io/api/akka-http/10.0.0/akka/http/scaladsl/Http$.html">Http</a></p>""")
    markdown("@scaladoc[Actor](akka.actor.Actor)") shouldEqual
      html("""<p><a href="http://doc.akka.io/api/akka/2.4.10/akka/actor/Actor.html">Actor</a></p>""")
  }

  it should "retain whitespace before or after" in {
    markdown("The @scaladoc:[Model](org.example.Model) class") shouldEqual
      html("""<p>The <a href="http://example.org/api/0.1.2/org/example/Model.html">Model</a> class</p>""")
  }

  it should "parse but ignore directive attributes" in {
    markdown("The @scaladoc:[Model](org.example.Model) { .scaladoc a=1 } spec") shouldEqual
      html("""<p>The <a href="http://example.org/api/0.1.2/org/example/Model.html">Model</a> spec</p>""")
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
      html("""<p><a href="http://www.scala-lang.org/api/2.12.0/scala/Int.html">Int</a></p>""")
    markdown("@scaladoc[Codec$](scala.io.Codec$)") shouldEqual
      html("""<p><a href="http://www.scala-lang.org/api/2.12.0/scala/io/Codec$.html">Codec$</a></p>""")
    markdown("@scaladoc[scala.io package](scala.io.index)") shouldEqual
      html("""<p><a href="http://www.scala-lang.org/api/2.12.0/scala/io/index.html">scala.io package</a></p>""")
  }

  it should "support Scala 2.11 links" in {
    implicit val context = writerContextWithProperties(
      "scaladoc.scala.base_url" -> "http://www.scala-lang.org/api/2.11.12/",
      "scaladoc.version" -> "2.11.12")

    markdown("@scaladoc[Int](scala.Int)") shouldEqual
      html("""<p><a href="http://www.scala-lang.org/api/2.11.12/scala/Int.html">Int</a></p>""")
    markdown("@scaladoc[Codec$](scala.io.Codec$)") shouldEqual
      html("""<p><a href="http://www.scala-lang.org/api/2.11.12/scala/io/Codec$.html">Codec$</a></p>""")
    markdown("@scaladoc[scala.io package](scala.io.package)") shouldEqual
      html("""<p><a href="http://www.scala-lang.org/api/2.11.12/scala/io/package.html">scala.io package</a></p>""")
  }

  it should "support referenced links" in {
    markdown(
      """The @scaladoc:[Model][1] spec
        |
        |  [1]: org.example.Model
      """) shouldEqual
      html("""<p>The <a href="http://example.org/api/0.1.2/org/example/Model.html">Model</a> spec</p>""")
  }
}
