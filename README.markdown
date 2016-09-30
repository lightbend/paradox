Paradox
=======

Paradox is a documentation tool for software projects.

**Status**: In development.

**Currently Paradox is not supported under Lightbend subscription.**

Setup
-----

Find [the latest](https://github.com/lightbend/paradox/releases) version, and create `project/paradox.sbt`:

```scala
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox" % "X.Y.Z")
```

Inside `build.sbt`, add `ParadoxPlugin` to a subproject:

```scala
lazy val root = (project in file(".")).
  enablePlugins(ParadoxPlugin).
  settings(
    name := "Hello Project",
    paradoxTheme := Some(builtinParadoxTheme("generic"))
  )
```

Then call `paradox` which will generate the site in `target/paradox/site/`.

Documents
---------

Your markdown documentation will go inside `src/main/paradox/`. For example, you can start with `src/main/paradox/index.md`.

### GFM

Powered by [Pegdown][pegdown], Github Flavored Markdown extensions, such as fenced code blocks and tables are supported.

### StringTemplate

Paradox uses [StringTemplate][st] for the basic templating. For example:

```
$page.title$
```

is substituted with the title of the page.

### Properties front matter

Paradox allows to specify some properties at page level using `---` delimiters

The `out` property allows to rename the target name of the current file.
And the `layout` allows to specify the layout we want to be used for this particular page, the layouts are placed by default in the `target/paradox/theme` folder:

```
---
out: newIndex.html
layout: templateName
---

/*
 * Content of the page 
 */
```

where `newIndex.html` will be the new name of the generated file and `templateName` is the name of a template, which corresponds more precisely to the file `templateName.st`.

Moreover, it is possible to specify the properties defined at page level inside the template files by using the `$` delimiters, for example: `$out$`

### Generic directive

In addition Paradox extends Markdown in a principled manner called generic directives syntax,
which basically means that all of our extensions would start with `@` (for inline), `@@` (leaf block), or `@@@` (container block).

### @ref link

Paradox extensions are designed so the resulting Markdown is Github friendly.
For example, you might want to link from one document to the other, let's say from `index.md` to `setup/index.md`.

```
See @ref:[Setup](setup/index.md) for more information.
```

This will render to be `setup/index.html` in the HTML, but the source on Github will link correct as well!

### @@@index container

`@@@index` is used to list child pages or sections from a page.
For example, your main `index.md` could contain something like this:

```
@@@ index

* [Setup](setup/index.md)
* [Usage](usage/index.md)

@@@
```

Inside `setup/index.md` can list its own child pages as follows:


```
@@@ index

* [sbt](sbt.md)
* [Maven](maven.md)
* [Gradle](gradle.md)

@@@
```

Paradox will walk through these indices and create a hierarchical navigation sidebar:

![index](docs/index.png)

Similar to `@ref`, the source document on Github will link correctly the other sources.

### @@toc block

The "generic" theme already renders a hierarchical navigation sidebar,
but let's say you would like to render a more detailed table of contents for a section overview page.

The `@@toc` block is used to include a table of content with arbitrary depth.

```
@@toc { depth=2 }
```

This will render the page names (first header), and the second headers.

![toc](docs/toc.png)

### @@snip block

The `@@snip` block is used to include code snippets from another file.

```
@@snip [Hello.scala](../scala/Hello.scala) { #hello_example }
```

Inside of `Hello.scala` mark the desired section you want to extract using the `#hello_example` label as follows:

```scala
// #hello_example
object Hello extends App {
  println("hello")
}
// #hello_example
```

This lets us compile and test the source before including it in the documentation.
The snippet is rendered with code formatting like this:

![snip](docs/snip.png)

To display multiple snippets in a tabbed view, use definition list syntax as follows:

```markdown
sbt
:   @@snip [build.sbt](../../../build.sbt) { #setup_example }

Maven
:   @@snip [pom.xml](../../../pom.xml) { #setup_example }

Gradle
:   @@snip [build.gradle](../../../build.gradle) { #setup_example }
```

This will be rendered like this:

![multi_snip](docs/multi_snip.png)

### Parameterized links

Parameterized link directives help to manage links that references
external documentation, such as API documentation or source code. The
directives are configured via base URLs defined in `paradoxProperties`:

```sbt
paradoxProperties in Compile ++= Map(
  "github.base_url" -> s"https://github.com/lightbend/paradox/tree/${version.value}",
  "scaladoc.akka.base_url" -> s"http://doc.akka.io/api/${Dependencies.akkaVersion}",
  "extref.rfc.base_url" -> "http://tools.ietf.org/html/rfc%s"
)
```

After which the directives can be used as follows:

```markdown
The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL
NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED",  "MAY", and
"OPTIONAL" in this document are to be interpreted as
described in @extref[RFC 2119](rfc:2119).

...

Use a @scaladoc[Future](scala.concurrent.Future) to avoid that long
running operations block the @scaladoc[Actor](akka.actor.Actor).

...

Issue @github[#1](#1) was fixed in commit @github[83986f9](83986f9).
```

*NOTE*: Only use these directives if standard Markdown and `@ref` does
not work, since GitHub won't preview them correctly.

#### @scaladoc directive

Use the `@scaladoc` directives to link to Scaladoc sites based on the package
prefix. Scaladoc URL mappings can be configured via the properties
`scaladoc.<package-prefix>.base_url` and the default `scaladoc.base_url`.
The directive will match the link text with the longest common package prefix and use the default base URL as a fall-back if nothing else matches.

For example, given:

 - `scaladoc.akka.base_url=http://doc.akka.io/api/akka/2.4.10`
 - `scaladoc.akka.http.base_url=http://doc.akka.io/api/akka-http/10.0.0`

Then `@scaladoc[Http](akka.http.scaladsl.Http$)` will resolve to
<http://doc.akka.io/api/akka-http/10.0.0/#akka.http.scaladsl.Http$>.

By default, `scaladoc.scala.base_url` is configured to the Scaladoc
associated with the configured `scalaVersion`. If the sbt project's
`apiURL` setting is configured, it is used as the default Scaladoc base
URL.

#### @github directive

Use the `@github` directive to link to GitHub issues, commits and files.
It supports most of [GitHub's autolinking syntax][github-autolinking].

The `github.base_url` property must be configured to use shorthands such
as `#1`. For source code links to a specific version set the base URL to
a tree revision, for example:
<https://github.com/lightbend/paradox/tree/v0.2.1>.

If the sbt project's `scmInfo` setting is configured and the `browseUrl`
points to a GitHub project, it is used as the GitHub base URL.

[github-autolinking]: https://help.github.com/articles/autolinked-references-and-urls/

#### @extref directive

Use the `@extref` directive to link to pages using custom URL templates.
URL templates can be configured via `extref.<scheme>.base_url` and the
template may contain one `%s` which is replaced with the scheme specific
part of the link URL. For example, given the property:

    scaladoc.rfc.base_url=http://tools.ietf.org/html/rfc%s

then `@extref[RFC 2119](rfc:2119)` will resolve to the URL
<http://tools.ietf.org/html/rfc2119>.

License and credits
-------------------

- Copyright 2015-2016 Lightbend, Inc. Paradox is provided under the Apache 2.0 license.
- The markdown engine is based on Mathias's [Pegdown][pegdown].

  [pegdown]: http://pegdown.org
  [st]: http://www.stringtemplate.org/
