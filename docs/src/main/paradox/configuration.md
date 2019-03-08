# Configuration

Paradox is configured with sbt settings.

TBD - explain main settings here

## Overlays

It is possible to add one or more overlays to a paradox project. Their location can be defined at build level and is applied to all configurations in the project unless we overwrite it for a particular configuration.

```scala
val DocsFirst = config("docs-first")
val DocsSecond = config("docs-second")

lazy val root = (project in file(".")).
  enablePlugins(ParadoxPlugin).
  settings(
    name := "Paradox Project",
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    paradoxOverlayDirectories := Seq(baseDirectory.value / "src" / "docs-common"),
    ParadoxPlugin.paradoxSettings(DocsFirst),
    ParadoxPlugin.paradoxSettings(DocsSecond),
    paradoxOverlayDirectories in DocsFirst := Seq(baseDirectory.value / "src" / "docs-first-common", baseDirectory.value / "src" / "docs-second-common")
  )
```

Markdown source files from the overlay directories are merged with the ones in the main project directory and are generated as if they were part of this latest.
If a file duplicate exist between the directories, the overlay file is dropped in favour of the main directory file.

## Multi Configuration

Paradox supports multiple sbt configurations. Each configuration is by default located to `src/configName` of the project,
with the target directory defined as `target/paradox/site/configName`, `configName` corresponding to configuration.name of
a particular configuration. There still remains the usual main project in `src/main` of course if you don't need multiple
paradox project directories.

To associate a configuration to paradox, use its settings, and change its default source and/or target directorie(s) if needed:

```scala
val SomeConfig = config("some-config")

lazy val root = (project in file(".")).
  enablePlugins(ParadoxPlugin).
  settings(
    paradoxTheme = Some(builtinParadoxTheme("generic")),
    ParadoxPlugin.paradoxSettings(SomeConfig),
    sourceDirectory in SomeConfig := baseDirectory.value / "src" / "configuration-source-directory",
    (target in paradox) in SomeConfig := baseDirectory.value / "paradox" / "site" / "configuration-target-directory"
  )
```

Now, either you run paradox on one configuration; "sbt someConfig:paradox" or you can run the main project with the usual way; "sbt paradox".

## Version warning

Paradox supports showing a warning when users browse documentation which is not the current released version. This is achieved by storing a JSON file (`paradox.json`) together with the generated site and specifying a stable URL to where the released version will be available.

The built in theme (`generic`) contains Javascript to fetch the JSON file and compare the version with the version for which the documentation showing was generated. Whenever they differ, a warning text shows on every page offering a link to the released version's page.

To use this functionality, make `project.url` point to a URL where the **current** version will be deployed. It defaults to the sbt `homepage` setting. Make sure `homepage` contains something useful.

To set a URL different from `homepage`, add `project.url` to Paradox properties

```scala
paradoxProperties += ("project.url" -> "https://developer.lightbend.com/docs/paradox/current/")
```

## Canonical URL

The built-in theme (`generic`) will add a `<link rel="canonical" href=...` page header using the sbt `homepage` setting. Make sure `homepage` contains something useful.

To set a URL different from `homepage`, add `canonical.base_url` to Paradox properties

```scala
paradoxProperties += ("canonical.base_url" -> "https://developer.lightbend.com/docs/paradox/current/")
```

## HTML description tag

Most Paradox themes create the html tag `<meta name="description" content=` with text from the sbt `description` setting. Make sure to set in or overwrite it project-wide with

```scala
paradoxProperties += ("project.description" -> "A useful description text for search engines to contemplate")
```

You may set a page specific description by adding it to the @ref:[Front matter](variable-substitution.md) like this

```markdown
---
project.description: This page is very useful for whatever you want to know.
---
```
