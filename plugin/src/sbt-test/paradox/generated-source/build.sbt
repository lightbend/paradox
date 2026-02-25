lazy val docs = project
  .in(file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    name         := "Paradox Source Generation Test",
    paradoxTheme := None,
    Compile / paradoxMarkdownToHtml / sourceGenerators += PageGenerator.generatePages.taskValue
  )
