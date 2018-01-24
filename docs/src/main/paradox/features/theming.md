Theming
-------

A theme is a collection of templates and other resources like css and image files that can be distributed as a web jar. Using this mechanism, multiple Paradox sites can have consistent look and feel. By default, a simple predefined theme in paradox is used and looks like [this](https://github.com/lightbend/paradox/tree/master/themes/generic/src/main/assets).

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

### How to use themes

To use a theme you need to set `paradoxTheme` in your build.

- If you want to use the default theme of paradox, you can use it by setting `paradoxTheme := Some(builtinParadoxTheme("generic"))`. Take a look at this [default theme](https://github.com/lightbend/paradox/tree/master/themes/generic/src/main/assets) to get an idea of how it looks like.
- If you want to use an external theme, you can use it by setting `paradoxTheme := Some("organization" % "name" % "X.Y.Z")`. A good example of an external theme is the [Lightbend theme](https://github.com/typesafehub/paradox-theme-lightbend).


### How to create themes

If you don't want to use a predefined theme, or at least the entire theme, you can create one. You have three options:

- Create an external theme
- Create a local template system
- Modify a theme

#### External theme

External themes use the paradox theme plugin to package files into a JAR which
can be published to Maven Central or some other repository. The
[Akka theme](https://github.com/akka/akka-paradox) and
[Material Design theme](https://github.com/jonas/paradox-material-theme) are
good examples of what a paradox theme repository should look like.

Add to the `project/plugins.sbt` file of your theme repository:

@@@vars
```scala
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox-theme" % "$version$")
```
@@@

and in your `build.sbt` file the information that will be used to generate your theme.

```scala
lazy val myTheme = (project in file("."))
  .enablePlugins(ParadoxThemePlugin)
  .settings(
    inThisBuild(List(
      organization := "com.organization.paradox"
    )),
    name := "paradox-theme-organization"
  )
```

Finally, the theme created will be available inside your paradox project by setting the `paradoxTheme` like this:

```scala
  paradoxTheme := Some("com.organization.paradox" % "paradox-theme-organization" % "X.Y.Z")
```

Concerning the template/css/js files, they need to be placed inside the `src/main/assets` folder.
Remember that the default template used by paradox at generation will be the template named `page.st`, unless you specify an other one with the `layout` property; see the @ref[Templating](templating.md#layout) section.

In order to use your template in your paradox projects, you need to publish it.

#### Create local templates

If you do not want to use any external theme, you can create your own theme directly inside the paradox project. To do this, you have to build your entire new system of templates from scratch.

The procedure is to indicate to paradox that you don't need to use any theme in your build.sbt file:

```scala
  paradoxTheme := None
```

All template files need to be created inside the `src/main/paradox/_template` folder of your project. You can specify an other one by setting its new location in the build:
```scala
sourceDirectory in Compile in paradoxTheme := sourceDirectory.value / "main" / "paradox" / "templatesConfig"
```

Remember that your theme need to contain a page template with the name `page.st` that will be used by default for all your pages. You can specify an other default template for your pages, by specifying a new `layout`; see @ref[layout templating](templating.md#layout):

#### Modify a theme

An other way of dealing with themes is modifying or overwriting an existing one. In your project `build.sbt` file, when you set `paradoxTheme := Some(...)`, you can easily change the theme by modifying the templates or by creating new ones (in the same way than the above section). All those modifications and/or creations must be done inside the `src/main/paradox/_template` folder, which will overwrite the files with the same name in the original template. If you want to modify/create new css or js files for your templates, you can do it inside `src/main/paradox/_template/css` and `src/main/paradox/_template/js` folders respectively.

As in the previous section, you can also change your template folder in the build:
```scala
sourceDirectory in Compile in paradoxTheme := sourceDirectory.value / "main" / "paradox" / "templatesConfig"
```

If you use the default paradox theme for example, take a look at the [generic theme](https://github.com/lightbend/paradox/tree/master/themes/generic/src/main/assets) to see the templates you could use or modify for your personal templates.

An example of modification of this template would be to use some of its templates and modify for example the default `page.st` file to manipulate the other template files differently. Once again, if you don't want to modify the `page.st` file but use an other default page built by yourself, you can add the `layout` property as explained in the @ref[layout section](templating.md#layout)
