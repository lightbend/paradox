// Uses generic theme (sbt-paradox-material-theme not published for sbt 2)
import com.lightbend.paradox.sbt.ParadoxPlugin.autoImport.builtinParadoxTheme

lazy val libraryDependencyTest = project
  .in(file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    TaskKey[Unit]("check") := {
      val siteDir = (Compile / paradox / target).value
      assert(siteDir.exists, s"paradox site not found at ${siteDir.getAbsolutePath}")
    },
    TaskKey[Unit]("verifyTheme") := {
      val siteDir   = (Compile / paradox / target).value
      val indexHtml = siteDir / "index.html"
      assert(indexHtml.exists, s"Paradox site index not found at ${indexHtml.getAbsolutePath}")
    }
  )
