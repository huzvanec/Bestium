package cz.jeme.bestium

import cz.jeme.bestium.api.PluginSupport
import cz.jeme.bestium.hooks.bettermodel.BetterModelHook
import cz.jeme.bestium.util.CachedFirst
import org.bukkit.Bukkit

object PluginSupportImpl : PluginSupport {
    val betterModelHook by CachedFirst.NotNull { if (isBetterModelLoaded()) BetterModelHook() else null }

    private fun loadedPlugin(name: String): Boolean {
        try {
            Bukkit.getPluginManager().getPlugin(name)
            return true
        } catch (e: NullPointerException) {
            throw IllegalStateException("Cannot check for plugin support, server is still in bootstrap phase", e)
        }
    }

    private fun loadedClass(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    val betterModelLoaded by CachedFirst.True {
        loadedPlugin("BetterModel") &&
                loadedClass("kr.toxicity.model.api.BetterModel") &&
                loadedClass("kr.toxicity.model.api.bukkit.BetterModelBukkit")
    }

    override fun isBetterModelLoaded() = betterModelLoaded

    val novaLoaded by CachedFirst.True { loadedPlugin("Nova") }

    override fun isNovaLoaded() = novaLoaded
}
