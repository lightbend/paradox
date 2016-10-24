## Setup

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

## Documents

Your markdown documentation will go inside `src/main/paradox/`. For example, you can start with `src/main/paradox/index.md`.

@@@ index

* [Generic Directives](features/genericDirective.md)
* [Parameterized Links](features/paramLinks.md)
* [String Template](features/stringTemplate.md)

@@@