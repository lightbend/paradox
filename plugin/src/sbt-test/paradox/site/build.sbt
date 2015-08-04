lazy val docs = project
  .in(file("."))
  .enablePlugins(Paradox)
  .settings(
    name := "Paradox Test"
  )
