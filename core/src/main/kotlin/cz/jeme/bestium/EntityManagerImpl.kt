package cz.jeme.bestium

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import cz.jeme.bestium.api.EntityManager
import cz.jeme.bestium.api.inject.EntityInjection
import cz.jeme.bestium.api.inject.variant.BoundEntityVariant
import cz.jeme.bestium.api.inject.variant.EntitySpawnContext
import cz.jeme.bestium.inject.EntityInjectorImpl
import cz.jeme.bestium.persistence.PersistentData
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import kr.toxicity.model.api.tracker.EntityTrackerRegistry
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import org.bukkit.Location
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.util.CraftLocation
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.persistence.PersistentDataType
import java.util.function.Consumer
import org.bukkit.entity.Entity as BukkitEntity

const val CURRENT_DATA_VERSION = 1

const val NO_VARIANT_ID = "NONE"

fun fetchDataVersion(entity: BukkitEntity): Int {
    return PersistentData.BESTIUM_DATA_VERSION[entity] ?: -1
}

object EntityManagerImpl : EntityManager, Listener {
    private val _noVariantEntityIds = IntOpenHashSet()
    val noVariantEntityIds: IntSet = _noVariantEntityIds

    /**
     * Migrates an entity to the latest data version.
     * Returns `false` if no migration happened, `true` if migration was successful.
     */
    private fun migrate(entity: BukkitEntity, entityType: EntityType<*>): Boolean {
        when (fetchDataVersion(entity)) {
            CURRENT_DATA_VERSION -> return false // the entity does not need to be migrated

            -1 -> {
                BestiumPlugin.componentLogger.error(
                    "Cannot migrate entity '${entity.uniqueId}' of type '${entityType}'. No data version is present."
                )
                return false
            }

            // currently there is no migration logic here, but it will be inserted when necessary
        }

        PersistentData.BESTIUM_DATA_VERSION[entity] = CURRENT_DATA_VERSION
        return true
    }

    private fun postInitializeBestiumEntity(entity: BukkitEntity) {
        val injection = getInjection(entity) ?: return

        val key = injection.key // the key of the Bestium entity
        val realType = injection.realType

        // true if this entity is spawning for the first time
        val isFirstSpawn = !PersistentData.BESTIUM_VARIANT.has(entity)

        /** Picks a variant for the entity, returns `null` if no model should be applied */
        fun pickVariant(): BoundEntityVariant? {
            val variant = injection.variantRule.apply(
                injection.variants,
                EntitySpawnContext(entity)
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
            entity.persistentDataContainer.set(
                EntityTrackerRegistry.TRACKING_ID,
                PersistentDataType.STRING,
                modelName
            )
            // remove pending model application (if any is present)
            PersistentData.BESTIUM_PENDING_MODEL.remove(entity)
        }

        if (isFirstSpawn) {
            val variant = pickVariant()
            PersistentData.BESTIUM_VARIANT[entity] = (variant?.id ?: run {
                _noVariantEntityIds += entity.entityId
                NO_VARIANT_ID
            }) // save variant data

            variant?.let {
                // a variant is present, try to apply it with better model
                if (PluginSupportImpl.isBetterModelLoaded()) {
                    applyModel(it.modelName)
                } else {
                    // BetterModel currently isn't loaded
                    // but the user might want to install it later,
                    // so the modelName is saved for later use
                    PersistentData.BESTIUM_PENDING_MODEL[entity] = variant.modelName
                }
            }

            // store current data version
            PersistentData.BESTIUM_DATA_VERSION[entity] = CURRENT_DATA_VERSION
        } else { // the entity was already spawned and is just being loaded
            migrate(entity, realType)

            if (PersistentData.BESTIUM_VARIANT[entity] == NO_VARIANT_ID) {
                _noVariantEntityIds += entity.entityId
            }

            if (PluginSupportImpl.isBetterModelLoaded()) {
                // better model is loaded check for a pending model
                PersistentData.BESTIUM_PENDING_MODEL[entity]?.let {
                    applyModel(it)
                }
            }
        }
    }

    // optimized contains(EntityType)
    private val injectedTypes by lazy { EntityInjectorImpl.types.values.toHashSet() }

    override fun isInjected(type: EntityType<*>) = injectedTypes.contains(type)

    override fun getInjection(entity: Entity) = EntityInjectorImpl.injections[entity.javaClass]

    @EventHandler(priority = EventPriority.LOWEST)
    private fun EntityAddToWorldEvent.handle() {
        if (!isInjected(entity)) return
        postInitializeBestiumEntity(entity)
    }

    override fun <T : Entity> spawn(
        location: Location,
        entityClass: Class<T>,
        entitySpawnReason: EntitySpawnReason,
        creatureSpawnReason: CreatureSpawnEvent.SpawnReason,
        consumer: Consumer<T>
    ): T? {
        val type = EntityInjectorImpl.getType(entityClass) ?: throw IllegalArgumentException(
            "Entity '$entityClass' is not an injected entity!"
        )
        return type.spawn(
            (location.world as CraftWorld).handle,
            { entity ->
                entity.snapTo(
                    location.x,
                    location.y,
                    location.z,
                    location.yaw,
                    location.pitch
                )
            },
            CraftLocation.toBlockPosition(location),
            entitySpawnReason,
            false,
            false,
            creatureSpawnReason,
        )
    }

    private val backingTypes by lazy {
        EntityInjectorImpl.injections.values.associateBy(
            EntityInjection<*, *>::getRealType,
            EntityInjection<*, *>::getBackingType
        )
    }

    override fun getBackingType(type: EntityType<*>) = backingTypes[type]
}