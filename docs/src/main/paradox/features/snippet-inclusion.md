Snippet inclusion
-----------------

### @@snip block

The `@@snip` block is used to include code snippets from another file.

```markdown
@@snip [Hello.scala](../../scala/Hello.scala) { #hello_example }
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

@@snip [Hello.scala](../../scala/Hello.scala) { #hello_example }

To display multiple snippets in a tabbed view, use definition list syntax as follows:

@@@vars
```markdown
sbt
:   @@snip [build.sbt](../../resources/build.sbt) { #setup_example }
$empty$
Maven
:   @@snip [pom.xml](../../resources/pom.xml) { #setup_example }
$empty$
Gradle
:   @@snip [build.gradle](../../resources/build.gradle) { #setup_example }
```
@@@

This will be rendered like this:

sbt
:   @@snip [build.sbt](../../resources/build.sbt) { #setup_example }

Maven
:   @@snip [pom.xml](../../resources/pom.xml) { #setup_example }

Gradle
:   @@snip [build.gradle](../../resources/build.gradle) { #setup_example }

By default, Paradox uses Prettify to highlight code and will try to detect the
language of the snippet using the file extension. In cases where a snippet
should not be highlighted set `type=text` in the directive's attribute section:

```markdown
@@snip [example.log](example.log) { #example-log type=text }
```

### snip.*.base_dir

In order to specify your snippet source paths off certain base directories you can define placeholders
either in the page's front matter or globally like this (for example):

```sbt
paradoxProperties in Compile ++= Map(
  "snip.foo.base_dir" -> "../../../some/dir",
  "snip.test.base_dir" -> s"${(sourceDirectory in Test).value}/scala/org/example"
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
SBT project's root directory.

**Note**: Using this feature will not allow GitHub to follow the snippet links correctly on the web UI.
