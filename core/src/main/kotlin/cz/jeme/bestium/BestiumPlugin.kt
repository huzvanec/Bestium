package cz.jeme.bestium

import cz.jeme.bestium.util.InstanceUtils
import org.bukkit.plugin.java.JavaPlugin


internal object BestiumPlugin : JavaPlugin() {
    init {
        InstanceUtils.storeApiInstance(this)
        InstanceUtils.storeApiInstance(PluginSupportImpl)
    }

    override fun onEnable() {
        val start = System.currentTimeMillis()
        saveDefaultConfig()

        EntityInjectorImpl.injectBukkit() // phase 2 injection
        if (PluginSupportImpl.betterModel()) {
            logger.info("BetterModel detected, updating models...")
            EntityInjectorImpl.copyModels()
        }
        logger.info("Bestium enabled successfully (took ${System.currentTimeMillis() - start}ms)")
    }
}
