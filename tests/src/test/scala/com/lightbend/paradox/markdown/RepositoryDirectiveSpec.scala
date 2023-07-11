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

import com.lightbend.paradox.tree.Tree.Location

class RepositoryDirectiveSpec extends MarkdownBaseSpec {

  val testProperties = Map(
    "project.version" -> "10.0.10",
    "scala.version" -> "2.12.3",
    "scala.binary.version" -> "2.12"
  )

  implicit val context: Location[Page] => Writer.Context = { loc =>
    writerContext(loc).copy(properties = testProperties)
  }

  "Repository directive" should "render single repo" in {
    markdown("""
      |@@repository[sbt,Maven,gradle] {
      |  id="id1"
      |  name="Company repository"
      |  url="http://jars.acme.com"
      |}""") shouldEqual html(s"""
      |<dl class="repository">
      |<dt>sbt</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-scala">
      |resolvers += "Company repository".at("http://jars.acme.com")
      |</code>
      |</pre>
      |</dd>
      |<dt>Maven</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-xml">
      |&lt;project&gt;
      |  ...
      |  &lt;repositories&gt;
      |    &lt;repository&gt;
      |      &lt;id&gt;id1&lt;/id&gt;
      |      &lt;name&gt;Company repository&lt;/name&gt;
      |      &lt;url&gt;http://jars.acme.com&lt;/url&gt;
      |    &lt;/repository&gt;
      |  &lt;/repositories&gt;
      |&lt;/project&gt;
      |</code></pre>
      |</dd>
      |<dt>gradle</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-gradle">
      |repositories {
      |    mavenCentral()
      |    maven {
      |        url "http://jars.acme.com"
      |    }
      |}
      |</code></pre>
      |</dd>
      |</dl>""")
  }

  "Repository directive" should "render two repos" in {
    markdown("""
        |@@repository[sbt,Maven,gradle] {
        |  id1="id1"
        |  name1="Company repository"
        |  url1="http://jars.acme.com"
        |  id2="id-2"
        |  name2="Company repository 2"
        |  url2="http://uberjars.acme.com"
        |}""") shouldEqual html(s"""
         |<dl class="repository">
         |<dt>sbt</dt>
         |<dd>
         |<pre class="prettyprint">
         |<code class="language-scala">
         |resolvers ++= Seq(
         |  "Company repository".at("http://jars.acme.com"),
         |  "Company repository 2".at("http://uberjars.acme.com")
         |)
         |</code>
         |</pre>
         |</dd>
         |<dt>Maven</dt>
         |<dd>
         |<pre class="prettyprint">
         |<code class="language-xml">
         |&lt;project&gt;
         |  ...
         |  &lt;repositories&gt;
         |    &lt;repository&gt;
         |      &lt;id&gt;id1&lt;/id&gt;
         |      &lt;name&gt;Company repository&lt;/name&gt;
         |      &lt;url&gt;http://jars.acme.com&lt;/url&gt;
         |    &lt;/repository&gt;
         |    &lt;repository&gt;
         |      &lt;id&gt;id-2&lt;/id&gt;
         |      &lt;name&gt;Company repository 2&lt;/name&gt;
         |      &lt;url&gt;http://uberjars.acme.com&lt;/url&gt;
         |    &lt;/repository&gt;
         |  &lt;/repositories&gt;
         |&lt;/project&gt;
         |</code></pre>
         |</dd>
         |<dt>gradle</dt>
         |<dd>
         |<pre class="prettyprint">
         |<code class="language-gradle">
         |repositories {
         |    mavenCentral()
         |    maven {
         |        url "http://jars.acme.com"
         |    }
         |    maven {
         |        url "http://uberjars.acme.com"
         |    }
         |}
         |</code></pre>
         |</dd>
         |</dl>""")
  }

}
