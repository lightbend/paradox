Theming
=======

Paradox uses "theming" to generate its target files. Combined with templates within it, themes can produce a global styling of the site generated.
A theme is constituted of `.st` template files, javascript files and css files. By default, a simple predefined theme in paradox is used and looks like [this](https://github.com/lightbend/paradox/tree/master/themes/generic/src/main/assets).

If you want to use a template `otherTemplate.st` inside an other template `myTemplate.st`, you need to specify it inside the `myTemplate.st`'s code:

```
/*
  some template code
 */

$otherTemplate()$

/*
  some other template code
 */
```

## How to use themes

Three possibilities are available to deal with themes:

- Create a theme as an external repository
- Create a theme from scratch locally
- Modify an existing theme

### External theme

The [Lightbend theme](https://github.com/typesafehub/paradox-theme-lightbend) is a good example of what should look like a paradox theme repository.

Add to the `project/plugins.sbt` file of your theme repository:

```scala
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox-theme" % "X.Y.Z")
```

and in your `build.sbt` file the information that will be used to generate your theme.

```scala
lazy val myTheme = (project in file("."))
  .enablePlugins(ParadoxThemePlugin)
  .settings(
    inThisBuild(List(
      organization := "com.organization.paradox"
    )),
    name := "paradox-theme-organization",
```

Finally, the theme created will be available inside your paradox project by setting the `paradoxTheme` like this:

```scala
  paradoxTheme := Some("com.organization.paradox" % "paradox-theme-organization" % "X.Y.Z")
```

Concerning the template/css/js files, they need to be placed inside the `src/main/assets` folder.
Remember that the default template used by paradox at generation will be the template named `page.st`, unless you specify an other one with the `out` property at page level; see the @ref[Templating](templating.md) section.

In order to use your template in your paradox projects, you need to publish it.

### Create a local theme

If you do not want to use any external theme, you can create your own theme directly inside the paradox project. To do this, you have to build your entire new system of templates from scratch.

The procedure is to indicate to paradox that you don't need to use any theme in your build.sbt file:

```scala
  paradoxTheme := None
```

All template files need to be created inside the `src/main/paradox/_template` folder of your project. You can specify an other one by setting its new location in the build:

```scala
sourceDirectory in Compile in paradoxTheme := sourceDirectory.value / "main" / "paradox" / "templatesConfig"
```

Remember that your theme need to contain a page template with the name `page.st` that will be used by default for all your pages. You can specify an other default template for your pages, either by specifying a layout at page level (see @ref[layout templating](templating.md#layout)), or by setting it at global scope in the build:

```scala
paradoxProperties += ("layout" -> "myDefault")
```

considering you have previously created a template with the name `myDefault.st`


### Modify a theme

An other way of dealing with themes is modifying or overwriting an existing one. In your project `build.sbt` file, when you set `paradoxTheme := Some(...)`, you can easily modify the theme by modifying the templates or by creating new ones (in the same way than the above section). All those modifications and/or creations must be done inside the `src/main/paradox/_template` folder, which will overwrite the files with the same name in the original template. If you want to modify/create new css or js files for your templates, you can do it inside `src/main/paradox/_template/css` and `src/main/paradox/_template/js` folders respectively.

Take a look at [generic template](https://github.com/lightbend/paradox/tree/master/themes/generic/src/main/assets) to see the templates you could use or modify for your personal templates.

An example of modification of this template would be to use some of its templates and modify for example the default `page.st` file to manipulate the other template files differently. If you don't want to modify the `page.st` file but use an other default page built by yourself, you can add the `layout` property as explained in the [previous section](#create-a-local-theme)