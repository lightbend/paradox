Markdown file inclusion
-----------------------

### @@include block

The `@@include` block is used to include full or partial markdown files rendered into this file.

```markdown
@@include[my-file.md](includes/my-file.md)
```

This will load and render `includes/my-file.md` at the location of the include.

To include partial files, snippet identifiers can be used:

```markdown
@@include[my-file.md](includes/my-file.md) { #my-section }
```

Then, inside `includes/my-file.md`, mark the section using the `#my-section` inside comments as follows:

```markdown
<!--- #my-section --->
Only this part of the markdown file will be included.
<!--- #my-section --->
```

Note that the file name label optional and not used, however it is recommended that you include it to ensure that
other markdown renderers that don't support the include directive, such as GitHub, will render the include as a 
link to the included file.

### `include.*.base_dir`

In order to specify your include source paths off certain base directories you can define placeholders
either in the page's front matter or globally like this (for example):

```sbt
paradoxProperties in Compile ++= Map(
  "include.test.base_dir" -> s"${(sourceDirectory in Test).value}/paradox"
)
```

You can then refer to one of the defined base directories by starting the include's target path with `$placeholder$`,
for example:

```markdown
@@include[test-docs.md]($test$/test-docs.md)
```

If a placeholder directory is relative it'll be based of the path of the respective page it is used in.
Also, *paradox* always auto-defines the placeholder `$root$` to denote the absolute path of the sbt (sub)project's 
root directory.

**Note**: Using this feature will not allow GitHub to follow the include links correctly on the web UI.

