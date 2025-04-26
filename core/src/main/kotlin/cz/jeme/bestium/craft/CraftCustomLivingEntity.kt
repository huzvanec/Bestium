package cz.jeme.bestium.craft

import net.minecraft.world.entity.LivingEntity
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftLivingEntity

class CraftCustomLivingEntity(server: CraftServer, entity: LivingEntity) : CraftLivingEntity(server, entity) {
    override fun toString(): String {
        return "CraftCustomLivingEntity{id=$entityId, backedBy=${entity.type}}"
    }
}