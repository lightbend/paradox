Library dependencies
--------------------

The `@@dependency` block is used to show example code for how to configure a
dependency on a library in a build tool, such as sbt.

```markdown
@@dependency[sbt,Maven,Gradle] {
  group="com.typesafe.akka"
  artifact="akka-http_2.12"
  version="10.0.10"
}
```

Which will render as:

@@dependency[sbt,Maven,Gradle] {
  group="com.typesafe.akka"
  artifact="akka-http_2.12"
  version="10.0.10"
}

The build tools for which to show a configuration snippet can be configured in the directive content. Recognized build tools are: sbt, Maven and Gradle. Each build tool snippet will be shown in a separate tab.

The library coordinates are defined via the `group`, `artifact` and `version` attributes. Optionally, `scope` and `classifier` attributes can also be defined if needed. Variables may be used inside attributes, for example `$project.version$`.

```scala
@@dependency[sbt,Maven,Gradle] {
  group="com.example"
  artifact="domain"
  version="0.1.0"
  scope="runtime"
  classifier="assets"
}
```

Which will render as:

@@dependency[sbt,Maven,Gradle] {
  group="com.example"
  artifact="domain"
  version="0.1.0"
  scope="runtime"
  classifier="assets"
}

## Multiple dependencies

It is also possible to render more than one dependency in a list. In that case library coordinates need to be appended with the
same suffix. For example

```scala
@@dependency[sbt,Maven,Gradle] {
  group="com.example" artifact="domain" version="0.1.0"
  group2="com.example" artifact2="another-domain" version2="0.2.1"
}
```

will be rendered as:

@@dependency[sbt,Maven,Gradle] {
  group="com.example" artifact="domain" version="0.1.0"
  group2="com.example" artifact2="another-domain" version2="0.2.1"
}

## Symbolic version numbers

When multiple dependencies always use the same version, symbolic version names can be shown. For example

```scala
@@dependency[sbt,Maven,gradle] {
  symbol="AkkaVersion"
  value="2.5.29"
  symbol2="AkkaHttpVersion"
  value2="10.1.0"
  group="com.typesafe.akka"
  artifact="akka-stream_$scala.binary.version$"
  version="AkkaVersion"
  group2="com.typesafe.akka"
  artifact2="akka-actor-typed_$scala.binary.version$"
  version2="AkkaVersion"
  group3="com.typesafe.akka"
  artifact3="akka-http_$scala.binary.version$"
  version3="AkkaHttpVersion"
}
```

will be rendered as:

@@dependency[sbt,Maven,gradle] {
  symbol="AkkaVersion"
  value="2.5.29"
  symbol2="AkkaHttpVersion"
  value2="10.1.0"
  group="com.typesafe.akka"
  artifact="akka-stream_$scala.binary.version$"
  version="AkkaVersion"
  group2="com.typesafe.akka"
  artifact2="akka-actor-typed_$scala.binary.version$"
  version2="AkkaVersion"
  group3="com.typesafe.akka"
  artifact3="akka-http_$scala.binary.version$"
  version3="AkkaHttpVersion"
}
