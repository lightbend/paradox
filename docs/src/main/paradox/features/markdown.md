Markdown flavour
----------------

Paradox source files are formatted in [Markdown syntax](https://daringfireball.net/projects/markdown/syntax), and by convention we prefer the [GitHub-flavored style](https://help.github.com/articles/basic-writing-and-formatting-syntax/).

We currently use the [Pegdown](https://github.com/sirthias/pegdown#introduction) processor, though this may not [remain the case](https://github.com/lightbend/paradox/issues/81).

Some aspects of our Markdown dialect worth noting are:

* Bulleted lists must always be preceded by an empty line (otherwise they'll be considered to be a 'normal' part of the previous paragraph)
* For references within a paradox tree, we prefer `@ref:[Link Text](../other-file.md#some-anchor)` syntax over free-form `[Link Text](../other-file.html#some-anchor)`. This introduces opportunities for [validation](https://github.com/lightbend/paradox/issues/53)
