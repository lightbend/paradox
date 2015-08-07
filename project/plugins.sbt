/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

addSbtPlugin("com.github.gseitz" % "sbt-release"     % "1.0.1")
addSbtPlugin("com.typesafe.sbt"  % "sbt-scalariform" % "1.3.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-web"         % "1.1.1")
addSbtPlugin("com.typesafe.tmp"  % "sbt-header"      % "1.5.0-JDK6-0.1")
addSbtPlugin("me.lessis"         % "bintray-sbt"     % "0.3.0")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value

// override scalariform version to get some fixes
resolvers += Resolver.typesafeRepo("releases")
libraryDependencies += "org.scalariform" %% "scalariform" % "0.1.5-20140822-69e2e30"
