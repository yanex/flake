package org.yanex.flake

import android.content.res.Resources
import android.view.View
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class FlakeHolder(val root: View)

open class IdHolder(root: View) : FlakeHolder(root) {

    @Suppress("UNCHECKED_CAST")
    protected fun <V : View> id(id: Int): ReadOnlyProperty<IdHolder, V> = Lazy { property ->
        root.findViewById(id) as V? ?: viewNotFound(root, id)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <V : View> opt(id: Int): ReadOnlyProperty<IdHolder, V?> = Lazy { property ->
        root.findViewById(id) as V?
    }

    private fun viewNotFound(root: View, id: Int): Nothing {
        val textId: String

        try {
            textId = root.resources.getResourceName(id)
        } catch (e: Resources.NotFoundException) {
            textId = id.toString()
        }

        throw IllegalArgumentException("View with id = $textId ($id) was not found inside $root")
    }

    private class Lazy<T, V>(val initializer : (KProperty<*>) -> V) : ReadOnlyProperty<T, V> {
        private object UNINITIALIZED
        private var value: Any? = UNINITIALIZED

        override fun getValue(thisRef: T, property: KProperty<*>): V {
            if (value == UNINITIALIZED) {
                value = initializer(property)
            }

            @Suppress("UNCHECKED_CAST")
            return value as V
        }
    }
}