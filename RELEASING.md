# Releasing

1. Check [closed issues without a milestone](https://github.com/lightbend/paradox/issues?utf8=%E2%9C%93&q=is%3Aissue%20is%3Aclosed%20no%3Amilestone) and either assign them the upcoming release milestone or 'invalid'
1. Wait until all running [Travis CI jobs](https://travis-ci.org/lightbend/paradox/builds) complete, if any.
1. Publish the [draft release](https://github.com/lightbend/paradox/releases) with a 'v0.x.y' tag
1. Travis CI will start a [CI build](https://travis-ci.org/lightbend/paradox/builds) for the new tag and publish artifacts to Bintray and Sonatype.
1. Close milestone on github and move any open issues to next milestone
1. Announce the new release in the [lightbend/paradox](https://gitter.im/lightbend/paradox) Gitter channel.
