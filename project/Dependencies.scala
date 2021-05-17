/*
 * Copyright Lightbend, Inc.
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

import sbt._

object Version {
  val foundation = "6.2.4"
  val jtidy      = "r938"
  val pegdown    = "1.6.0"
  val parboiled  = "1.3.1"
  val prettify   = "4-Mar-2013-1"
  val sbtWeb     = "1.4.4"
  val scalatest  = "3.2.9"
  val st4        = "4.3.1"
  val jsoup      = "1.13.1"
}

object Library {
  val foundation = "org.webjars"       % "foundation" % Version.foundation
  val jtidy      = "net.sf.jtidy"      % "jtidy"      % Version.jtidy
  val pegdown    = Seq(
                     "org.pegdown"     % "pegdown"        % Version.pegdown,
                     "org.parboiled"   % "parboiled-java" % Version.parboiled // overwrite for JDK10 support
                   )
  val prettify   = "org.webjars"       % "prettify"   % Version.prettify
  val sbtWeb     = "com.typesafe.sbt"  % "sbt-web"    % Version.sbtWeb
  val scalatest  = "org.scalatest"    %% "scalatest"  % Version.scalatest
  val st4        = "org.antlr"         % "ST4"        % Version.st4
  val jsoup      = "org.jsoup"         % "jsoup"      % Version.jsoup
}
