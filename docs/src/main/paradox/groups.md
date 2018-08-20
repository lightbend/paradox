# Groups

Paradox supports 'groups' which allow users to easily switch between different
variants of the documentation.

## Configuration

Groups must be configured through sbt:

```
.enablePlugins(NoPublish, ParadoxPlugin)
  .settings(
    name := "paradox docs",
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    paradoxGroups := Map("Language" -> Seq("Scala", "Java"))
  )
```

## Syntax

### Tabs

Groups are used for tabs, where the group is determined by the tab name:

@@@vars
```markdown
Java
:   @@snip [example-first.java](/docs/src/main/resources/tab-switching/examples.java) { #java_first }
$empty$
Scala
:   @@snip [example-first.scala](/docs/src/main/resources/tab-switching/examples.scala) { #scala_first }
```
@@@

Java
:   @@snip [example-first.java](/docs/src/main/resources/tab-switching/examples.java) { #java_first }

Scala
:   @@snip [example-first.scala](/docs/src/main/resources/tab-switching/examples.scala) { #scala_first }

or by the group parameter on the snippet:

@@@vars
```markdown
example-first.java
:   @@snip [example-first.java](../../resources/tab-switching/examples.java) { #java_first group=java }
$empty$
example-first.scala
:   @@snip [example-first.scala](/docs/src/main/resources/tab-switching/examples.scala) { #scala_first group=scala }
```
@@@

example-first.java
:   @@snip [example-first.java](/docs/src/main/resources/tab-switching/examples.java) { #java_first group=java }

example-first.scala
:   @@snip [example-first.scala](/docs/src/main/resources/tab-switching/examples.scala) { #scala_first group=scala }


But tabs not associated with groups are left alone:

sbt
:   @@snip [build.sbt](/docs/src/main/resources/build.sbt) { #setup_example }

Maven
:   @@snip [pom.xml](/docs/src/main/resources/pom.xml) { #setup_example }

Gradle
:   Non-snippet tab body


### Inline

For each group a directive is automatically created. This can be used for
switching inline text:

```
Text describing the @java[Java variant]@scala[Scala variant containing ***markdown*** and @ref:[Linking](linking.md)].
```

Text describing the @java[Java variant]@scala[Scala variant containing ***markdown*** and @ref:[Linking](directives/linking.md)].

### Directives

You can also use groups with directives such as @ref[`@@@div`](directives/css-friendliness.md#div)
and @ref[`@@@note`](directives/callouts.md#note) as follows:

```
@@@ div { .group-scala }

This only shows up when the `group` is "scala"

@@@

@@@ note { .group-java }

This only shows up when the `group` is "java"

@@@
```

@@@ div { .group-scala }

This only shows up when the `group` is "scala"

@@@

@@@ note { .group-java }

This only shows up when the `group` is "java"

@@@

## Behavior

Switching is currently done in javascript. When the page loads, a script in
`page.js` looks for entities with class `supergroup` and derives the groups
catalog from that. If you use a custom theme, use `$page.groups$` to generate
the catalog in javascript.

The currently selected group for each category is stored in a cookie.

## Extending

You can register an event listener that will be called whenever a group is switched:

```
window.groupChanged(function(group, supergroup, catalog) {
  // your code here
});
```

## Linking

The current group is typically determined by the order in which is was defined, or the previously-selected
group as stored in a cookie. It is also possible to specify the desired group as a query paramter,
so you can link to [this page with Java selected](?language=java) or [this page with Scala selected](?language=scala).
