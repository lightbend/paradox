* tag as with vX.Y.Z
* `sbt ^publishSigned`
* close and release sonatype staging repo at http://oss.sonatype.org/
* publish bintray artifacts at https://bintray.com/sbt/sbt-plugin-releases/sbt-paradox/
* push tag to github
* close milestone on github and move any open issues to next milestone

`publishSigned` will automatically push "normal" artifacts to oss.sonatype.org and sbt artifacts
to bintray. You will need access rights / credentials both to `com.lightbend.paradox` on oss.sonatype.org and
to `sbt-plugin-releases/sbt-paradox` on bintray.
