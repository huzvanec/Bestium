package cz.jeme.bestium.util

import cz.jeme.bestium.BestiumBootstrapper
import org.bukkit.Bukkit

val NOVA_LOADED by lazy {
    // accessible way earlier than PluginSupport#isNovaLoaded
    try {
        // works, because plugin has joined classpath in paper-plugin.yml
        Class.forName("xyz.xenondevs.nova.Nova", false, BestiumBootstrapper::class.java.classLoader)
        return@lazy true
    } catch (_: ClassNotFoundException) {
        return@lazy false
    }
}

val PAPER_CLASS_LOADER: ClassLoader = Bukkit::class.java.classLoader
val BESTIUM_CLASS_LOADER: ClassLoader = BestiumBootstrapper::class.java.classLoader

val PLATFORM_CLASS_LOADER: ClassLoader = PAPER_CLASS_LOADER.let {
    if (NOVA_LOADED) it.parent.parent else it.parent
}