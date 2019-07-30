lazy val docs = project
  .in(file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    version := "0.1-SNAPSHOT",
    name := "Paradox Test",
    paradoxLeadingBreadcrumbs := List("Alphabet" -> "https://abc.xyz/", "Google" -> "https://www.google.com"),
    paradoxTheme := None,
    paradoxRoots := List("a.html"),
  )
