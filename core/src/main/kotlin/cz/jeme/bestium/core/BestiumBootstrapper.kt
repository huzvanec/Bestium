package cz.jeme.bestium.core

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext

@Suppress("UnstableApiUsage")
internal class BestiumBootstrapper : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {}

    override fun createPlugin(context: PluginProviderContext) = BestiumImpl
}