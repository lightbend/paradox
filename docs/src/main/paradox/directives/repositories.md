Library repositories
--------------------

The `@@repository` block is used to show example code for how to configure a
library repository in a build tool, such as sbt.

```markdown
@@dependency[sbt,Maven,Gradle] {
  id="id1"
  name="Company repository"
  url="http://jars.acme.com"
}
```

Which will render as:

@@repository[sbt,Maven,Gradle] {
  id="id1"
  name="Company repository"
  url="http://jars.acme.com"
}
