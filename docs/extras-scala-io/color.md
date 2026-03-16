---
sidebar_position: 2
id: 'color'
title: 'Color'
---

## Module
```scala
"io.kevinlee" %% "extras-scala-io" % "@VERSION@"
```
or for `Scala.js`:
```scala
"io.kevinlee" %%% "extras-scala-io" % "@VERSION@"
```


# Color

## `extras.scala.io.syntax`

`extras-scala-io` provides `syntax` to use `scala.io.AnsiColor` easily (The missing ones will be added later).

```scala mdoc:reset-object
import extras.scala.io.syntax.color._
```

```scala mdoc:reset-object
import extras.scala.io.syntax.color._

"Hello".blue

"Hello".red

"Hello".green

"Hello".bold

"Hello".underlined

"Hello".dim

println("Hello".blue)

println("Hello".red)

println("Hello".green)

println("Hello".bold)

println("Hello".underlined)

```

![AnsiColor syntax support Example 1](/img/docs/extras-scala-io/extras-scala-io-color-examples.png)
![AnsiColor syntax support Example 2](/img/docs/extras-scala-io/extras-scala-io-color-examples-2.png)


```scala mdoc
println("Hello".dim)
```
![Dim ANSI Color Example 1](/img/docs/extras-scala-io/dim-01-resized.png)
![Dim ANSI Color Example 2](/img/docs/extras-scala-io/dim-02-resized.png)
![Dim ANSI Color Example 3](/img/docs/extras-scala-io/dim-03-resized.png)

You can also chain them like this.
```scala mdoc:reset-object
import extras.scala.io.syntax.color._

println("Hello".blue)

println("Hello".blue.bold)

println("Hello".blue.bold.underlined)

println("Hello".underlined.bold.blue)

println("Hello".blue.dim)

println("Hello".blue.bold.dim)

```
![AnsiColor syntax support Example 3](/img/docs/extras-scala-io/extras-scala-io-color-examples-3-resized.png)

![Dim ANSI Color Example 4](/img/docs/extras-scala-io/dim-04-resized.png)

:::info New Feature
The `dim` color option was added in v0.51.0. It can be used alone or chained with other colors and styles.
:::
