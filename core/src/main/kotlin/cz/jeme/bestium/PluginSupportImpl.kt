package cz.jeme.bestium

import cz.jeme.bestium.api.Bestium
import cz.jeme.bestium.api.PluginSupport
import net.minecraft.world.entity.Entity
import org.bukkit.Bukkit
import org.bukkit.entity.Bee

object PluginSupportImpl : PluginSupport {
    init {
        val entity = null!! as Bee
        val key = Bestium.requireInjectedEntityKey(entity)
        val first = Bestium.getInjector().getInjections<Entity>().values.first { it.getKey() == key }
    }

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