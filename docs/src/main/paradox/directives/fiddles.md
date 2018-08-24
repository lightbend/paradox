Fiddle inclusion
-----------------

### @@fiddle block

The `@@fiddle` block is used to include code snippets from another file that are runnable in [scalafiddle.io](https://scalafiddle.io/).

```markdown
@@fiddle [Hello.scala](/docs/src/main/scala/Hello.scala) { #hello_example }
```

Inside of `Hello.scala` mark the desired section you want to extract using the `#hello_fiddle` label as follows:

```scala
// #hello_fiddle
  println("hello")
// #hello_fiddle
```

This lets us compile and test the source before including it in the documentation.
The fiddle is rendered with code formatting like this and the top right button let you run your code using and embedded view of [scalafiddle](https://scalafiddle.io/):

@@fiddle [Hello.scala](/docs/src/main/scala/Hello.scala) { #hello_fiddle }

Valid fiddle directive's attributes are:

  - prefix
  - dependency
  - scalaversion
  - template
  - theme
  - minheight
  - layout

and their usage is described [here](https://github.com/scalafiddle/scalafiddle-core/blob/master/integrations/README.md#integration-parameters).

### `fiddle.*.base_dir`

In order to specify your fiddle source paths off certain base directories you can define placeholders
either in the page's front matter or globally like this (for example):

```sbt
paradoxProperties in Compile ++= Map(
  "fiddle.foo.base_dir" -> "/docs/src/main/../some/dir",
  "fiddle.test.base_dir" -> s"${(sourceDirectory in Test).value}/scala/org/example",
  "fiddle.project.base_dir" -> (baseDirectory in ThisBuild).value.getAbsolutePath
)
```

and it will work such as described for `snippets`.
