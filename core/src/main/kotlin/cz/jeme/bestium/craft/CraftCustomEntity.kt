package cz.jeme.bestium.craft

import net.minecraft.world.entity.Entity
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftEntity

class CraftCustomEntity(server: CraftServer, entity: Entity) : CraftEntity(server, entity) {
    override fun toString(): String {
        return "CraftCustomEntity{id=$entityId, backedBy=${entity.type}}"
    }
}