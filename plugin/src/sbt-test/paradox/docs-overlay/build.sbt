val DocsFirst = config("docs-first")
val DocsSecond = config("docs-second")

lazy val docs = (project in file(".")).
  enablePlugins(ParadoxPlugin).
  settings(
    paradoxTheme := None,
    ParadoxPlugin.paradoxSettings(DocsFirst),
    ParadoxPlugin.paradoxSettings(DocsSecond),
    // paradoxOverlayDirectories := Seq(baseDirectory.value / "src" / "commonFirst"),
    paradoxOverlayDirectories in DocsFirst := Seq(baseDirectory.value / "src" / "commonFirst"),
    paradoxOverlayDirectories in DocsSecond := Seq(baseDirectory.value / "src" / "commonFirst", baseDirectory.value / "src" / "commonSecond"),
    paradoxExpectedNumberOfRoots in DocsFirst := 4,
    paradoxExpectedNumberOfRoots in DocsSecond := 6,
  )
