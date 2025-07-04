package cz.jeme.bestium

import cz.jeme.bestium.util.InstanceUtils
import cz.jeme.bestium.util.flushLoggingAndCrashJvm
import io.papermc.paper.ServerBuildInfo
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents

@Suppress("UnstableApiUsage")
internal class BestiumBootstrapper : PluginBootstrap {
    private var injectionHappened = false

    override fun bootstrap(context: BootstrapContext) {
        val logger = context.logger
        val requiredVersion = context.pluginMeta.apiVersion
        val actualVersion = ServerBuildInfo.buildInfo().minecraftVersionId()

        if (requiredVersion != actualVersion) {
            logger.error("This version of Bestium requires Minecraft version '$requiredVersion', but the current version is '$actualVersion'")
            logger.error("The server will now be terminated")

            flushLoggingAndCrashJvm()
            return
        }

        InstanceUtils.storeApiInstance(EntityInjectorImpl)

        val handler = LifecycleEvents.DATAPACK_DISCOVERY.newHandler {
            if (injectionHappened) return@newHandler; injectionHappened = true
            EntityInjectorImpl.injectMinecraft() // phase 1 injection
        }.priority(-1)

        context.lifecycleManager.registerEventHandler(handler)
    }

    override fun createPlugin(context: PluginProviderContext) = BestiumPlugin
}