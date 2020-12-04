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
      |}""") shouldEqual html(s"""
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
      |&lt;properties&gt;
      |  &lt;scala.binary.version&gt;2.12&lt;/scala.binary.version&gt;
      |&lt;/properties&gt;
      |&lt;dependencies&gt;
      |  &lt;dependency&gt;
      |    &lt;groupId&gt;com.typesafe.akka&lt;/groupId&gt;
      |    &lt;artifactId&gt;akka-http_$${scala.binary.version}&lt;/artifactId&gt;
      |    &lt;version&gt;10.0.10&lt;/version&gt;
      |  &lt;/dependency&gt;
      |&lt;/dependencies&gt;</code></pre>
      |</dd>
      |<dt>Gradle</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-gradle">
      |def versions = [
      |  ScalaBinary: "2.12"
      |]
      |dependencies {
      |  implementation "com.typesafe.akka:akka-http_$${versions.ScalaBinary}:10.0.10"
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
      |&lt;dependencies&gt;
      |  &lt;dependency&gt;
      |    &lt;groupId&gt;com.example&lt;/groupId&gt;
      |    &lt;artifactId&gt;domain&lt;/artifactId&gt;
      |    &lt;version&gt;0.1.0-RC2&lt;/version&gt;
      |    &lt;classifier&gt;assets&lt;/classifier&gt;
      |    &lt;scope&gt;runtime&lt;/scope&gt;
      |  &lt;/dependency&gt;
      |&lt;/dependencies&gt;</code></pre>
      |</dd>
      |<dt>Gradle</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-gradle">
      |dependencies {
      |  runtime "com.example:domain:0.1.0-RC2:assets"
      |}</code>
      |</pre>
      |</dd>
      |</dl>""")
  }
  it should "render test scope" in {
    markdown("""
               |@@dependency[sbt,Maven,Gradle] {
               |  group="com.example"
               |  artifact="domain"
               |  version="0.1.0-RC2"
               |  scope="test"
               |}""") shouldEqual html("""
      |<dl class="dependency">
      |<dt>sbt</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-scala">
      |libraryDependencies += "com.example" % "domain" % "0.1.0-RC2" % Test</code></pre>
      |</dd>
      |<dt>Maven</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-xml">
      |&lt;dependencies&gt;
      |  &lt;dependency&gt;
      |    &lt;groupId&gt;com.example&lt;/groupId&gt;
      |    &lt;artifactId&gt;domain&lt;/artifactId&gt;
      |    &lt;version&gt;0.1.0-RC2&lt;/version&gt;
      |    &lt;scope&gt;test&lt;/scope&gt;
      |  &lt;/dependency&gt;
      |&lt;/dependencies&gt;</code></pre>
      |</dd>
      |<dt>Gradle</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-gradle">
      |dependencies {
      |  testImplementation "com.example:domain:0.1.0-RC2"
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
      |}""") shouldEqual html(s"""
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
      |&lt;dependencies&gt;
      |  &lt;dependency&gt;
      |    &lt;groupId&gt;org.example&lt;/groupId&gt;
      |    &lt;artifactId&gt;foo_2.12&lt;/artifactId&gt;
      |    &lt;version&gt;0.1.0&lt;/version&gt;
      |  &lt;/dependency&gt;
      |  &lt;dependency&gt;
      |    &lt;groupId&gt;org.example&lt;/groupId&gt;
      |    &lt;artifactId&gt;bar_2.12&lt;/artifactId&gt;
      |    &lt;version&gt;0.2.0&lt;/version&gt;
      |  &lt;/dependency&gt;
      |&lt;/dependencies&gt;</code>
      |</pre>
      |</dd>
      |<dt>gradle</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-gradle">
      |dependencies {
      |  implementation "org.example:foo_2.12:0.1.0"
      |  implementation "org.example:bar_2.12:0.2.0"
      |}</code>
      |</pre>
      |</dd>
      |</dl>
      """)
  }

  it should "render symbolic version values" in {
    markdown("""
               |@@dependency[sbt,Maven,gradle] {
               |  symbol="AkkaHttpVersion"
               |  value="10.1.0"
               |  group="com.typesafe.akka"
               |  artifact="akka-http_$scala.binary.version$"
               |  version="AkkaHttpVersion"
               |}""") shouldEqual html(s"""
      |<dl class="dependency">
      |<dt>sbt</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-scala">
      |val AkkaHttpVersion = "10.1.0"
      |libraryDependencies += "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion</code></pre>
      |</dd>
      |<dt>Maven</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-xml">
      |&lt;properties&gt;
      |  &lt;akka.http.version&gt;10.1.0&lt;/akka.http.version&gt;
      |  &lt;scala.binary.version&gt;2.12&lt;/scala.binary.version&gt;
      |&lt;/properties&gt;
      |&lt;dependencies&gt;
      |  &lt;dependency&gt;
      |    &lt;groupId&gt;com.typesafe.akka&lt;/groupId&gt;
      |    &lt;artifactId&gt;akka-http_$${scala.binary.version}&lt;/artifactId&gt;
      |    &lt;version&gt;$${akka.http.version}&lt;/version&gt;
      |  &lt;/dependency&gt;
      |&lt;/dependencies&gt;</code></pre>
      |</dd>
      |<dt>gradle</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-gradle">
      |def versions = [
      |  AkkaHttpVersion: "10.1.0",
      |  ScalaBinary: "2.12"
      |]
      |dependencies {
      |  implementation "com.typesafe.akka:akka-http_$${versions.ScalaBinary}:$${versions.AkkaHttpVersion}"
      |}</code>
      |</pre>
      |</dd>
      |</dl>""")
  }

  it should "render multiple symbolic version values" in {
    markdown("""
               |@@dependency[sbt,Maven,gradle] {
               |  symbol="AkkaVersion"
               |  value="2.5.29"
               |  symbol2="AkkaHttpVersion"
               |  value2="10.1.0"
               |  group="com.typesafe.akka"
               |  artifact="akka-stream_$scala.binary.version$"
               |  version="AkkaVersion"
               |  group2="com.typesafe.akka"
               |  artifact2="akka-http_$scala.binary.version$"
               |  version2="AkkaHttpVersion"
               |}""") shouldEqual html(s"""
      |<dl class="dependency">
      |<dt>sbt</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-scala">
      |val AkkaVersion = "2.5.29"
      |val AkkaHttpVersion = "10.1.0"
      |libraryDependencies ++= Seq(
      |  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      |  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
      |)</code></pre>
      |</dd>
      |<dt>Maven</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-xml">
      |&lt;properties&gt;
      |  &lt;akka.version&gt;2.5.29&lt;/akka.version&gt;
      |  &lt;akka.http.version&gt;10.1.0&lt;/akka.http.version&gt;
      |  &lt;scala.binary.version&gt;2.12&lt;/scala.binary.version&gt;
      |&lt;/properties&gt;
      |&lt;dependencies&gt;
      |  &lt;dependency&gt;
      |    &lt;groupId&gt;com.typesafe.akka&lt;/groupId&gt;
      |    &lt;artifactId&gt;akka-stream_$${scala.binary.version}&lt;/artifactId&gt;
      |    &lt;version&gt;$${akka.version}&lt;/version&gt;
      |  &lt;/dependency&gt;
      |  &lt;dependency&gt;
      |    &lt;groupId&gt;com.typesafe.akka&lt;/groupId&gt;
      |    &lt;artifactId&gt;akka-http_$${scala.binary.version}&lt;/artifactId&gt;
      |    &lt;version&gt;$${akka.http.version}&lt;/version&gt;
      |  &lt;/dependency&gt;
      |&lt;/dependencies&gt;</code></pre>
      |</dd>
      |<dt>gradle</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-gradle">
      |def versions = [
      |  AkkaVersion: "2.5.29",
      |  AkkaHttpVersion: "10.1.0",
      |  ScalaBinary: "2.12"
      |]
      |dependencies {
      |  implementation "com.typesafe.akka:akka-stream_$${versions.ScalaBinary}:$${versions.AkkaVersion}"
      |  implementation "com.typesafe.akka:akka-http_$${versions.ScalaBinary}:$${versions.AkkaHttpVersion}"
      |}</code>
      |</pre>
      |</dd>
      |</dl>""")
  }

  it should "render bom import" in {
    markdown("""
               |@@dependency[sbt,Maven,gradle] {
               |  bomGroup="com.typesafe.akka"
               |  bomArtifact="akka-http-bom_$scala.binary.version$"
               |  bomVersionSymbols="AkkaHttpVersion"
               |  symbol="AkkaHttpVersion"
               |  value="10.1.0"
               |  group="com.typesafe.akka"
               |  artifact="akka-http_$scala.binary.version$"
               |  version="AkkaHttpVersion"
               |}""") shouldEqual html(s"""
      |<dl class="dependency">
      |<dt>sbt</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-scala">
      |val AkkaHttpVersion = "10.1.0"
      |libraryDependencies += "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion</code></pre>
      |</dd>
      |<dt>Maven</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-xml">
      |&lt;properties&gt;
      |  &lt;scala.binary.version&gt;2.12&lt;/scala.binary.version&gt;
      |&lt;/properties&gt;
      |&lt;dependencyManagement&gt;
      |  &lt;dependencies&gt;
      |    &lt;dependency&gt;
      |      &lt;groupId&gt;com.typesafe.akka&lt;/groupId&gt;
      |      &lt;artifactId&gt;akka-http-bom_$${scala.binary.version}&lt;/artifactId&gt;
      |      &lt;version&gt;10.1.0&lt;/version&gt;
      |      &lt;type&gt;pom&lt;/type&gt;
      |      &lt;scope&gt;import&lt;/scope&gt;
      |    &lt;/dependency&gt;
      |  &lt;/dependencies&gt;
      |&lt;/dependencyManagement&gt;
      |&lt;dependencies&gt;
      |  &lt;dependency&gt;
      |    &lt;groupId&gt;com.typesafe.akka&lt;/groupId&gt;
      |    &lt;artifactId&gt;akka-http_$${scala.binary.version}&lt;/artifactId&gt;
      |  &lt;/dependency&gt;
      |&lt;/dependencies&gt;</code></pre>
      |</dd>
      |<dt>gradle</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-gradle">
      |def versions = [
      |  ScalaBinary: "2.12"
      |]
      |dependencies {
      |  implementation platform("com.typesafe.akka:akka-http-bom_$${versions.ScalaBinary}:10.1.0")
      |
      |  implementation "com.typesafe.akka:akka-http_$${versions.ScalaBinary}"
      |}</code>
      |</pre>
      |</dd>
      |</dl>""")
  }

  it should "render multiple bom imports" in {
    markdown("""
               |@@dependency[Maven,gradle] {
               |  bomGroup="com.typesafe.akka"
               |  bomArtifact="akka-bom_$scala.binary.version$"
               |  bomVersionSymbols="AkkaVersion"
               |  bomGroup2="com.typesafe.akka"
               |  bomArtifact2="akka-http-bom_$scala.binary.version$"
               |  bomVersionSymbols2="AkkaHttpVersion"
               |  symbol1="AkkaVersion"
               |  value1="2.6.12"
               |  group1="com.typesafe.akka"
               |  symbol2="AkkaHttpVersion"
               |  value2="10.1.0"
               |  artifact1="akka-stream_$scala.binary.version$"
               |  version1="AkkaVersion"
               |  group2="com.typesafe.akka"
               |  artifact2="akka-http_$scala.binary.version$"
               |  version2="AkkaHttpVersion"
               |}""") shouldEqual html(s"""
      |<dl class="dependency">
      |<dt>Maven</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-xml">
      |&lt;properties&gt;
      |  &lt;scala.binary.version&gt;2.12&lt;/scala.binary.version&gt;
      |&lt;/properties&gt;
      |&lt;dependencyManagement&gt;
      |  &lt;dependencies&gt;
      |    &lt;dependency&gt;
      |      &lt;groupId&gt;com.typesafe.akka&lt;/groupId&gt;
      |      &lt;artifactId&gt;akka-bom_$${scala.binary.version}&lt;/artifactId&gt;
      |      &lt;version&gt;2.6.12&lt;/version&gt;
      |      &lt;type&gt;pom&lt;/type&gt;
      |      &lt;scope&gt;import&lt;/scope&gt;
      |    &lt;/dependency&gt;
      |    &lt;dependency&gt;
      |      &lt;groupId&gt;com.typesafe.akka&lt;/groupId&gt;
      |      &lt;artifactId&gt;akka-http-bom_$${scala.binary.version}&lt;/artifactId&gt;
      |      &lt;version&gt;10.1.0&lt;/version&gt;
      |      &lt;type&gt;pom&lt;/type&gt;
      |      &lt;scope&gt;import&lt;/scope&gt;
      |    &lt;/dependency&gt;
      |  &lt;/dependencies&gt;
      |&lt;/dependencyManagement&gt;
      |&lt;dependencies&gt;
      |  &lt;dependency&gt;
      |    &lt;groupId&gt;com.typesafe.akka&lt;/groupId&gt;
      |    &lt;artifactId&gt;akka-stream_$${scala.binary.version}&lt;/artifactId&gt;
      |  &lt;/dependency&gt;
      |  &lt;dependency&gt;
      |    &lt;groupId&gt;com.typesafe.akka&lt;/groupId&gt;
      |    &lt;artifactId&gt;akka-http_$${scala.binary.version}&lt;/artifactId&gt;
      |  &lt;/dependency&gt;
      |&lt;/dependencies&gt;</code></pre>
      |</dd>
      |<dt>gradle</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-gradle">
      |def versions = [
      |  ScalaBinary: "2.12"
      |]
      |dependencies {
      |  implementation platform("com.typesafe.akka:akka-bom_$${versions.ScalaBinary}:2.6.12")
      |  implementation platform("com.typesafe.akka:akka-http-bom_$${versions.ScalaBinary}:10.1.0")
      |
      |  implementation "com.typesafe.akka:akka-stream_$${versions.ScalaBinary}"
      |  implementation "com.typesafe.akka:akka-http_$${versions.ScalaBinary}"
      |}</code>
      |</pre>
      |</dd>
      |</dl>""")
  }

  it should "Maven: allow for multiple symbolic versions in one bom" in {
    markdown("""
               |@@dependency[Maven] {
               |  bomGroup="com.typesafe.akka"
               |  bomArtifact="akka-bom_$scala.binary.version$"
               |  bomVersionSymbols="AkkaVersion,AkkaHttpVersion"
               |  symbol1="AkkaVersion"
               |  value1="2.6.12"
               |  group1="com.typesafe.akka"
               |  symbol2="AkkaHttpVersion"
               |  value2="10.1.0"
               |  artifact1="akka-stream_$scala.binary.version$"
               |  version1="AkkaVersion"
               |  group2="com.typesafe.akka"
               |  artifact2="akka-http_$scala.binary.version$"
               |  version2="AkkaHttpVersion"
               |}""") shouldEqual html(s"""
      |<dl class="dependency">
      |<dt>Maven</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-xml">
      |&lt;properties&gt;
      |  &lt;scala.binary.version&gt;2.12&lt;/scala.binary.version&gt;
      |&lt;/properties&gt;
      |&lt;dependencyManagement&gt;
      |  &lt;dependencies&gt;
      |    &lt;dependency&gt;
      |      &lt;groupId&gt;com.typesafe.akka&lt;/groupId&gt;
      |      &lt;artifactId&gt;akka-bom_$${scala.binary.version}&lt;/artifactId&gt;
      |      &lt;version&gt;2.6.12&lt;/version&gt;
      |      &lt;type&gt;pom&lt;/type&gt;
      |      &lt;scope&gt;import&lt;/scope&gt;
      |    &lt;/dependency&gt;
      |  &lt;/dependencies&gt;
      |&lt;/dependencyManagement&gt;
      |&lt;dependencies&gt;
      |  &lt;dependency&gt;
      |    &lt;groupId&gt;com.typesafe.akka&lt;/groupId&gt;
      |    &lt;artifactId&gt;akka-stream_$${scala.binary.version}&lt;/artifactId&gt;
      |  &lt;/dependency&gt;
      |  &lt;dependency&gt;
      |    &lt;groupId&gt;com.typesafe.akka&lt;/groupId&gt;
      |    &lt;artifactId&gt;akka-http_$${scala.binary.version}&lt;/artifactId&gt;
      |  &lt;/dependency&gt;
      |&lt;/dependencies&gt;</code></pre>
      |</dd>
      |</dl>""")
  }

  it should "Gradle: allow for multiple symbolic versions in one bom" in {
    markdown("""
               |@@dependency[gradle] {
               |  bomGroup="com.typesafe.akka"
               |  bomArtifact="akka-bom_$scala.binary.version$"
               |  bomVersionSymbols="AkkaVersion,AkkaHttpVersion"
               |  symbol1="AkkaVersion"
               |  value1="2.6.12"
               |  group1="com.typesafe.akka"
               |  symbol2="AkkaHttpVersion"
               |  value2="10.1.0"
               |  artifact1="akka-stream_$scala.binary.version$"
               |  version1="AkkaVersion"
               |  group2="com.typesafe.akka"
               |  artifact2="akka-http_$scala.binary.version$"
               |  version2="AkkaHttpVersion"
               |}""") shouldEqual html(s"""
      |<dl class="dependency">
      |<dt>gradle</dt>
      |<dd>
      |<pre class="prettyprint">
      |<code class="language-gradle">
      |def versions = [
      |  ScalaBinary: "2.12"
      |]
      |dependencies {
      |  implementation platform("com.typesafe.akka:akka-bom_$${versions.ScalaBinary}:2.6.12")
      |
      |  implementation "com.typesafe.akka:akka-stream_$${versions.ScalaBinary}"
      |  implementation "com.typesafe.akka:akka-http_$${versions.ScalaBinary}"
      |}</code>
      |</pre>
      |</dd>
      |</dl>""")
  }

}
