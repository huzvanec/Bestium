package cz.jeme.bestium

import net.minecraft.core.registries.Registries
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent

object WorldLoadListener : Listener {
    private var worldsLoadedCount: Int = 0

    private val worldsToLoad: Int by lazy {
        // see MinecraftServer#loadWorld0(String)
        val dedicatedServer = (Bukkit.getServer() as CraftServer).server
        val access = dedicatedServer.registryAccess()
        val levelStems = access.lookupOrThrow(Registries.LEVEL_STEM)
        levelStems.size()
    }

    /**
     * `true` all worlds were loaded successfully, otherwise `false`.
     */
    var worldsLoaded: Boolean = false
        private set

    init {
        Bukkit.getPluginManager().registerEvents(this, BestiumPlugin)
    }


    @Suppress("UnusedReceiverParameter")
    @EventHandler
    private fun WorldLoadEvent.handle() {
        if (++worldsLoadedCount == worldsToLoad) worldsLoaded = true
    }
}