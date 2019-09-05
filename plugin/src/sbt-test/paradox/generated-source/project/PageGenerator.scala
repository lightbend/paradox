import com.lightbend.paradox.sbt.ParadoxPlugin.autoImport.paradox
import sbt.Keys._
import sbt._

object PageGenerator {
  def generatePages: Def.Initialize[Task[Seq[java.io.File]]] = Def.task {
    val firstPage = (sourceManaged in (Compile, paradox)).value / "generated-page.md"
    IO.write(firstPage, "Generated Page 1")

    val secondPage = (sourceManaged in (Compile, paradox)).value / "generated" / "page.md"
    IO.write(secondPage, "Generated Page 2")

    Seq(firstPage, secondPage)
  }
}
