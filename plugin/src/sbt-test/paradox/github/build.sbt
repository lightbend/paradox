// setting values through custom keys because scripted tests do not like strings in 'set' statements
val scmInfoTree        = SettingKey[Some[ScmInfo]]("scmInfoTree")
val scmInfoProject     = SettingKey[Some[ScmInfo]]("scmInfoProject")
val taggedVersion      = SettingKey[String]("taggedVersion")
val githubBaseUrlEntry = SettingKey[(String, String)]("githubBaseUrlEntry")

lazy val root = project in file(".")

lazy val docs = (project in file("docs"))
  .enablePlugins(ParadoxPlugin)
  .settings(
    paradoxTheme := None,
    paradoxRoots := List("github.html"),
    scmInfoTree := Some(
      ScmInfo(url("https://github.com/lightbend/paradox/tree/v0.2.1"), "git@github.com:lightbend/paradox.git")
    ),
    scmInfoProject := Some(
      ScmInfo(url("https://github.com/lightbend/paradox"), "git@github.com:lightbend/paradox.git")
    ),
    taggedVersion      := "0.2.1",
    githubBaseUrlEntry := "github.base_url" -> "https://github.com/lightbend/paradox/tree/v0.2.1"
  )
