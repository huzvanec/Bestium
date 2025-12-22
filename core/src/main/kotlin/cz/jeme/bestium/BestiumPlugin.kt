package cz.jeme.bestium

import cz.jeme.bestium.api.Bestium
import cz.jeme.bestium.command.BestiumCommand
import cz.jeme.bestium.command.SpawnEggCommand
import cz.jeme.bestium.command.SummonCommand
import cz.jeme.bestium.config.logNormal
import cz.jeme.bestium.config.logVerbose
import cz.jeme.bestium.inject.EntityInjectorImpl
import cz.jeme.bestium.util.storeApiInstance
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.KeyPattern
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin


internal object BestiumPlugin : JavaPlugin(), Bestium {
    // override default logger
    private val logger = componentLogger

    init {
        storeApiInstance(this)
        storeApiInstance(PluginSupportImpl)
    }

    override fun onEnable() {
        val start = System.currentTimeMillis()

        EntityInjectorImpl.injectLoad() // phase 2 injection

        if (logNormal) {
            val injected = EntityInjectorImpl.keyedInjections.keys.map(Key::asString)
            logger.info(
                if (injected.isEmpty()) "No entities found to inject"
                else "Injected ${injected.size} entit${if (injected.size == 1) "y" else "ies"}: ${injected.joinToString()}"
            )
        }

        if (PluginSupportImpl.isBetterModelLoaded) {
            if (logNormal) logger.info("BetterModel detected, updating models...")
            EntityInjectorImpl.copyModels()
        }

        if (logVerbose) logger.info("Registering commands")
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands = event.registrar()
            SummonCommand(this, commands)
            SpawnEggCommand(this, commands)
            BestiumCommand(this, commands)
        }

        if (logVerbose) logger.info("Registering event listeners")
        fun Listener.register() = Bukkit.getPluginManager().registerEvents(this, this@BestiumPlugin)

        EntityManagerImpl.register()

        if (logVerbose) logger.info("Registering entity translations")
        EntityInjectorImpl.injections.values.forEach(EntityTranslator::addInjection)
        EntityTranslator.register()

        if (logNormal) logger.info("Bestium enabled successfully (took ${System.currentTimeMillis() - start} ms)")
    }

    override fun createKey(@KeyPattern.Value key: String): NamespacedKey = NamespacedKey(this, key)
}
