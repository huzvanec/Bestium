package cz.jeme.bestium.util

import cz.jeme.bestium.api.Bestium

@Suppress("UNCHECKED_CAST")
private val instances by lazy {
    Bestium.InstanceHolder::class.java.getDeclaredField("INSTANCES")
        .apply { isAccessible = true }[null] as MutableMap<String, Any>
}

fun storeApiInstance(instance: Any) {
    instances[instance.javaClass.name] = instance
}