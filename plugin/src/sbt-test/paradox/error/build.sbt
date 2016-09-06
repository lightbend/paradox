lazy val docs = (project in file(".")).
  enablePlugins(ParadoxPlugin).
  settings(
    paradoxTheme := None
  )
