package org.yanex.flake.internal

inline fun <T> List<T>.forEachByIndex(f: (T) -> Unit) {
    val lastIndex = size() - 1
    for (i in 0..lastIndex) {
        f(get(i))
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> MutableList<T>.removeLast(): T? {
    return if (!isEmpty()) remove(size() - 1) else null
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> T.oneIfNotNull(): Int = if (this != null) 1 else 0