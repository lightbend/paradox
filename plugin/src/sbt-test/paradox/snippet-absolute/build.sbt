lazy val root = (project in file("."))
  .aggregate(docs)

lazy val docs = (project in file("docs"))
  .enablePlugins(ParadoxPlugin)
  .settings(
    paradoxTheme := None,
    paradoxRoots := List("absolute.html")
  )
