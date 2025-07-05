package cz.jeme.bestium

import cz.jeme.bestium.command.SummonCommand
import cz.jeme.bestium.util.InstanceUtils
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.key.KeyPattern
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin


internal object BestiumPlugin : JavaPlugin() {
    // override default logger
    private val logger = componentLogger

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

        logger.info("Registering commands")
        @Suppress("UnstableApiUsage")
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands = event.registrar()
            SummonCommand(this, commands)
        }

        logger.info("Bestium enabled successfully (took ${System.currentTimeMillis() - start}ms)")
    }

    fun key(@KeyPattern.Value key: String): NamespacedKey = NamespacedKey(this, key)
}
