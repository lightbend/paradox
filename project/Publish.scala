/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

import sbt._
import sbt.Keys._
import bintray.{ BintrayKeys, BintrayPlugin }

/**
 * Publish to private bintray repository.
 */
object Publish extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = plugins.JvmPlugin && BintrayPlugin

  override def buildSettings = Seq(
    BintrayKeys.bintrayOrganization := Some("typesafe"),
    BintrayKeys.bintrayReleaseOnPublish := false
  )

  override def projectSettings = Seq(
    BintrayKeys.bintrayRepository := "phoenix",
    BintrayKeys.bintrayPackage := "paradox",
    licenses += "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"),
    pomIncludeRepository := { _ => false }
  )

}

/**
 * For projects that are not published.
 */
object NoPublish extends AutoPlugin {

  override def requires = plugins.JvmPlugin && Publish

  override def projectSettings = Seq(
    publish := (),
    publishLocal := ()
  )

}
