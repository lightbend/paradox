---
key: This is my key value
---

# Variable Substitution

Often it is necessary to transport values from the build into the documentation, like the current version of the project.
This can be done with properties. Properties can be defined with sbt settings and can then be accessed in documentation 
Markdown files.

## Defining Properties


### With Sbt Settings

To define properties add a key / value tuple to the `paradoxProperties` setting. For example, to make the Scala version
of the build available to the documentation you can use this (in fact, this property is already [predefined](#predefined-properties)): 

```
paradoxProperties += ("scala.version" -> scalaVersion.value)
```

### With Front-Matter

You can set or override property values with "front-matter" on pages. At the beginning of a Markdown file, add a block

```
---
key: This is my key value
---
```

to change a property value just for this page.

## Referencing Properties From Markdown

In Markdown you can either directly reference properties using dollar notation `$property$`.
 
For example, this snippet:

```
The scala version is "$scala.version$".
```

renders as:

The scala version is "$scala.version$".
 
Alternatively, you can also use the @ref[`@var`](directives/vars.md#var) or @ref[`@@@vars`](directives/vars.md#vars)
directives to insert property values depending on the context.

## Predefined Properties

 - `project.name`: Name of the project
 - `project.version`: Project version
 - `project.version.short`: Project version with `-SNAPSHOT` replaced by `*`
 - `project.description`: Project description
 - `scala.version`: Project Scala Version
 - `scala.binary.version`: Project Scala binary compatible version (e.g. "2.12")
 - `date`: Generation date in the form `MMM DD, YYYY`
 - `date.day`: Generation day of the month
 - `date.month`: Generation month
 - `date.year`: Generation year

## Special Properties

Some properties have special meaning for the generation engine. See the @ref[Templating](customization/templating.md#properties-front-matter)
section for more information.
