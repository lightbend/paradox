CSS Friendliness
----------------

Regular markdown has no syntax for attaching identifiers or CSS classes to document tree elements.
While this keeps a document output-format-agnostic in principle, it can also make the CSS styling of the generated HTML
difficult and brittle.

*paradox* comes with two "wrapping directives" which address this problem by allow you to introduce additional
`div` or `p` container elements, with custom `id` or `class` attributes, at arbitrary points in the document structure.


### @@@ div

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

By omitting the extra blank lines you can prevent the addition of the extra `<p>..</p>` wrapper.
So this:
  
```markdown
@@@ div { #foo .bar .baz }
Inner **markdown** content.
@@@
```

will render as:

```html
<div id="foo" class="bar baz">
Inner <strong>markdown</strong> content.    
</div>
```


### @@@ p

The `@@@ p` directive works in exactly the same way as `@@@ div`, with the only difference that it generates a
`<p>...</p>` wrapper rather than a `<div>...</div>`.

For example this snippet:
  
```markdown
@@@ p { #foo .bar .baz }
Inner **markdown** content.
@@@
```

will render as:

```html
<p id="foo" class="bar baz">
Inner <strong>markdown</strong> content.    
</p>
```