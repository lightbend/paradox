/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

import sbt._

object Version {
  val foundation = "5.1.1"
  val jtidy      = "r938"
  val pegdown    = "1.5.0"
  val prettify   = "4-Mar-2013"
  val sbtWeb     = "1.2.2"
  val scalatest  = "2.2.5"
  val st4        = "4.0.8"
}

object Library {
  val foundation = "org.webjars"       % "foundation" % Version.foundation
  val jtidy      = "net.sf.jtidy"      % "jtidy"      % Version.jtidy
  val pegdown    = "org.pegdown"       % "pegdown"    % Version.pegdown
  val prettify   = "org.webjars"       % "prettify"   % Version.prettify
  val sbtWeb     = "com.typesafe.sbt"  % "sbt-web"    % Version.sbtWeb
  val scalatest  = "org.scalatest"    %% "scalatest"  % Version.scalatest
  val st4        = "org.antlr"         % "ST4"        % Version.st4
}
