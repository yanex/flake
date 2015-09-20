# :snowflake: Flake

**Flake** is a lightweight `Fragment` alternative.  
The library is written in [Kotlin](http://kotlinlang.org/), and it can be used both with Kotlin and Java.  
No reflection is involved.

It goes well with XML layouts. It goes well with [Anko](https://github.com/JetBrains/anko). It is awesome!

<table>
<tr><td width="50px" align="center">:construction:</td>
<td>
<i>This document is in construction. You would probably better to take a look at an <a href="https://github.com/yanex/flake/tree/master/example/src/main/java/org/yanex/flaketest">example project</a>.
</td>
</tr>
</table>

## A small example

A *flake* is a simple class that extends `Flake`. It is quite similar to the `Fragment` class in Android, except that it does not hold any UI state by itself and it does not being re-created auto:tophat:ally.

`SimpleFlake` inflates the `R.layout.flake_simple` layout and sets a listener to the `button`.

```kotlin
public class SimpleFlake : XmlFlake<SimpleFlake.Holder>() {
    override val layoutResource = R.layout.flake_simple
    override fun createHolder(root: View) = Holder(root)

    override fun setup(h: Holder, manager: FlakeManager) {
        h.button.setOnClickListener { /* some code! */ }
    }
    
    private class Holder(root: View) : IdHolder(root) {
        public val button: Button by id(R.id.button)
    }
}
```

## Why not `Fragment`?

Android fragments are said to be over-complicated. If you do not think so, you probably should read this amazing article by Square: [Advocating Against Android Fragments](https://corner.squareup.com/2014/10/advocating-against-android-fragments.html). In short, fragments have lots of state transitions, and sometimes it is really hard to figure out what is going wrong.

## Project status

**Flake** is in its early stages right now, but all the basic functionality seems to work.

Features currently supported:
- Flake back stack;
- Transition animations;
- Nested flakes;
- Handling configuration changes.

## TODO

- [ ] Replace animations
- [ ] Unit tests
