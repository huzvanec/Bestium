package cz.jeme.bestium

import cz.jeme.bestium.inject.EntityInjectorImpl
import cz.jeme.bestium.inject.patch.Patcher
import cz.jeme.bestium.util.flushLoggingAndCrashJvm
import cz.jeme.bestium.util.storeApiInstance
import io.papermc.paper.ServerBuildInfo
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.logger.slf4j.ComponentLogger

@Suppress("UnstableApiUsage", "unused")
internal class BestiumBootstrapper : PluginBootstrap {
    private var injectionHappened = false
    private val logger = ComponentLogger.logger("BestiumBootstrapper")

    override fun bootstrap(context: BootstrapContext) {
        val start = System.currentTimeMillis()
        logger.info("Starting bootstrap")

        Patcher.run()

        val requiredVersion = context.pluginMeta.apiVersion
        val actualVersion = ServerBuildInfo.buildInfo().minecraftVersionId()

        if (requiredVersion != actualVersion) {
            logger.error("This version of Bestium requires Minecraft version '$requiredVersion', but the current version is '$actualVersion'")
            logger.error("The server will now be terminated")

            flushLoggingAndCrashJvm()
            return
        }

        storeApiInstance(EntityInjectorImpl)
        storeApiInstance(EntityManagerImpl)

        val injectionHandler = LifecycleEvents.DATAPACK_DISCOVERY.newHandler {
            if (injectionHappened) return@newHandler; injectionHappened = true
            EntityInjectorImpl.injectBootstrap() // phase 1 injection
        }.priority(-1) // prioritize

        context.lifecycleManager.registerEventHandler(injectionHandler)

        logger.info("Bootstrap OK (took ${System.currentTimeMillis() - start} ms)")
    }

    override fun createPlugin(context: PluginProviderContext) = BestiumPlugin
}