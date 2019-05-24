lazy val docs = project
  .in(file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    name := "Paradox Default Theme Test",
    paradoxProperties += ("canonical.base_url" -> "https://example.com/doc/"),
  )
