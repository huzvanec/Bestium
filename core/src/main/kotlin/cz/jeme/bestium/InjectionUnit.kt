package cz.jeme.bestium

import com.mojang.datafixers.DataFixUtils
import cz.jeme.bestium.api.inject.ConvertFunction
import cz.jeme.bestium.api.inject.EntityInjection
import cz.jeme.bestium.util.setStaticFinal
import cz.jeme.bestium.util.toNamespacedKey
import cz.jeme.bestium.util.toResourceLocation
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minecraft.SharedConstants
import net.minecraft.core.Holder
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.util.datafix.DataFixers
import net.minecraft.util.datafix.fixes.References
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.DefaultAttributes
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftEntityType
import org.bukkit.craftbukkit.entity.CraftEntityTypes
import org.bukkit.craftbukkit.entity.CraftEntityTypes.EntityTypeData
import java.util.*
import java.util.function.BiFunction
import java.util.function.Consumer
import com.mojang.datafixers.types.Type as DataFixerType
import net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE as ENTITY_TYPE_REGISTRY
import org.bukkit.Registry as BukkitRegistry
import org.bukkit.entity.Entity as BukkitEntity
import org.bukkit.entity.EntityType as BukkitEntityType

class InjectionUnit(val injections: Collection<EntityInjection<*, *>>) {
    private val logger = ComponentLogger.logger("BestiumInjectionUnit")
    private val _types = mutableMapOf<Class<out Entity>, EntityType<*>>()
    private val _keyedTypes = mutableMapOf<Key, EntityType<*>>()
    private val toInject = injections.size
    val types: Map<Class<out Entity>, EntityType<*>> = _types
    val keyedTypes: Map<Key, EntityType<*>> = _keyedTypes

