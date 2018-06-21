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

class DependencyDirectiveSpec extends MarkdownBaseSpec {

  val testProperties = Map(
    "project.version" -> "10.0.10",
    "scala.version" -> "2.12.3",
    "scala.binary.version" -> "2.12"
  )

  implicit val context: Location[Page] => Writer.Context = { loc =>
    writerContext(loc).copy(properties = testProperties)
  }

  "Dependency directive" should "render scala dependency" in {
    markdown("""
      |@@dependency[sbt,Maven,Gradle] {
      |  group="com.typesafe.akka"
      |  artifact="akka-http_$scala.binary.version$"
      |  version="$project.version$"
      |}""") shouldEqual html("""
      |<dl class="dependency">
      |<dt>sbt</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-scala">
      |libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.10"</code></pre>
      |</dd>
      |<dt>Maven</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-xml">
      |&lt;dependency&gt;
      |  &lt;groupId&gt;com.typesafe.akka&lt;/groupId&gt;
      |  &lt;artifactId&gt;akka-http_2.12&lt;/artifactId&gt;
      |  &lt;version&gt;10.0.10&lt;/version&gt;
      |&lt;/dependency&gt;</code></pre>
      |</dd>
      |<dt>Gradle</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-gradle">
      |dependencies {
      |  compile group: 'com.typesafe.akka', name: 'akka-http_2.12', version: '10.0.10'
      |}</code>
      |</pre>
      |</dd>
      |</dl>""")
  }

  it should "only render the configured tools" in {
    markdown("""
      |@@dependency [sbt] {
      |  .add-config-dep
      |  group=com.typesafe
      |  artifact=config
      |  version=1.3.1
      |}""") shouldEqual html("""
      |<dl class="dependency add-config-dep">
      |<dt>sbt</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-scala">
      |libraryDependencies += "com.typesafe" % "config" % "1.3.1"</code></pre>
      |</dd>
      |</dl>""")
  }

  it should "optionally render the configured tools" in {
    markdown("""
      |@@dependency[sbt,Maven,Gradle] {
      |  group="com.example"
      |  artifact="domain"
      |  version="0.1.0-RC2"
      |  scope="runtime"
      |  classifier="assets"
      |}""") shouldEqual html("""
      |<dl class="dependency">
      |<dt>sbt</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-scala">
      |libraryDependencies += "com.example" % "domain" % "0.1.0-RC2" % Runtime classifier "assets"</code></pre>
      |</dd>
      |<dt>Maven</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-xml">
      |&lt;dependency&gt;
      |  &lt;groupId&gt;com.example&lt;/groupId&gt;
      |  &lt;artifactId&gt;domain&lt;/artifactId&gt;
      |  &lt;version&gt;0.1.0-RC2&lt;/version&gt;
      |  &lt;classifier&gt;assets&lt;/classifier&gt;
      |  &lt;scope&gt;runtime&lt;/scope&gt;
      |&lt;/dependency&gt;</code></pre>
      |</dd>
      |<dt>Gradle</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-gradle">
      |dependencies {
      |  runtime group: 'com.example', name: 'domain', version: '0.1.0-RC2', classifier: 'assets'
      |}</code>
      |</pre>
      |</dd>
      |</dl>""")
  }

  it should "only simplify sbt definition if the scalaBinaryVersion matches" in {
    markdown("""
      |@@dependency [sbt] {
      |  group=org.example
      |  artifact=lib_2.12.1
      |  version=0.1.0
      |}""") shouldEqual html("""
      |<dl class="dependency">
      |<dt>sbt</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-scala">
      |libraryDependencies += "org.example" % "lib_2.12.1" % "0.1.0"</code></pre>
      |</dd>
      |</dl>""")
  }

  it should "use CrossVersion.full when scalaVersion matches" in {
    markdown("""
      |@@dependency [sbt] {
      |  group=org.example
      |  artifact=lib_2.12.3
      |  version=0.1.0
      |}""") shouldEqual html("""
      |<dl class="dependency">
      |<dt>sbt</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-scala">
      |libraryDependencies += "org.example" % "lib" % "0.1.0" cross CrossVersion.full</code></pre>
      |</dd>
      |</dl>""")
  }

  it should "use CrossVersion.full when scalaVersion and scalaBinaryVersion matches" in {
    val scalaVersion = "2.13.0-M1"
    val testProperties = Map(
      "scala.version" -> scalaVersion,
      "scala.binary.version" -> scalaVersion
    )

    implicit val context: Location[Page] => Writer.Context = { loc =>
      writerContext(loc).copy(properties = testProperties)
    }

    markdown("""
      |@@dependency [sbt] {
      |  group=org.example
      |  artifact=lib_2.13.0-M1
      |  version=0.1.0
      |}""") shouldEqual html("""
      |<dl class="dependency">
      |<dt>sbt</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-scala">
      |libraryDependencies += "org.example" % "lib" % "0.1.0" cross CrossVersion.full</code></pre>
      |</dd>
      |</dl>""")
  }

  it should "render multiple dependencies" in {
    markdown("""
      |@@dependency [sbt,maven,gradle] {
      |  group=org.example  artifact=foo_2.12  version=0.1.0
      |  group2=org.example artifact2=bar_2.12 version2=0.2.0
      |}""") shouldEqual html("""
      |<dl class="dependency">
      |<dt>sbt</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-scala">
      |libraryDependencies ++= Seq(
      |  "org.example" %% "foo" % "0.1.0",
      |  "org.example" %% "bar" % "0.2.0"
      |)</code>
      |</pre>
      |</dd>
      |<dt>maven</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-xml">
      |&lt;dependency&gt;
      |  &lt;groupId&gt;org.example&lt;/groupId&gt;
      |  &lt;artifactId&gt;foo_2.12&lt;/artifactId&gt;
      |  &lt;version&gt;0.1.0&lt;/version&gt;
      |&lt;/dependency&gt;
      |&lt;dependency&gt;
      |  &lt;groupId&gt;org.example&lt;/groupId&gt;
      |  &lt;artifactId&gt;bar_2.12&lt;/artifactId&gt;
      |  &lt;version&gt;0.2.0&lt;/version&gt;
      |&lt;/dependency&gt;</code>
      |</pre>
      |</dd>
      |<dt>gradle</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-gradle">
      |dependencies {
      |  compile group: 'org.example', name: 'foo_2.12', version: '0.1.0',
      |  compile group: 'org.example', name: 'bar_2.12', version: '0.2.0'
      |}</code>
      |</pre>
      |</dd>
      |</dl>
      """)
  }
}
