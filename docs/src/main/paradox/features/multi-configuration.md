Multi Configuration
-------------------

Paradox supports multiple sbt configurations. Each configuration is by default located to `src/configName` of the project, with the target directory defined as `target/paradox/site/configName`, `configName` corresponding to configuration.name of a particular configuration. There still remains the usual main project in `src/main` of course if you don't need multiple paradox project directories.

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