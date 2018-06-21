Version warning
---------------

Paradox supports showing a warning when users browse documentation which is not the current released version. This is achieved by storing a JSON file (`paradox.json`) together with the generated site and specifying a stable URL to where the released version will be available.

The built in theme (`generic`) contains Javascript to fetch the JSON file and compare the version with the version for which the documentation showing was generated. Whenever they differ, a warning text shows on every page offering a link to the released version's page.

To use this functionality, add `project.url` to Paradox properties

```scala
paradoxProperties += ("project.url" -> "https://developer.lightbend.com/docs/paradox/current/")
```
