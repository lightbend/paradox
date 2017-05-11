lazy val root = (project in file(".")).
  enablePlugins(ParadoxPlugin).
  settings(
    paradoxTheme := None,
    name := "snippet-indent"
  )