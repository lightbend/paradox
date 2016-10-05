## StringTemplate

Paradox uses [StringTemplate][st] for the basic templating. For example:

```
$page.title$
```

is substituted with the title of the page.

### Properties front matter

Paradox allows to specify some properties at page level using `---` delimiters at the top of the page.

The `out` property allows to rename the target name of the current file.
And the `layout` allows to specify the layout we want to be used for this particular page, the layouts are placed by default in the `target/paradox/theme` folder:

```scala
---
out: newIndex.html
layout: templateName
---
/*
 * Content of the page
 */
```

where `newIndex.html` will be the new name of the generated file and `templateName` is the name of a template, which corresponds more precisely to the file `templateName.st`.

#### `$` delimiters

Inside the templates (".st" files), it is possible to specify the properties of the page passed to the template by using `$` delimiters. For the following example file:

```scala
---
foo: bar
test: testValue
---
```

It is possible to use those properties in the template files by writing `$foo$` and `$test$`.