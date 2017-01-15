lazy val docs = (project in file(".")).
  enablePlugins(ParadoxPlugin).
  settings(
    paradoxTheme := None,
    paradoxProperties in Compile ++= Map(
      "snip.test.base_dir" -> (sourceDirectory in Test).value.toString,
      "snip.test-scala.base_dir" -> "../../test/scala")
  )
