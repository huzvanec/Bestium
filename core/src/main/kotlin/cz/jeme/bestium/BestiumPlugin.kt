package cz.jeme.bestium

import cz.jeme.bestium.api.Bestium
import cz.jeme.bestium.command.SpawnEggCommand
import cz.jeme.bestium.command.SummonCommand
import cz.jeme.bestium.util.storeApiInstance
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.key.KeyPattern
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin


internal object BestiumPlugin : JavaPlugin(), Bestium {
    // override default logger
    private val logger = componentLogger

    init {
        storeApiInstance(this)
        storeApiInstance(PluginSupportImpl)
        storeApiInstance(BestiumEntityManagerImpl)
    }

    override fun onEnable() {
        val start = System.currentTimeMillis()
        saveDefaultConfig()

        EntityInjectorImpl.injectBukkit() // phase 2 injection
        if (PluginSupportImpl.betterModel()) {
            logger.info("BetterModel detected, updating models...")
            EntityInjectorImpl.copyModels()
        }

        logger.info("Registering commands")
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands = event.registrar()
            SummonCommand(this, commands)
            SpawnEggCommand(this, commands)
        }

        // register events
        WorldLoadListener

        logger.info("Bestium enabled successfully (took ${System.currentTimeMillis() - start}ms)")
    }

    override fun createKey(@KeyPattern.Value key: String): NamespacedKey = NamespacedKey(this, key)
}
