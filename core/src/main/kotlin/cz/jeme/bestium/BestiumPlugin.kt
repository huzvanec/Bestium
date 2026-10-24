package cz.jeme.bestium

import cz.jeme.bestium.api.Bestium
import cz.jeme.bestium.command.BestiumCommand
import cz.jeme.bestium.command.SpawnEggCommand
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

    override fun onLoad() {
        super.onLoad()

        PluginSupportImpl.betterModelHook?.apply {
            val betterModelVersionRaw = Bukkit.getPluginManager()
                .getPlugin("BetterModel")!!
                .pluginMeta
                .version

            val betterModelVersion = betterModelVersionRaw
                .split('-')[0]
                .split('.')

            val requiredBettermodelVersion = PluginSupportImpl.requiredBettermodelVersion.split('.')

            val majorValid = betterModelVersion[0] == requiredBettermodelVersion[0]
            val minorValid = betterModelVersion[1] >= requiredBettermodelVersion[1]
            val patchValid = betterModelVersion[2] >= requiredBettermodelVersion[2]

            if (!(majorValid && minorValid && patchValid)) {
                logger.error("This BetterModel version is not compatible with Bestium")
                if (majorValid) {
                    logger.error("Required: >=${PluginSupportImpl.requiredBettermodelVersion}, found: $betterModelVersionRaw")
                } else {
                    logger.error("Required BetterModel version: >=${PluginSupportImpl.requiredBettermodelVersion}, found: $betterModelVersionRaw (major version mismatch)")
                }
                logger.error("Server will be shut down")
                Bukkit.shutdown()
                return@apply
            }

            if (logVerbose) logger.info("Registering hook for BetterModel v${betterModelVersionRaw}")
            register()
        }
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

        if (logVerbose) logger.info("Registering commands")
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands = event.registrar()
            SpawnEggCommand(this, commands)
            BestiumCommand(this, commands)
        }

        if (logVerbose) logger.info("Registering event listeners")
        fun Listener.registerEvents() = Bukkit.getPluginManager().registerEvents(this, this@BestiumPlugin)

        EntityManagerImpl.registerEvents()
        LootManager.registerEvents()

        if (logVerbose) logger.info("Registering entity translations")
        EntityInjectorImpl.injections.values.forEach(EntityTranslator::addInjection)
        EntityTranslator.register()

        if (logNormal) logger.info("Bestium enabled successfully (took ${System.currentTimeMillis() - start} ms)")
    }

    override fun createKey(@KeyPattern.Value key: String): NamespacedKey = NamespacedKey(this, key)
}
