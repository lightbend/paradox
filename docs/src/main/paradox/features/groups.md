Groups
------

Paradox supports 'groups' which allow users to easily switch between different
'variations' of the documentation.

## Configuration

Groups must be configured through sbt:

```
.enablePlugins(NoPublish, ParadoxPlugin)
  .settings(
    name := "paradox docs",
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    paradoxGroups := Map("Languages" -> Seq("Scala", "Java"))
  )
```

## Syntax

### Tabs

Groups are used for tabs, where the group is determined by the tab name:

@@@vars
```markdown
Java
:   @@snip [example-first.java](../../resources/tab-switching/examples.java) { #java_first }
$empty$
Scala
:   @@snip [example-first.scala](../../resources/tab-switching/examples.scala) { #scala_first }
```
@@@

Java
:   @@snip [example-first.java](../../resources/tab-switching/examples.java) { #java_first }

Scala
:   @@snip [example-first.scala](../../resources/tab-switching/examples.scala) { #scala_first }

or by the group parameter on the snippet:

@@@vars
```markdown
example-first.java
:   @@snip [example-first.java](../../resources/tab-switching/examples.java) { #java_first group=java }
$empty$
example-first.scala
:   @@snip [example-first.scala](../../resources/tab-switching/examples.scala) { #scala_first group=scala }
```
@@@

example-first.java
:   @@snip [example-first.java](../../resources/tab-switching/examples.java) { #java_first group=java }

example-first.scala
:   @@snip [example-first.scala](../../resources/tab-switching/examples.scala) { #scala_first group=scala }


But tabs not associated with groups are left alone:

sbt
:   @@snip [build.sbt](../../resources/build.sbt) { #setup_example }

Maven
:   @@snip [pom.xml](../../resources/pom.xml) { #setup_example }

Gradle
:   Non-snippet tab body


### Inline

For each group a directive is automatically created. This can be used for
switching inline text:

```
Text describing the @java[Java variant]@scala[Scala variant containing ***markdown*** and @ref:[Linking](linking.md)].
```

Text describing the @java[Java variant]@scala[Scala variant containing ***markdown*** and @ref:[Linking](linking.md)].


## Behavior

Switching is currently done in javascript. When the page loads, a script in
`page.js` looks for entities with class `supergroup` and derives the groups
catalog from that. If you use a custom theme, use `$groups()$` to generate
the catalog in javascript.

The currently selected group for each category is stored in a cookie.
