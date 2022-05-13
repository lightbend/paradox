lazy val root = (project in file("."))
  .enablePlugins(ParadoxPlugin)

lazy val additionalInternalMappings = settingKey[List[(File, String)]]("Additional mappings to add to paradox mappings")
lazy val additionalMappings         = settingKey[List[(File, String)]]("Additional mappings to add to paradox mappings")

additionalInternalMappings := Nil
additionalMappings         := Nil
Compile / paradox / mappings ++= additionalInternalMappings.value
Compile / paradoxValidateInternalLinks / mappings ++= additionalMappings.value
