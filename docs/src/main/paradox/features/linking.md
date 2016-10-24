Linking
-------

### @ref link

Paradox extensions are designed so the resulting Markdown is Github friendly.
For example, you might want to link from one document to the other, let's say from `index.md` to `setup/index.md`.

```
See @ref:[Setup](setup/index.md) for more information.
```

This will render to be `setup/index.html` in the HTML, but the source on Github will link correct as well!

## Parameterized links

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
```

*NOTE*: Only use these directives if standard Markdown and `@ref` does
not work, since GitHub won't preview them correctly.

### @scaladoc directive

Use the `@scaladoc` directives to link to Scaladoc sites based on the package
prefix. Scaladoc URL mappings can be configured via the properties
`scaladoc.<package-prefix>.base_url` and the default `scaladoc.base_url`.
The directive will match the link text with the longest common package prefix and use the default base URL as a fall-back if nothing else matches.

For example, given:

 - `scaladoc.akka.base_url=http://doc.akka.io/api/akka/2.4.10`
 - `scaladoc.akka.http.base_url=http://doc.akka.io/api/akka-http/10.0.0`

Then `@scaladoc[Http](akka.http.scaladsl.Http$)` will resolve to
<http://doc.akka.io/api/akka-http/10.0.0/#akka.http.scaladsl.Http$>.

By default, `scaladoc.scala.base_url` is configured to the Scaladoc
associated with the configured `scalaVersion`. If the sbt project's
`apiURL` setting is configured, it is used as the default Scaladoc base
URL.

### @github directive

Use the `@github` directive to link to GitHub issues, commits and files.
It supports most of [GitHub's autolinking syntax][github-autolinking].

The `github.base_url` property must be configured to use shorthands such
as `#1`. For source code links to a specific version set the base URL to
a tree revision, for example:
<https://github.com/lightbend/paradox/tree/v0.2.1>.

If the sbt project's `scmInfo` setting is configured and the `browseUrl`
points to a GitHub project, it is used as the GitHub base URL.

[github-autolinking]: https://help.github.com/articles/autolinked-references-and-urls/

### @extref directive

Use the `@extref` directive to link to pages using custom URL templates.
URL templates can be configured via `extref.<scheme>.base_url` and the
template may contain one `%s` which is replaced with the scheme specific
part of the link URL. For example, given the property:

    scaladoc.rfc.base_url=http://tools.ietf.org/html/rfc%s

then `@extref[RFC 2119](rfc:2119)` will resolve to the URL
<http://tools.ietf.org/html/rfc2119>.

### image.base_url

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
