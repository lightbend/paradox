lazy val docs = project
  .in(file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    name         := "Paradox Directives Test",
    paradoxTheme := None,
    paradoxDirectives += CustomDirective,
    paradoxProperties += "custom.content" -> "directive",
    paradoxRoots := List("test.html")
  )
