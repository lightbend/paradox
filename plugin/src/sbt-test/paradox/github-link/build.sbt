lazy val root = (project in file("."))
  .aggregate(docs)

lazy val docs = (project in file("docs"))
  .enablePlugins(ParadoxPlugin)
  .settings(
    paradoxTheme := None,
    paradoxProperties in Compile += "github.base_url" -> "https://github.com/lightbend/paradox/tree/develop"
  )
