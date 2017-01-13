Documentation overlay
---------------------

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