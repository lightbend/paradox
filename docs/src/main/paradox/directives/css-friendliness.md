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
