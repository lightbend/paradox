lazy val docs = project
  .in(file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    name := "Paradox Default Theme Test",
    paradoxProperties += ("canonical.base_url" -> "https://example.com/doc/"),
    paradoxProperties in Compile ~= { _.updated("date.year", "2019") },
    paradoxRoots := List("index.html", "sub/unindexed.html"),
  )
