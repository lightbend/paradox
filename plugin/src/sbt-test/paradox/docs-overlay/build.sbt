val DocsFirst  = config("docs-first")
val DocsSecond = config("docs-second")

lazy val docs = (project in file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    paradoxTheme := None,
    ParadoxPlugin.paradoxSettings(DocsFirst),
    ParadoxPlugin.paradoxSettings(DocsSecond),
    // paradoxOverlayDirectories := Seq(baseDirectory.value / "src" / "commonFirst"),
    paradoxOverlayDirectories in DocsFirst  := Seq(baseDirectory.value / "src" / "commonFirst"),
    paradoxOverlayDirectories in DocsSecond := Seq(
      baseDirectory.value / "src" / "commonFirst",
      baseDirectory.value / "src" / "commonSecond"
    ),
    paradoxRoots in DocsFirst := List(
      "commonFirst.html",
      "commonFirstDir/commonFirstFile.html",
      "docsFirstDir/docsFirstSubfile.html",
      "docsFirstFile.html"
    ),
    paradoxRoots in DocsSecond := List(
      "commonFirst.html",
      "commonFirstDir/commonFirstFile.html",
      "commonSecond.html",
      "commonSecondDir/commonSecondFile.html",
      "docsSecondDir/docsSecondSubfile.html",
      "docsSecondFile.html"
    )
  )