    fun minecraftInjection() {
        logger.info("[Phase 1/2] Starting entity injection (bootstrap phase)")

        logger.info("[Phase 1/2] Fetching DataFixer entity types")
        @Suppress("UNCHECKED_CAST")
        val dataFixerTypes = DataFixers.getDataFixer()
            .getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().dataVersion().version()))
            .findChoiceType(References.ENTITY)
            .types() as MutableMap<String, DataFixerType<*>>

        logger.info("[Phase 1/2] Retrieving default attribute suppliers")
        val attributeSuppliersField = DefaultAttributes::class.java.getDeclaredField("SUPPLIERS")
            .apply { isAccessible = true }

        @Suppress("UNCHECKED_CAST")
        val attributeSuppliers: MutableMap<EntityType<out Entity>, AttributeSupplier> = (
                attributeSuppliersField[null] as Map<EntityType<out Entity>, AttributeSupplier>
                ).toMutableMap()

        logger.info("[Phase 1/2] Resetting unregistered intrusive entity type holders")
        MappedRegistry::class.java.getDeclaredField("unregisteredIntrusiveHolders")
            .apply { isAccessible = true }
            .set(ENTITY_TYPE_REGISTRY, IdentityHashMap<EntityType<*>, Holder.Reference<EntityType<*>>>())

        logger.info("[Phase 1/2] Unfreezing entity type registry")
        val frozenField = MappedRegistry::class.java.getDeclaredField("frozen")
            .apply { isAccessible = true }

        frozenField.set(ENTITY_TYPE_REGISTRY, false)

        logger.info("[Phase 1/2] Injecting $toInject entit${if (toInject == 1) "y" else "ies"}")
        for (inj in injections) {
            val entityClass = inj.entityClass
            val key = inj.key
            val keyStr = key.asString()
            val backingType = inj.backingType

            // copy data fixer from backing type
            dataFixerTypes[keyStr] = dataFixerTypes[ENTITY_TYPE_REGISTRY.getKey(backingType).toString()]
                ?: throw IllegalStateException("No data fixer registered for backing type: '$backingType'")

            // create main entity type
            val nmsType = EntityType.Builder.of(
                inj.entityFactory,
                inj.mobCategory
            )
                .clientTrackingRange(backingType.clientTrackingRange())
                .sized(backingType.width, backingType.height)
                .eyeHeight(backingType.dimensions.eyeHeight)
                .apply {
                    @Suppress("UNCHECKED_CAST")
                    (inj.typeCustomizer as Consumer<EntityType.Builder<out Entity>>).accept(this)
                }
                .build(
                    ResourceKey.create(
                        ENTITY_TYPE_REGISTRY.key(),
                        key.toResourceLocation()
                    )
                )

            // register entity type to registry
            ENTITY_TYPE_REGISTRY.createIntrusiveHolder(nmsType)
            Registry.register(ENTITY_TYPE_REGISTRY, keyStr, nmsType)

            // register default attributes
            inj.defaultAttributes?.let { attributeSuppliers[nmsType] = it }

            // copy registry holder from backing type 
            val registryHolder = EntityType::class.java.getDeclaredField("builtInRegistryHolder")
                .apply { isAccessible = true }
            registryHolder[nmsType] = registryHolder[backingType]

            // store entity type for later use
            _types[entityClass] = nmsType
            _keyedTypes[key] = nmsType
        }

        logger.info("[Phase 1/2] Overwriting default attribute suppliers")
        // set default attributes
        attributeSuppliersField.setStaticFinal(attributeSuppliers)

        logger.info("[Phase 1/2] Refreezing entity type registry")
        frozenField.set(ENTITY_TYPE_REGISTRY, true)

        logger.info("[Phase 1/2] Bootstrap injection complete")
    }

    fun bukkitInjection() {
        logger.info("[Phase 2/2] Starting injection (load phase)")

        // map of nms backing entity type -> list of entity injections
        val bukkitTypeDataContainer = mutableMapOf<EntityType<out Entity>, MutableList<EntityInjection<*, *>>>()

        logger.info("[Phase 2/2] Fetching Bukkit entity type registry internals")
        @Suppress("UnstableApiUsage")
        val simpleRegistryMapField = BukkitRegistry.SimpleRegistry::class.java.getDeclaredField("map")
            .apply { isAccessible = true }

        @Suppress("UNCHECKED_CAST")
        val simpleRegistryMap = (
                simpleRegistryMapField[BukkitRegistry.ENTITY_TYPE] as Map<NamespacedKey, BukkitEntityType>
                ).toMutableMap()

        logger.info("[Phase 2/2] Injecting $toInject entit${if (toInject == 1) "y" else "ies"} into Bukkit entity registry")
        for (inj in injections) {
            val backingType = inj.backingType
            val entityClass = inj.entityClass
            val key = inj.key

            // add class to bukkit type container
            val classes = bukkitTypeDataContainer.computeIfAbsent(backingType) { mutableListOf() }
            val index = classes.indexOfFirst { it.entityClass.isAssignableFrom(entityClass) }
                .let { if (it == -1) classes.size else it }
            classes.add(index, inj)

            // inject into Bukkit registry
            simpleRegistryMap[key.toNamespacedKey()] = CraftEntityType.minecraftToBukkit(backingType)
        }

        logger.info("[Phase 2/2] Retrieving Bukkit entity type data")
        val bukkitEntityTypeDataField = CraftEntityTypes::class.java.getDeclaredField("ENTITY_TYPE_DATA")
            .apply { isAccessible = true }

        @Suppress("UNCHECKED_CAST")
        val bukkitEntityTypeData: MutableMap<BukkitEntityType, EntityTypeData<*, *>> = (
                bukkitEntityTypeDataField[null] as Map<BukkitEntityType, EntityTypeData<*, *>>
                ).toMutableMap()

        logger.info("[Phase 2/2] Injecting $toInject entit${if (toInject == 1) "y" else "ies"} into Bukkit entity type data")
        // inject into backing bukkit entity type data
        bukkitTypeDataContainer.forEach { (type, classes) ->
            val craftType = CraftEntityType.minecraftToBukkit(type)
            val originalData = bukkitEntityTypeData[craftType]!!

            bukkitEntityTypeData[craftType] = EntityTypeData(
                craftType,
                BukkitEntity::class.java,
                { server, nms ->
                    val inj = classes.firstOrNull { it.entityClass.isInstance(nms) }

                    inj?.let {
                        (inj.convertFunction as ConvertFunction<Entity, BukkitEntity>).apply(
                            server,
                            nms
                        )
                    } ?: (originalData.convertFunction as BiFunction<CraftServer, Entity, BukkitEntity>).apply(
                        server,
                        nms
                    )
                },
                originalData.spawnFunction
            )
        }

        logger.info("[Phase 2/2] Overwriting Bukkit entity type data")
        bukkitEntityTypeDataField.setStaticFinal(bukkitEntityTypeData)

        logger.info("[Phase 2/2] Overwriting Bukkit entity type registry internals")
        simpleRegistryMapField.set(BukkitRegistry.ENTITY_TYPE, simpleRegistryMap)

        logger.info("[Phase 2/2] Load injection complete")
    }
}