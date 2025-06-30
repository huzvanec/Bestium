package cz.jeme.bestium

import cz.jeme.bestium.util.InstanceUtils
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents

@Suppress("UnstableApiUsage")
internal class BestiumBootstrapper : PluginBootstrap {
    private var lock = false

    override fun bootstrap(context: BootstrapContext) {
        InstanceUtils.storeApiInstance(EntityInjectorImpl)

        val handler = LifecycleEvents.DATAPACK_DISCOVERY.newHandler {
            if (lock) return@newHandler; lock = true
            EntityInjectorImpl.injectMinecraft() // phase 1 injection
        }.priority(-1)

        context.lifecycleManager.registerEventHandler(handler)
    }

    override fun createPlugin(context: PluginProviderContext) = BestiumPlugin
}