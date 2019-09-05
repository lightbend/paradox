lazy val docs = project
  .in(file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    name := "Paradox Source Generation Test",
    paradoxProperties += ("canonical.base_url" -> "https://example.com/doc/"),
    paradoxRoots := List("index.html"),
    sourceGenerators in (Compile, paradoxMarkdownToHtml) += PageGenerator.generatePages.taskValue
  )
