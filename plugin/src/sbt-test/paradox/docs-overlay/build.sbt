val DocsFirst  = config("docsFirst")
val DocsSecond = config("docsSecond")

lazy val docs = (project in file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    paradoxTheme := None,
    ParadoxPlugin.paradoxSettings(DocsFirst),
    ParadoxPlugin.paradoxSettings(DocsSecond),
    // paradoxOverlayDirectories := Seq(baseDirectory.value / "src" / "commonFirst"),
    DocsFirst / paradoxOverlayDirectories  := Seq(baseDirectory.value / "src" / "commonFirst"),
    DocsSecond / paradoxOverlayDirectories := Seq(
      baseDirectory.value / "src" / "commonFirst",
      baseDirectory.value / "src" / "commonSecond"
    ),
    DocsFirst / paradoxRoots := List(
      "commonFirst.html",
      "commonFirstDir/commonFirstFile.html",
      "docsFirstDir/docsFirstSubfile.html",
      "docsFirstFile.html"
    ),
    DocsSecond / paradoxRoots := List(
      "commonFirst.html",
      "commonFirstDir/commonFirstFile.html",
      "commonSecond.html",
      "commonSecondDir/commonSecondFile.html",
      "docsSecondDir/docsSecondSubfile.html",
      "docsSecondFile.html"
    )
  )
