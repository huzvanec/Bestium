package cz.jeme.bestium

import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.damage.CraftDamageSource
import org.bukkit.craftbukkit.entity.CraftLivingEntity
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

object LootManager : Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private fun EntityDeathEvent.entity() {
        val craftEntity = entity as? CraftLivingEntity ?: return
        val mojangEntity = craftEntity.handle

        val inj = EntityManagerImpl.getInjection(mojangEntity) ?: return

        val craftSource = damageSource as CraftDamageSource
        val mojangSource = craftSource.handle

        val serverLevel = (entity.world as CraftWorld).handle

        val lootParams = LootParams.Builder(serverLevel)
            .withParameter(LootContextParams.THIS_ENTITY, mojangEntity)
            .withParameter(LootContextParams.ORIGIN, mojangEntity.position())
            .withParameter(LootContextParams.DAMAGE_SOURCE, mojangSource)
            .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, mojangSource.entity)
            .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, mojangSource.directEntity)
            .apply {
                val wasKilledByPlayer = mojangEntity.lastHurtByPlayerMemoryTime > 0
                val killerPlayer = mojangEntity.getLastHurtByPlayer()
                if (wasKilledByPlayer && killerPlayer != null) {
                    withParameter(LootContextParams.LAST_DAMAGE_PLAYER, killerPlayer)
                    withLuck(killerPlayer.luck)
                }
            }
            .create(LootContextParamSets.ENTITY)

        for (lootTable in inj.lootTables) {
            val loot = lootTable.getRandomItems(lootParams, mojangEntity.lootTableSeed)
                .map { CraftItemStack.asBukkitMirror(it) }

            drops.addAll(loot)
        }
    }
}
