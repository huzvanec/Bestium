package cz.jeme.bestium.config

import kotlin.reflect.KProperty

class ConfigEntry<T>(private val initialize: () -> T) {
    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value == null) {
            value = initialize()
        }
        return value!!
    }

    fun markDirty() {
        value = null
    }
}