val release          = project.configure(docs("2.12.3"))
val releaseCandidate = project.configure(docs("2.12.0-RC1"))
val milestone        = project.configure(docs("2.13.0-M1"))

def docs(scalaV: String)(project: Project) =
  project
    .enablePlugins(ParadoxPlugin)
    .settings(
      version                                  := "0.1.0",
      scalaVersion                             := scalaV,
      paradoxTheme                             := None,
      Compile / paradoxTheme / sourceDirectory := (ThisBuild / baseDirectory).value / "theme",
      Compile / paradox / sourceDirectory      := (ThisBuild / baseDirectory).value / "doc"
    )
