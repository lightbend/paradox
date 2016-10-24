Snippet inclusion
-----------------

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
