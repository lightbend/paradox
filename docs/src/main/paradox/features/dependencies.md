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
