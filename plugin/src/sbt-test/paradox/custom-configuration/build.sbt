val Docs1 = config("docs1")
val Docs2 = config("docs2")

enablePlugins(ParadoxPlugin)

ParadoxPlugin.paradoxSettings(Docs1)
ParadoxPlugin.paradoxSettings(Docs2)

TaskKey[Unit]("makeDocs") := {
  (Docs1 / paradox).value
  (Docs2 / paradox).value
}

TaskKey[Unit]("check") := {
  def checkFileContent(file: File, expected: String) = {
    assert(file.exists, s"${file.getAbsolutePath} does not exists")
    val contents = IO.readLines(file)
    assert(contents.exists(_.contains(expected)), s"Did not find '$expected' in\n${contents.mkString("\n")}")
  }

  checkFileContent((Docs1 / paradox / target).value / "index.html", "Docs1 index")
  checkFileContent((Docs2 / paradox / target).value / "index.html", "Docs2 index")
}
