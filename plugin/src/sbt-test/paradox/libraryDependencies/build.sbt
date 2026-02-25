lazy val libraryDependencyTest = project
  .in(file("."))
  .enablePlugins(ParadoxPlugin)
  .enablePlugins(ParadoxMaterialThemePlugin)
  .settings(
    TaskKey[Unit]("check") := {
      val (_, file) = (makePom / packagedArtifact).value
      assert(file.exists, s"${file.getAbsolutePath} did not exist")
      val lines        = IO.readLines(file)
      val paradoxTheme = lines.find(_.matches(".*<artifactId>.*paradox.*theme.*</artifactId>.*"))
      assert(
        paradoxTheme.isEmpty,
        s"""pom contains paradox-theme dependency: ${paradoxTheme.map(_.trim)}
           |lines: ${lines.mkString("\n")}""".stripMargin
      )
    }
  )
