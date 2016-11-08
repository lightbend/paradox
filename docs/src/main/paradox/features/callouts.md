Callouts
--------

Use callouts to discuss and clarify specific topics. In the resulting output,
the content inside the callout will appear in a separate block.

Paradox provides callouts for notes and warnings which can further be
customized to fit specific needs.

### @@@note callout

Notes are written as:

```
@@@ note

For the connection pooled client side the idle period is counted only when the
pool has no pending requests waiting.

@@@
```

will render as:

@@@ note

For the connection pooled client side the idle period is counted only when the
pool has no pending requests waiting.

@@@

The note title can be customized using the `title` attribute. For example:

```
@@@ note { title=Hint }
...
@@@
```

will render as:

@@@ note { title=Hint }
...
@@@

### @@@warning callout

Warnings are written as:

```
@@@ warning
Make sure to use basic authentication only over SSL/TLS because credentials are transferred in plaintext.
@@@
```

and will render as:

@@@ warning
Make sure to use basic authentication only over SSL/TLS because credentials are transferred in plaintext.
@@@

The warning title can be customized using the `title` attribute. For example:

```
@@@ warning { title='Caveat Emptor' }
...
@@@
```

will render as:

@@@ warning { title='Caveat Emptor' }
...
@@@
