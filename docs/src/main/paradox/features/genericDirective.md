## Generic directive

Paradox extends Markdown in a principled manner called generic directives syntax,
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

![index](../img/index.png)

Similar to `@ref`, the source document on Github will link correctly the other sources.

### @@toc block

The "generic" theme already renders a hierarchical navigation sidebar,
but let's say you would like to render a more detailed table of contents for a section overview page.

The `@@toc` block is used to include a table of content with arbitrary depth.

```markdown
@@toc { depth=2 }
```

This will render the page names (first header), and the second headers.

![toc](../img/toc.png)

### @@snip block

The `@@snip` block is used to include code snippets from another file.

```markdown
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

![snip](../img/snip.png)

To display multiple snippets in a tabbed view, use definition list syntax as follows:

```markdown
sbt
:   @@snip [build.sbt](/../../../build.sbt) { #setup_example }
Maven
:   @@snip [pom.xml](../../../pom.xml) { #setup_example }
Gradle
:   @@snip [build.gradle](../../../build.gradle) { #setup_example }
```

This will be rendered like this:

![multi_snip](../img/multi_snip.png)