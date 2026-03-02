lazy val docs = (project in file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    paradoxTheme := None,
    Compile / paradoxProperties ++= Map(
      "snip.test.base_dir" -> (Test / sourceDirectory).value.toString,
      "snip.test-scala.base_dir" -> "../../test/scala"
    ),
    paradoxRoots := List(
      "configured-bases.html",
      "group.html",
      "multiple.html",
      "nocode.html",
      "reference.html",
      "snippets.html",
      "some-xml.html"
    )
  )
