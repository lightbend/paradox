Snippet inclusion
-----------------

### @@snip block

The `@@snip` block is used to include code snippets from another file.

```markdown
@@snip [Hello.scala](/docs/src/main/scala/Hello.scala) { #hello_example }
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

@@snip [Hello.scala](/docs/src/main/scala/Hello.scala) { #hello_example }

To display multiple snippets in a tabbed view, use definition list syntax as follows:

@@@vars
```markdown
sbt
:   @@snip [build.sbt](/docs/src/main/resources/build.sbt) { #setup_example }
$empty$
Maven
:   @@snip [pom.xml](/docs/src/main/resources/pom.xml) { #setup_example }
$empty$
Gradle
:   @@snip [build.gradle](/docs/src/main/resources/build.gradle) { #setup_example }
```
@@@

This will be rendered like this:

sbt
:   @@snip [build.sbt](/docs/src/main/resources/build.sbt) { #setup_example }

Maven
:   @@snip [pom.xml](/docs/src/main/resources/pom.xml) { #setup_example }

Gradle
:   @@snip [build.gradle](/docs/src/main/resources/build.gradle) { #setup_example }


#### Label filtering

Any lines containing `#labels` within the included snippet are filtered out. This filtering can
be switched off with `filterLabels`. It is off by default for snippets that include the whole file
(without limiting the snippet by providing a label) and can be set to `true` to overwrite that.

```markdown
@@snip [example.log](example.log) { #example-log filterLabels=false }
```

The default value is set with the `include.filterLabels` property.

```
paradoxProperties += "snip.filterLabels" -> "false"
```

This label filtering applies to @ref:[Markdown includes](includes.md) and @ref:[Fiddle includes](fiddles.md), as well.

```
paradoxProperties += "include.filterLabels" -> "false",
paradoxProperties += "fiddle.filterLabels" -> "false"
```


#### Syntax highlighting

By default, Paradox uses Prettify to highlight code and will try to detect the
language of the snippet using the file extension. In cases where a snippet
should not be highlighted set `type=text` in the directive's attribute section:

```markdown
@@snip [example.log](example.log) { #example-log type=text }
```

#### Tab Switching

It is possible to associate multiple snippets under the same "tag". If some tab of a snippet is switched by the user, all tabs associated with the selected one will be switched as well.

@@@vars
```markdown
First-java
:   @@snip [example-first.java](/docs/src/main/resources/tab-switching/examples.java) { #java_first }
$empty$
First-scala
:   @@snip [example-first.scala](/docs/src/main/resources/tab-switching/examples.scala) { #scala_first }
$empty$
Some separator.
$empty$
Java
:   @@snip [example-second.java](/docs/src/main/resources/tab-switching/examples.java)
$empty$
Scala
:   @@snip [example-second.scala](/docs/src/main/resources/tab-switching/examples.scala)
```
@@@

The result will be rendered like this (try to switch tabs):

First-java
:   @@snip [example-first.java](/docs/src/main/resources/tab-switching/examples.java) { #java_first group=java }

First-scala
:   @@snip [example-first.scala](/docs/src/main/resources/tab-switching/examples.scala) { #scala_first group=scala }

Some separator.

Java
:   @@snip [example-second.java](/docs/src/main/resources/tab-switching/examples.java)

Scala
:   @@snip [example-second.scala](/docs/src/main/resources/tab-switching/examples.scala)

This is also synced if some tabs have no snippet:

Java
:   @@snip [example-second.java](/docs/src/main/resources/tab-switching/examples.java)

Scala
:   More inline tabbing

### `snip.*.base_dir`

In order to specify your snippet source paths off certain base directories you can define placeholders
either in the page's front matter or globally like this (for example):

```sbt
paradoxProperties in Compile ++= Map(
  "snip.foo.base_dir" -> "/docs/src/main/../some/dir",
  "snip.test.base_dir" -> s"${(sourceDirectory in Test).value}/scala/org/example",
  "snip.project.base_dir" -> (baseDirectory in ThisBuild).value.getAbsolutePath
)
```

You can then refer to one of the defined base directories by starting the snippet's target path with `$placeholder$`,
for example:

```markdown
@@snip [Hello.scala]($foo$/Hello.scala) { #hello_example }

@@snip [Yeah.scala]($test$/Yeah.scala) { #yeah_example }

@@snip [Yeah.scala]($root$/src/test/scala/org/example/Yeah.scala) { #yeah_example }
```

If a placeholder directory is relative (like `$foo$` in this example) it'll be based of the path of the respective page
it is used in. Also, *paradox* always auto-defines the placeholder `$root$` to denote the absolute path of the
sbt (sub)project's root directory.

**Note**: Using this feature will not allow GitHub to follow the snippet links correctly on the web UI.


### Link to full source at GitHub

By default a snippet is followed by a link to the source file at Github. This can be switched off by setting `snip.github_link` to `false`.

```sbt
paradoxProperties in Compile ++= Map(
  "snip.github_link" -> "false"
)
```
