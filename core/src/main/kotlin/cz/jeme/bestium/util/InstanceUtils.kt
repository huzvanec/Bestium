package cz.jeme.bestium.util

import cz.jeme.bestium.api.Bestium
import cz.jeme.bestium.config.logVerbose
import net.kyori.adventure.text.logger.slf4j.ComponentLogger

@Suppress("UNCHECKED_CAST")
private val instances by lazy {
    Bestium.InstanceHolder::class.java.getDeclaredField("INSTANCES")
        .apply { isAccessible = true }[null] as MutableMap<String, Any>
}

private val logger = ComponentLogger.logger("BestiumInstances")

fun storeApiInstance(instance: Any) {
    val cpath = instance.javaClass.name
    if (logVerbose) logger.info("Exposing: $cpath")
    instances[cpath] = instance
}