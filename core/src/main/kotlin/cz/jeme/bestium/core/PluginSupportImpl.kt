package cz.jeme.bestium.core

import cz.jeme.bestium.api.PluginSupport
import org.bukkit.Bukkit

object PluginSupportImpl : PluginSupport {
    private fun loaded(name: String) = lazy {
        Bukkit.getPluginManager().getPlugin(name) != null
    }

    private val betterModel by loaded("BetterModel")
    override fun betterModel() = betterModel

    private val nova by loaded("Nova")
    override fun nova() = nova
}