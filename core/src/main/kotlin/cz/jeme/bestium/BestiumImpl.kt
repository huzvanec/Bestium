package cz.jeme.bestium

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

        val injected = injector().inject()
        if (injected && pluginSupport().betterModel()) {
            logger.info("BetterModel detected, updating models...")
            injector().copyModels()
        }
        logger.info("Bestium enabled successfully (took ${System.currentTimeMillis() - start}ms)")
    }

    override fun injector() = EntityInjectorImpl

    override fun pluginSupport() = PluginSupportImpl
}
