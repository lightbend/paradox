# Overview

Paradox is a Markdown documentation tool for sbt projects. It takes a directory tree of markdown files and generates a 
static documentation site.

It has several features that help to structure and build documentation sites easily.

 * Supports @ref[GitHub flavored Markdown](markdown.md)
 * Pages can be @ref[organized](directives/organizing-pages.md) structured hierarchically. A sidebar with an index or an in-page table of contents
   can be generated.
 * @ref[Directives](directives/index.md) provide additional features over Markdown and allow custom extensions in a principled way
 * @ref[Variable substitution](variable-substitution.md) allows configuring values from your sbt project to be used in the documentation
 * @ref[Groups](groups.md) allow to create variants of the documentation where parts of pages are only shown when a
   group is selected e.g. for switching between Scala and Java versions of documentation, snippets, etc.
 * Various @ref[customization](customization/index.md) options allow tailoring Paradox' output to your needs.  For example, 
   @ref[Themes](customization/theming.md) allow customizing the appearance of Paradox-generated documentation. Custom 
   directives and themes can be packaged and published as separate sbt plugin @ref[extensions](customization/extensions.md).

### Project info

@@project-info{ projectId="core" }

### License and credits
 
 - Copyright 2015-2019 Lightbend, Inc. Paradox is provided under the Apache 2.0 license.
 - **Paradox is NOT supported under the Lightbend subscription.**
 - The markdown engine is based on Mathias's [Pegdown][]. 
 
 [Pegdown]: https://github.com/sirthias/pegdown/
 [repo]: https://github.com/lightbend/paradox
