# Variable Substitution Directives

## @var

Inserts the given variable into the text.

Example:

```
The Scala Version is @var[scala.version].
```

results in:

The Scala Version is @var[scala.version].

## @@@vars

Allows to insert property values in verbatim blocks.

For example,

```
 @@@vars

 ```scala
 println("The project version is $version$")
 ```

 @@@
```

(added extra indentation to be able to show the example)

would render to:

@@@vars

```scala
println("The project version is $version$")
```

@@@

### Customize delimiters with @@@vars

You can customize the delimiters if `$` already has a special meaning inside the verbatim block:

```
 @@@vars { start-delimiter="*&*" stop-delimiter="&*&" }

 ```scala
 println("The project version is *&*version&*&")
 ```

@@@
```

renders to:

@@@vars { start-delimiter="*&*" stop-delimiter="&*&" }

```scala
println("The project version is *&*version&*&")
```

@@@