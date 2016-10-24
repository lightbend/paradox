  [st]: http://www.stringtemplate.org/

Templating
----------

Paradox uses [StringTemplate][st] for the basic templating. For example:

```
$page.title$
```

is substituted with the title of the page.

### Properties front matter

Paradox allows to specify some properties at page level using `---` delimiters at the top of the page.

#### out

The `out` property allows to rename the target name of the current file.

```scala
---
out: newIndex.html
---
/*
 * Content of the page
 */
```

where `newIndex.html` will be the new name of the generated file. Links leading to this page are automatically updated.

#### layout

The `layout` property allows to specify the layout we want to be used for this particular page. The layouts are placed by default in the `target/paradox/theme` folder, but you can create one in `src/main/paradox/_template` folder as a string template file (.st).

```scala
---
layout: templateName
---
/*
 * Content of the page
 */
```

where `templateName` is the name of a template, more precisely the `templateName.st` file, which could either be a predefined template, or a created one in the `src/main/paradox/_template` folder.

#### `$` delimiters

Inside the templates (".st" files), it is possible to specify the properties of the page passed to the template by using `$` delimiters. For the following example file:

```scala
---
foo: bar
test: testValue
---
```

It is possible to use those properties in the template files by writing `$foo$` and `$test$`, which is similar to `$page.properties.("foo")$` and `$page.properties.("test")$` respectively.
