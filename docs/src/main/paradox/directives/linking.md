Linking
-------

#### External links

External links can be created with the default markdown syntax `[text](url)`. Additionally Paradox introduces `@link:` which accepts the `open=new` attribute to make the link open in a new browser tab (it adds `target="_blank" rel="noopener noreferrer"` to the anchor tag), and the `title` attribute to be used on the `a` tag.

```
See the @link:[Paradox GitHub repo](https://github.com/lightbend/paradox) { open=new } for more information.
```

#### @ref link

Paradox extensions are designed so the resulting Markdown is Github friendly.
For example, you might want to link from one document to the other, let's say from `index.md` to `setup/index.md`.

```
See @ref:[Setup](setup/index.md) for more information.
```

This will render to be `setup/index.html` in the HTML, but the source on Github will link correct as well!

### Parameterized links

Parameterized link directives help to manage links that references
external documentation, such as API documentation or source code. The
directives are configured via base URLs defined in `paradoxProperties`:

```sbt
paradoxProperties in Compile ++= Map(
  "github.base_url" -> s"https://github.com/lightbend/paradox/tree/${version.value}",
  "scaladoc.akka.base_url" -> s"http://doc.akka.io/api/${Dependencies.akkaVersion}",
  "extref.rfc.base_url" -> "http://tools.ietf.org/html/rfc%s"
)
```

After which the directives can be used as follows:

```
The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL
NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED",  "MAY", and
"OPTIONAL" in this document are to be interpreted as
described in @extref[RFC 2119](rfc:2119).

...

Use a @scaladoc[Future](scala.concurrent.Future) to avoid that long
running operations block the @scaladoc[Actor](akka.actor.Actor).

...

Issue @github[#1](#1) was fixed in commit @github[83986f9](83986f9).
See @github[example](/src/test/scala/ServerSetup.scala) { #ssl-setup } for more info.
```

*NOTE*: Only use these directives if standard Markdown and `@ref` does
not work, since GitHub won't preview them correctly.

#### @scaladoc directive

Use the `@scaladoc` directives to link to Scaladoc sites based on the package
prefix. Scaladoc URL mappings can be configured via the properties
`scaladoc.<package-prefix>.base_url` and the default `scaladoc.base_url`.
The directive will match the link text with the longest common package prefix
and use the default base URL as a fall-back if nothing else matches.

For example, given:

 - `scaladoc.akka.base_url=http://doc.akka.io/api/akka/2.4.10`
 - `scaladoc.akka.http.base_url=http://doc.akka.io/api/akka-http/10.0.0`

Then `@scaladoc[Http](akka.http.scaladsl.Http$)` will resolve to
<http://doc.akka.io/api/akka-http/10.0.0/akka/http/scaladsl/Http$.html>. To link
to a package page append `.index` for Scala 2.12 and `.package` for Scaladoc
generated with versions earlier than 2.12, for example `@scaladoc[scaladsl
package](akka.http.scaladsl.index)` will resolve to
<http://doc.akka.io/api/akka-http/10.0.0/akka/http/scaladsl/index.html>.

By default, `scaladoc.scala.base_url` is configured to the Scaladoc
associated with the configured `scalaVersion`. If the sbt project's
`apiURL` setting is configured, it is used as the default Scaladoc base
URL.

The `@scaladoc` directive also supports site root relative base URLs using the `.../` syntax.

The directive will identify inner classes and resolve a reference like
`@scaladoc[Consumer.Control](akka.kafka.scaladsl.Consumer.Control)` to
<https://doc.akka.io/api/alpakka-kafka/current/akka/kafka/scaladsl/Consumer$$Control.html>. 
This is working fine as long as all (sub)package names are starting with a lowercase
character while class names start with an uppercase character -- which is most often
the case.

In a situation where a (sub)package name starts with an uppercase character the
reference is resolved incorrectly. This can be fixed by configuring the properties
`scaladoc.<package-prefix>.package_name_style` or the default 
`scaladoc.package_name_style` and set it to `startWithAnycase`.
The directive will match the link text with the longest common package prefix
and use the default style as a fall-back if nothing else matches. Keep in mind
that the `OuterClass.InnerClass` notation is no longer working then and has
to be replaced by `OuterClass$$InnerClass`.

For example, given:

```sbt
paradoxProperties in Compile ++= Map(
  //...
  "scaladoc.com.example.package_name_style" -> s"startWithAnycase"
)
```

```markdown
 @scaladoc[SomeClass](com.example.Some.Library.SomeClass)
 @scaladoc[Outer.Inner](com.example.Some.Library.Outer$$Inner)
 @scaladoc[Consumer.Control](akka.kafka.scaladsl.Consumer.Control)
```

Then all are being resolved to the correct URL.

@@@ Note

