CSS Friendliness
----------------

Regular markdown has no syntax for attaching identifiers or CSS classes to document tree elements.
While this keeps a document output-format-agnostic in principle, it can also make the CSS styling of the generated HTML
difficult and brittle.

Paradox comes with a "wrapping directive" which addresses this problem by allowing you to introduce additional
`div` elements, with custom `id` or `class` attributes, at arbitrary points in the document structure.

### @@@div

Wrapping content with `@@@ div`, e.g. like this:

```markdown
@@@ div { #foo .bar .baz }

Inner **markdown** content.

@@@
```

will render as:

```html
<div id="foo" class="bar baz">
  <p>Inner <strong>markdown</strong> content.</p>    
</div>
```

You can even nest blocks by using more `@` characters like this:

```
@@@ div { #foo .bar .baz }

Inner **markdown** content.

@@@@ warning

With an embedded warning

@@@@

@@@
```


### @span

Wrapping content with `@span[...]`, e.g. like this:

```markdown
This is a @span[Scala variant containing ***markdown*** and @ref:[Linking](test.md)] { .group-scala } to show.
```

will render as:

```html
<p>This is a <span class="group-scala">Scala variant containing <strong><em>markdown</em></strong> and <a href="test.html">Linking</a></span> to show.</p>


### Raw text in fenced blocks

In case you need to go wild and want to add text as-is to the page, the `raw` fenced block is available. Whereas other fenced block are html-ified, this is left untouched.

@@snip [snip](/tests/src/test/scala/com/lightbend/paradox/markdown/WrapDirectiveSpec.scala) { #raw }

will render as:

```html
<blink>Hello?</blink>
```

In many cases that section should be wrapped in a `div` with an appropriate class

@@snip [snip](/tests/src/test/scala/com/lightbend/paradox/markdown/WrapDirectiveSpec.scala) { #div-raw }

will render as:

```html
<div class="divStyleClass">
<blink>Hello?</blink>
</div>
```
