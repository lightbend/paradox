lazy val root = (project in file("."))
  .enablePlugins(ParadoxPlugin)

lazy val additionalInternalMappings = settingKey[List[(File, String)]]("Additional mappings to add to paradox mappings")
lazy val additionalMappings         = settingKey[List[(File, String)]]("Additional mappings to add to paradox mappings")

additionalInternalMappings := Nil
additionalMappings         := Nil
(Compile / paradox / mappings) ++= Def.task {
  val conv = fileConverter.value
  sbtcompat.PluginCompat.toFileRefsMapping(additionalInternalMappings.value)(using conv)
}.value
(Compile / paradoxValidateInternalLinks / mappings) ++= Def.task {
  val conv = fileConverter.value
  sbtcompat.PluginCompat.toFileRefsMapping(additionalMappings.value)(using conv)
}.value
