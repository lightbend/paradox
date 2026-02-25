import com.lightbend.paradox.markdown.Writer

lazy val docs = project
  .in(file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    name         := "Paradox Directives Test",
    paradoxTheme := None,
    paradoxDirectives := Def.uncached(Writer.defaultDirectives :+ CustomDirective),
    paradoxProperties += "custom.content" -> "directive",
    paradoxRoots := List("test.html")
  )
