package cz.jeme.bestium

import cz.jeme.bestium.config.logNormal
import cz.jeme.bestium.inject.EntityInjectorImpl
import cz.jeme.bestium.util.flushLoggingAndCrashJvm
import cz.jeme.bestium.util.storeApiInstance
import io.papermc.paper.ServerBuildInfo
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import java.nio.file.Path

lateinit var dataFolder: Path

@Suppress("UnstableApiUsage", "unused")
internal class BestiumBootstrapper : PluginBootstrap {
    private var injectionHappened = false
    private val logger = ComponentLogger.logger("BestiumBootstrapper")

    override fun bootstrap(context: BootstrapContext) {
        try {
            bootstrap0(context)
        } catch (t: Throwable) {
            logger.error("A fatal exception occurred during bootstrap: ", t)
            flushLoggingAndCrashJvm()
        }
    }

    private fun bootstrap0(context: BootstrapContext) {
        val start = System.currentTimeMillis()
        dataFolder = context.dataDirectory.apply { toFile().mkdirs() }
        if (logNormal) logger.info("Starting bootstrap")

        try {
            Class.forName("org.spongepowered.asm.mixin.Mixin")
        } catch (_: ClassNotFoundException) {
            logger.error("SpongePowered MIXIN Subsystem is not loaded")
            logger.error("Please make sure you start your Minecraft server with a '-javaagent:plugins/Bestium-VERSION.jar' argument")
            logger.error("The server will now be terminated")

            flushLoggingAndCrashJvm()
        }

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

        if (logNormal) logger.info("Bootstrap OK (took ${System.currentTimeMillis() - start} ms)")
    }

    override fun createPlugin(context: PluginProviderContext) = BestiumPlugin
}