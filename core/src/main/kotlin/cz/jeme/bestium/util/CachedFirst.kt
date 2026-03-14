package cz.jeme.bestium.util

import kotlin.reflect.KProperty

open class CachedFirst<T>(
    private val init: () -> T,
    private val keepValue: (T) -> Boolean
) {
    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value == null) {
            val got = init()
            if (!keepValue(got)) return got
            value = got
        }
        return value!!
    }

    fun reset() {
        value = null
    }

    class Value<T>(init: () -> T) : CachedFirst<T>(init, { true })
    class NotNull<T>(init: () -> T) : CachedFirst<T>(init, { it != null })
    class True(init: () -> Boolean) : CachedFirst<Boolean>(init, { it })
    class False(init: () -> Boolean) : CachedFirst<Boolean>(init, { !it })
}