The [sbt-paradox-apidoc](https://github.com/lightbend/sbt-paradox-apidoc) plugin creates `@scaladoc` and `@javadoc` API links by searching the class paths for the appropriate class to link to.

@@@


#### @javadoc directive

Use the `@javadoc` directives to link to Javadoc sites based on the package
prefix. Javadoc URL mappings can be configured via the properties
`javadoc.<package-prefix>.base_url` and the default `javadoc.base_url`.
The directive will match the link text with the longest common package prefix
and use the default base URL as a fall-back if nothing else matches.

For example, given:

```
javadoc.akka.base_url=http://doc.akka.io/japi/akka/2.4.10
javadoc.akka.link_style=frames
javadoc.akka.http.base_url=http://doc.akka.io/japi/akka-http/10.0.0
javadoc.akka.http.link_style=frames
```

Then `@javadoc[Http](akka.http.javadsl.Http#shutdownAllConnectionPools--)` will resolve to
<http://doc.akka.io/japi/akka-http/10.0.0/?akka/http/javadsl/Http.html#shutdownAllConnectionPools-->.

Since Java 9 the API documentation from Javadoc doesn't use frames anymore. Links to classes
need to include the class as part of the URI. To choose the link style for the Javadoc-generated
API to link to, set the `javadoc.<package>.link_style` to `direct`.

The root default, `javadoc.base_url` is configured to be `frames` (Javadoc < 9 style).

By default, `javadoc.java.base_url` is configured to the Javadoc
associated with the `java.specification.version` system property.

The `@javadoc` directive also supports site root relative base URLs using the `.../` syntax.

The directive will identify inner classes and resolve a reference like
`@javadoc[Flow.Subscriber](java.util.concurrent.Flow.Subscriber)` to
<https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/Flow.Subscriber.html>. 
This is working fine as long as all (sub)package names are starting with a lowercase
character while class names start with an uppercase character -- which is most often
the case.

In a situation where a (sub)package name starts with an uppercase character the
reference is resolved incorrectly. This can be fixed by configuring the properties
`javadoc.<package-prefix>.package_name_style` or the default 
`javadoc.package_name_style` and set it to `startWithAnycase`.
The directive will match the link text with the longest common package prefix
and use the default style as a fall-back if nothing else matches. Keep in mind
that the `OuterClass.InnerClass` notation is no longer working then. In this case
the class has to be referenced as `OuterClass$$InnerClass` which is being resolved
back to the `.`-notation.

For example, given:

```sbt
paradoxProperties in Compile ++= Map(
  //...
  "javadoc.com.example.package_name_style" -> s"startWithAnycase"
)
```

```markdown
 @javadoc[SomeClass](com.example.Some.Library.SomeClass)
 @javadoc[Outer.Inner](com.example.Some.Library.Outer$$Inner)
 @javadoc[outer.Inner](com.example.Some.Library.outer$$Inner)
 @javadoc[Outer.inner](com.example.Some.Library.Outer$$inner)
 @javadoc[Consumer.Control](akka.kafka.scaladsl.Consumer.Control)
```

Then all are being resolved to the correct URL.

@@@ Note

The [sbt-paradox-apidoc](https://github.com/lightbend/sbt-paradox-apidoc) plugin creates `@scaladoc` and `@javadoc` API links by searching the class paths for the appropriate class to link to.

@@@

#### @github directive

Use the `@github` directive to link to GitHub issues, commits and files.
It supports most of [GitHub's autolinking syntax][github-autolinking].

The `github.base_url` property must be configured to use shorthands such
as `#1`. To link to a directory or file in a specific version, set the base URL to
a tree revision, for example:
<https://github.com/lightbend/paradox/tree/v0.2.1>.

If the sbt project's `scmInfo` setting is configured and the `browseUrl`
points to a GitHub project, it is used as the GitHub base URL. Note that this
behaviour only occurs if `github.base_url` is not set so you still have the
option to define `github.base_url` if this is not desirable.

Relative as well as absolute file and directory paths use the value of the
`github.root.base_dir` property to resolve the absolute project path. The sbt
plugin automatically sets this property to the absolute path of the root project.

Links to a specific line or line range in a file can use snippet labels to ensure
that line numbers are up-to-date. Example:

```markdown
@github[../../resources/build.sbt] { #setup_example }
```

[github-autolinking]: https://help.github.com/articles/autolinked-references-and-urls/

#### @extref directive

Use the `@extref` directive to link to pages using custom URL templates.
URL templates can be configured via `extref.<scheme>.base_url` and the
template may contain one `%s` which is replaced with the scheme specific
part of the link URL. For example, given the property:

```text
extref.rfc.base_url=http://tools.ietf.org/html/rfc%s
```

then `@extref[RFC 2119](rfc:2119)` will resolve to the URL
<http://tools.ietf.org/html/rfc2119>.

The `@extref` directive also supports site root relative base URLs using the `.../` syntax.

#### image.base_url

When placing images via the standard markdown image syntax you can refer
to a configured base URL by starting the image href with `.../`, e.g. like this:

```
![logo](.../logo.png)
```

The `...` prefix refers to a defined a `image.base_url` property that is
specified either in the page's front matter or globally like this (for example):

```sbt
paradoxProperties in Compile ++= Map("image.base_url" -> ".../assets/images")
```

If the image base URL itself starts with three dots (`...`) then these in turn
refer to the root URL of the site.

**Note**: Using this feature will not allow GitHub to preview the images correctly on the web.
