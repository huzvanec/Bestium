package cz.jeme.bestium

import cz.jeme.bestium.api.PluginSupport
import org.bukkit.Bukkit

object PluginSupportImpl : PluginSupport {
    private fun loaded(name: String): Boolean {
        try {
            return Bukkit.getPluginManager().getPlugin(name) != null
        } catch (_: NullPointerException) {
            throw IllegalStateException("Cannot check for plugin support, server is still in bootstrap phase")
        }
    }

    override fun betterModel() = loaded("BetterModel")

    override fun nova() = loaded("Nova")
}