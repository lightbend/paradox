# Releasing

1. Wait until all running [Travis CI jobs](https://travis-ci.org/lightbend/paradox/builds) complete, if any.
1. Publish the [draft release](https://github.com/lightbend/paradox/releases) with a 'v0.x.y' tag
1. Travis CI will start a [CI build](https://travis-ci.org/lightbend/paradox/builds) for the new tag and publish artifacts to Bintray and Sonatype.
1. Announce the new release in the [lightbend/paradox](https://gitter.im/lightbend/paradox) Gitter channel.
