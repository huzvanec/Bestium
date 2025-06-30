package cz.jeme.bestium.util

import cz.jeme.bestium.api.Bestium

object InstanceUtils {
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    private val instances by lazy {
        Bestium.InstanceHolder::class.java.getDeclaredField("INSTANCES")
            .apply { isAccessible = true }
            .get(null) as MutableMap<String, Any>
    }

    @JvmStatic
    fun storeApiInstance(instance: Any) {
        instances[instance.javaClass.name] = instance
    }
}