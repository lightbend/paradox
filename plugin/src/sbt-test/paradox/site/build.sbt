lazy val docs = project
  .in(file("."))
  .enablePlugins(Paradox)
  .settings(
    ParadoxKeys.paradoxProperties in Compile += "version" -> version.value
  )
