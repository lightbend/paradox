lazy val templateTest = project
  .in(file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    // set the generic theme to test overriding page.st
    paradoxTheme := Some(builtinParadoxTheme("generic"))
  )
