lazy val docs = project
  .in(file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    name         := "Paradox Source Generation Test",
    paradoxTheme := None,
    sourceGenerators in (Compile, paradoxMarkdownToHtml) += PageGenerator.generatePages.taskValue
  )
