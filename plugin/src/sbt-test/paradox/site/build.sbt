lazy val docs = project
  .in(file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    name := "Paradox Test"
  )
