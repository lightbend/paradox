lazy val docs = project
  .in(file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    name                            := "Paradox Navigation Test",
    paradoxTheme                    := None,
    paradoxNavigationDepth          := 1,
    paradoxNavigationExpandDepth    := Some(1),
    paradoxNavigationIncludeHeaders := true
  )
