package cz.jeme.bestium.core

import cz.jeme.bestium.api.Bestium
import org.bukkit.plugin.java.JavaPlugin


internal object BestiumImpl : JavaPlugin(), Bestium {
    init {
        Bestium.InstanceHolder::class.java.getDeclaredField("bestium")
            .apply { isAccessible = true }
            .set(null, this)
    }

    override fun onEnable() {
        val start = System.currentTimeMillis()
        saveDefaultConfig()

        injector().inject()
        logger.info("Bestium enabled successfully (took ${System.currentTimeMillis() - start}ms)")

        if (pluginSupport().betterModel()) {
            logger.info("BetterModel detected, updating models...")
            injector().copyModels()
        }
    }

    override fun injector() = EntityInjectorImpl

    override fun pluginSupport() = PluginSupportImpl
}
