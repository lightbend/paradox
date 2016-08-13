Paradox
=======

Paradox is a documentation tool for software projects.

**Status**: In development.

**Currently Paradox is not supported under Lightbend subscription.**

Setup
-----

Find [the lastest](https://github.com/lightbend/paradox/releases) version, and create `project/paradox.sbt`:

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

Documents
---------

Your markdown documentation wil go inside `src/main/paradox/`. For example, you can start with `src/main/paradox/index.md`.

### GFM

Powered by [Pegdown][pegdown], Github Flavored Markdown extensions fensed code formatting and table are both supported.

### StringTemplate

Paradox uses [StringTemplate][st] for the basic templating. For example:

```
$page.title$
```

is substituted with the title of the page.

### Generic directive

In addition Paradox extends Markdown in a principled manner called generic directives syntax,
which basiclly means that all of our extensions would start with `@` (for inline), `@@` (leaf block), or `@@@` (container block).

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

Paradox will walk through these indices and create a hierachical navigation:

![index](docs/index.png)

Similar to `@ref`, the source document on Github will link correctly the other sources.

### @@toc block

The "generic" theme already renders a hierachical navigation,
but let's say you would like to render a more detailed table of contents for a section overview page.

`@@toc` block is used to include a table of content with arbitrary depth.

```
@@toc { depth=2 }
```

This will render the page names (first header), and the second headers.

![toc](docs/toc.png)

### @@snip block

`@@snip` block is used to include code snippets from another file.

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

To display muliple snippets in a tabbed view, use definition list syntax as follows:

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

License and credtis
-------------------

- Copyright 2015-2016 Lightbend, Inc. Paradox is provided under Apache 2.0 license.
- The markdown engine is based on Mathias's [Pegdown][pegdown].

  [pegdown]: http://pegdown.org
  [st]: http://www.stringtemplate.org/
