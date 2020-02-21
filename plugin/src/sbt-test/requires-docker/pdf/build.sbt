lazy val docs = project
  .in(file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    version := "0.1-SNAPSHOT",
    name := "Paradox Test",
    paradoxTheme := None,
    paradoxRoots := List("index.html"),
    paradoxPdfArgs := List(
        "--footer-right", "[page]",
        "--footer-left", (name in paradoxPdf).value
    )
  )

// There is a creation date in the PDFs, it must be excluded to compare the two files
lazy val comparePdfs = inputKey[Unit]("Compare two PDF files, excluding creation dates")

import complete.DefaultParsers._

comparePdfs := {
  val Seq(f1, f2) = spaceDelimited("<arg>").parsed
  val log = streams.value.log
  @annotation.tailrec
  def splitBytesByLines(bytes: Seq[Byte], lines: Seq[Array[Byte]]): Seq[Array[Byte]] = {
    val (line, rest) = bytes.span(_ != '\n')
    val newLines = lines :+ line.toArray
    rest.drop(1) match {
        case Nil => newLines
        case remaining => splitBytesByLines(remaining, newLines)
    }
  }

  val creationDate = "/CreationDate".getBytes

  def readLines(name: String) = {
    splitBytesByLines(IO.readBytes(file(name)).view, Vector())
  }

  val f1Lines = readLines(f1)
  val f2Lines = readLines(f2)
  def toPrintable(b: Byte) = if (b >= 0x20 && b < 0x7F) b.toChar else '.'
  f1Lines.zip(f2Lines).zipWithIndex.foreach {
    case ((line1, line2), idx) =>
      if (line1.startsWith(creationDate) && line2.startsWith(creationDate)) {
        // Ignore
      } else {
        if (!(line1 sameElements line2)) {
          log.error(s"Line $idx of $f1 is not equal to $f2")
          log.error(new String(line1.map(toPrintable)))
          log.error(new String(line2.map(toPrintable)))
          throw new AlreadyHandledException(new RuntimeException())
        }
      }
  }
  if (f1Lines.length != f2Lines.length) {
    log.error(s"$f1 has ${f1Lines.length} lines but $f2 has ${f2Lines.length}")
    throw new AlreadyHandledException(new RuntimeException())
  }
}