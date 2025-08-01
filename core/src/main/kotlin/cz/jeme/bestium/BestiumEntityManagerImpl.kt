package cz.jeme.bestium

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import cz.jeme.bestium.api.BestiumEntityManager
import cz.jeme.bestium.api.inject.Injectable
import cz.jeme.bestium.api.inject.variant.BoundEntityVariant
import cz.jeme.bestium.api.inject.variant.VariantPicker
import cz.jeme.bestium.persistence.PersistentData
import kr.toxicity.model.api.tracker.EntityTrackerRegistry
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType
import org.bukkit.entity.Entity as BukkitEntity

const val CURRENT_DATA_VERSION = 1

fun fetchDataVersion(entity: BukkitEntity): Int {
    return PersistentData.BESTIUM_DATA_VERSION[entity] ?: -1
}

object BestiumEntityManagerImpl : BestiumEntityManager, Listener {
    /**
     * Migrates an entity to the latest data version.
     * Returns `false` if no migration happened, `true` if migration was successful.
     */
    private fun migrate(bukkitEntity: BukkitEntity, entityType: EntityType<*>): Boolean {
        when (fetchDataVersion(bukkitEntity)) {
            CURRENT_DATA_VERSION -> return false // the entity does not need to be migrated

            -1 -> {
                BestiumPlugin.componentLogger.error(
                    "Cannot migrate entity '${bukkitEntity.uniqueId}' of type '${entityType}'. No data version is present."
                )
                return false
            }

            // currently there is no migration logic here, but it will be inserted when necessary
        }

        PersistentData.BESTIUM_DATA_VERSION[bukkitEntity] = CURRENT_DATA_VERSION
        return true
    }

    private fun postInitializeBestiumEntity(entity: Injectable) {
        entity as Entity
        val injection = entity.bestium_getInjection()

        val key = injection.key // the key of the Bestium entity
        val bukkitEntity = entity.bukkitEntity // so we can use Bukkit API
        val realType = injection.realType

        // true if this entity is spawning for the first time
        val isFirstSpawn = !PersistentData.BESTIUM_ID.has(bukkitEntity)

        // save Bestium entity key
        // runs regardless of whether the entity already has the key stored
        // ensures that it cannot be tampered with as much
        PersistentData.BESTIUM_ID[bukkitEntity] = key

        /** Picks a variant for the entity, returns `null` if no model should be applied */
        fun pickVariant(): BoundEntityVariant? {
            val variant = injection.variantPicker.pick(
                injection.variants,
                VariantPicker.Context(
                    entity,
                    realType,
                    injection
                )
            )

            variant?.let {
                // a variant should be applied, check that it's valid

                val variantEntityKey = variant.injection.key
                if (variantEntityKey != key) {
                    // the picked variant does not belong to the entity spawning, but to a different entity
                    throw IllegalStateException(
                        "Provided entity variant is registered for a different entity: ${variantEntityKey.asString()} instead of ${key.asString()}"
                    )
                }
            }

            return variant
        }

        /**
         * Attempts to apply a model to the entity, returns `true` if the
         * model was applied, if BetterModel isn't loaded, returns `false`
         *
         * Will throw an exception if BetterModel is not loaded.
         */
        fun applyModel(modelName: String) {
            // default BM API cannot be used here, because in cases where the entity spawns naturally,
            // it is not correctly loaded and BM cannot track it
            // a workaround for this is to directly save the BM model data to this entity
            // in legacy formatting and then wait for BM to pick the entity up and migrate it
            // TODO can this issue be somehow reproduced without Bestium's injections?
            bukkitEntity.persistentDataContainer.set(
                EntityTrackerRegistry.TRACKING_ID,
                PersistentDataType.STRING,
                modelName
            )
            // remove pending model application (if any is present)
            PersistentData.BESTIUM_PENDING_MODEL.remove(bukkitEntity)
        }

        if (isFirstSpawn) {
            val variant = pickVariant()
            PersistentData.BESTIUM_VARIANT[bukkitEntity] = (variant?.id ?: "NONE") // save variant data

            variant?.let {
                // a variant is present, try to apply it with better model
                if (PluginSupportImpl.betterModel()) {
                    applyModel(it.modelName)
                } else {
                    // BetterModel currently isn't loaded
                    // but the user might want to install it later,
                    // so the modelName is saved for later use
                    PersistentData.BESTIUM_PENDING_MODEL[bukkitEntity] = variant.modelName
                }
            }

            // store current data version
            PersistentData.BESTIUM_DATA_VERSION[bukkitEntity] = CURRENT_DATA_VERSION
        } else { // the entity was already spawned and is just being loaded
            migrate(bukkitEntity, realType)

            if (PluginSupportImpl.betterModel()) {
                // better model is loaded check for a pending model
                PersistentData.BESTIUM_PENDING_MODEL[bukkitEntity]?.let {
                    applyModel(it)
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun EntityAddToWorldEvent.handle() {
        val nms = (entity as CraftEntity).handle
        postInitializeBestiumEntity(nms as? Injectable ?: return)
    }

    override fun isInjectedEntity(entity: BukkitEntity) = PersistentData.BESTIUM_ID.has(entity)

    override fun getInjectedEntityKey(entity: BukkitEntity) = PersistentData.BESTIUM_ID[entity]
}