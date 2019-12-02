# Validation

Paradox will validate many directives at compile time, such as ref links and their fragments, and snippets. However, some validation can't be done (or is best not done) at compile time, such as validating external links, or non ref links to other paths in the documentation. Paradox provides two tasks to do this validation.

@@@ note

Paradox fails for regular markdown links which seem to link to a local markdown file (`.md`), as the `@ref` directive is often left out. This check is controlled by the regex in `paradoxIllegalLinkPath`.

@@@

## Validating internal links

The `paradoxValidateInternalLinks` task can validate internal links that are not validated at compile time. An internal link is any non `@ref` link that does not specify an authority part of its URI (ie, relative links).

Paradox uses `mappings in paradoxValidateInternalLinks` to validate these links, links to any paths that are not in the mappings are flagged as errors. `mappings in paradoxValidateInternalLinks` defaults to `mappings in paradox`, this means that any non markdown assets in the Paradox source root (such as images) will be included, along with the output of Paradox compilation itself (that is, all the HTML files). Additional files for validation can be added to either setting, the primary difference is that `mappings in paradox` is synced by Paradox to the Paradox `site` target directory when `paradox` is run, whereas `mappings in paradoxValidateInternalLinks` is not. Hence, if you are deploying those assets through a different means than taking the output of the Paradox site directory, then they should be added to the latter mappings.

### Links outside of the Paradox directory tree

If you wish to validate internal links that are outside of the Paradox directory tree, for example, if your paradox root gets deployed to a directory called `/docs`, but you want to include links to `/apidocs` in the validation, this can be done by specifying `paradoxValidationSiteBasePath`, like so:

```scala
paradoxValidationSiteBasePath := Some("/docs")
```

By default, internal links that are outside of the Paradox site directory tree are ignored, setting this setting will cause them to be validated. This will also cause all `mappings in paradoxValidateInternalLinks` that come from `mappings in paradox` to have their paths rebased to the site base path. The additional assets can then be added to `mappings in paradoxValidateInternalLinks`, for example, to add all api docs to it:

```scala
mappings in (Compile, paradoxValidateInternalLinks) ++= {
  val apiDocs = (doc in Compile).value
  ((apiDocs ** "*") pair Path.relativeTo(apiDocs)).map {
    case (file, path) => file -> s"/apidocs/$path"
  }
}
```

## Validating external links

External links can be validated by running `paradoxValidateLinks`. This will validate both internal links and external links, and will check to ensure that all external links return a 200 status code. It is strongly recommended that you do not run `paradoxValidateLinks` as part of CI, since it will make your CI build dependent on the availability of every site that your documentation links to, and will likely cause PR validation failures completely unrelated to the code being validated. Rather, it can be run manually, periodically, as a convenience to check that there are no dead links.

## Validating fragments

If a link contains a fragment, Paradox will validate the fragment by searching for a string that looks like an id tag in the target. For example, the link `validation.html#validating-fragments` will be considered valid if `validation.html` contains the string `id="validating-fragments"`. This applies to both external and internal links.

## Ignoring links

Links can be ignored by validation by specifying regular expressions to match on paths to ignore in `paradoxValidationIgnorePaths`. This list defaults to `http://localhost.*`. For example:

```scala
paradoxValidationIgnorePaths ++= Seq(
  // Don't validate any links to example.com
  "https://example\\.com.*".r,
  // Ignore all links to versions of the docs other than the latest
  "/docs/version/(?!latest).*"
)
```
