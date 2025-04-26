package cz.jeme.bestium

import com.mojang.datafixers.DataFixUtils
import com.mojang.datafixers.types.Type
import cz.jeme.bestium.api.entity.Injectable
import cz.jeme.bestium.api.inject.EntityInjection
import cz.jeme.bestium.api.inject.EntityInjector
import cz.jeme.bestium.craft.CraftCustomEntity
import cz.jeme.bestium.craft.CraftCustomLivingEntity
import cz.jeme.bestium.util.setStaticFinal
import cz.jeme.bestium.util.toResourceLocation
import kr.toxicity.model.api.BetterModel
import net.kyori.adventure.key.Key
import net.minecraft.SharedConstants
import net.minecraft.core.Holder
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.util.datafix.DataFixers
import net.minecraft.util.datafix.fixes.References
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.DefaultAttributes
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftEntityType
import org.bukkit.craftbukkit.entity.CraftEntityTypes
import org.bukkit.craftbukkit.entity.CraftEntityTypes.EntityTypeData
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.logging.Level
import net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE as ENTITY_TYPE_REGISTRY
import org.bukkit.entity.Entity as BukkitEntity
import org.bukkit.entity.EntityType as BukkitEntityType


internal object EntityInjectorImpl : EntityInjector {
    private val logger by lazy { BestiumImpl.logger }

    private val injections = mutableMapOf<Class<out Entity>, EntityInjection<*>>()

    override fun <T> injections(): Map<Class<T>, EntityInjection<T>> where T : Entity, T : Injectable {
        @Suppress("UNCHECKED_CAST")
        return injections as Map<Class<T>, EntityInjection<T>>
    }

    var frozen: Boolean = false
        private set

    override fun frozen() = frozen

    private val keys = mutableSetOf<Key>()

    override fun register(injection: EntityInjection<*>) {
        if (frozen)
            throw IllegalStateException("Entities already injected. Registrations must be done before the plugin is loaded.")
        val clazz = injection.entityClass()
        if (injections.containsKey(clazz))
            throw IllegalArgumentException("There is already a registered injection for the entity class '${clazz.name}'")
        if (keys.contains(injection.key()))
            throw IllegalArgumentException("There is already a registered injection with key '${injection.key()}'")
        keys.add(injection.key())
        injections[clazz] = injection
    }

    private fun doInject() {
        // reset entity type holders
        MappedRegistry::class.java.getDeclaredField("unregisteredIntrusiveHolders")
            .apply { isAccessible = true }
            .set(
                ENTITY_TYPE_REGISTRY,
                IdentityHashMap<EntityType<*>, Holder.Reference<EntityType<*>>>()
            )

        // unfreeze entity type registry
        MappedRegistry::class.java.getDeclaredField("frozen")
            .apply { isAccessible = true }
            .set(ENTITY_TYPE_REGISTRY, false)

        // obtain data fixer types
        @Suppress("UNCHECKED_CAST")
        val dataFixer = DataFixers.getDataFixer()
            .getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().dataVersion.version))
            .findChoiceType(References.ENTITY)
            .types() as MutableMap<String, Type<*>>

        // obtain attribute suppliers
        val attributeSuppliers = DefaultAttributes::class.java.getDeclaredField("SUPPLIERS")
            .apply { isAccessible = true }

        @Suppress("UNCHECKED_CAST")
        val attributeSupplierMap = (
                attributeSuppliers.get(null) as Map<EntityType<out Entity>, AttributeSupplier>
                ).toMutableMap()

        // obtain entity type data
        val entityTypeData = CraftEntityTypes::class.java.getDeclaredField("ENTITY_TYPE_DATA")
            .apply { isAccessible = true }

        @Suppress("UNCHECKED_CAST")
        val entityTypeDataMap = (
                entityTypeData.get(null) as Map<BukkitEntityType, EntityTypeData<*, *>>
                ).toMutableMap()

        val bukkitTypeDataContainer = mutableMapOf<EntityType<out Entity>, MutableList<Class<out Entity>>>()

        for (inj in injections.values) {
            val entityClass = inj.entityClass()
            val key = inj.key()
            val keyStr = key.asString()
            val backingType = inj.backingType()

            // copy data fixer from backing type
            dataFixer[keyStr] = dataFixer[ENTITY_TYPE_REGISTRY.getKey(backingType).toString()]
                ?: throw IllegalArgumentException("No data fixer registered for backing type '$backingType'")

            // create main entity type
            val nmsType = EntityType.Builder.of(
                inj.entityFactory(),
                inj.mobCategory()
            )
                .clientTrackingRange(backingType.clientTrackingRange())
                .sized(backingType.width, backingType.height)
                .eyeHeight(backingType.dimensions.eyeHeight)
                .apply {
                    @Suppress("UNCHECKED_CAST")
                    (inj.typeCustomizer() as Consumer<EntityType.Builder<out Entity>>).accept(this)
                }.build(
                    ResourceKey.create(
                        ENTITY_TYPE_REGISTRY.key(),
                        key.toResourceLocation()
                    )
                )

            // register entity type to registry
            ENTITY_TYPE_REGISTRY.createIntrusiveHolder(nmsType)
            Registry.register(ENTITY_TYPE_REGISTRY, keyStr, nmsType)

            // register default attributes
            val attributes = inj.attributes()
            if (attributes != null) attributeSupplierMap[nmsType] = attributes

            // copy registry holder from backing type 
            val registryHolder = EntityType::class.java.getDeclaredField("builtInRegistryHolder")
                .apply { isAccessible = true }
            registryHolder.set(nmsType, registryHolder.get(backingType))

            // add class to bukkit type container
            val classes = bukkitTypeDataContainer.computeIfAbsent(backingType) { mutableListOf() }
            var index = 0
            while (index < classes.size && !classes[index].isAssignableFrom(entityClass)) index++
            classes.add(index, entityClass)
        }

        // set default attributes
        attributeSuppliers.setStaticFinal(attributeSupplierMap)

        // inject into bukkit entity type data
        bukkitTypeDataContainer.forEach { (type, classes) ->
            val craftType = CraftEntityType.minecraftToBukkit(type)
            val originalData = entityTypeDataMap[craftType]!!

            entityTypeDataMap[craftType] = EntityTypeData(
                craftType,
                BukkitEntity::class.java,
                { server, nms ->
                    val matchingPair = classes.firstOrNull { it.isInstance(nms) }

                    @Suppress("UNCHECKED_CAST")
                    if (matchingPair == null)
                        (originalData.convertFunction as BiFunction<CraftServer, Entity, BukkitEntity>).apply(
                            server,
                            nms
                        )
                    else if (nms is LivingEntity) CraftCustomLivingEntity(server, nms)
                    else CraftCustomEntity(server, nms)
                },
                originalData.spawnFunction
            )
        }

        entityTypeData.setStaticFinal(entityTypeDataMap)
    }

    /**
     * Injects all registered custom entities into the game runtime.
     *
     * Once called, no further registrations can happen.
     * Must be called before the worlds are loaded to ensure that custom entities are not removed.
     *
     * If any exceptions occur during the injection phase, the JVM is immediately terminated to prevent further data corruption.
     * @return `true` if all entities were successfully injected, `false` if there were no entities to inject
     * @throws IllegalStateException if the injector is already frozen
     */
    fun inject(): Boolean {
        if (frozen) throw IllegalStateException("Entities already injected")
        frozen = true

        if (injections.isEmpty()) {
            logger.info("There are no entities to inject")
            return false
        }

        val toInject = injections.size
        logger.info("Injecting $toInject entit${if (toInject == 1) "y" else "ies"}...")

        try {
            doInject()
            logger.info("Injection success")
        } catch (t: Throwable) {
            logger.log(Level.SEVERE, "A fatal exception occurred during injection phase: ", t)
            logger.severe("The server will now be terminated to prevent further data corruption")

            // Flush and stop all logging
            (LogManager.getContext(false) as LoggerContext).stop()
            // Terminate JVM
            Runtime.getRuntime().halt(-1)
        }
        return true
    }

    /**
     * Copies the models from registered model URLs in entity injections to the BetterModel models folder.
     *
     * This method checks if the entities have been injected and the system is in a "frozen" state,
     * and verifies that BetterModel is loaded. It then copies each registered model URL to the models
     * directory within the BetterModel folder.
     *
     * @throws IllegalStateException if called before the injection is frozen or if BetterModel is not loaded
     * @throws IOException if an I/O exception occurs during model file copying
     */
    fun copyModels() {
        if (!frozen)
            throw IllegalStateException("Models can be copied only after all entities are injected and injection is frozen")
        if (!(BestiumImpl.pluginSupport().betterModel()))
            throw IllegalStateException("Models can be copied only when BetterModel is loaded")

        val modelsDir = File((BetterModel.inst() as JavaPlugin).dataFolder, "models/.bestium")
        modelsDir.mkdirs()

        for (inj in injections.values) {
            val modelUrl = inj.modelUrl() ?: continue
            val outputFile = File(modelsDir, inj.modelName()!! + ".bbmodel")
            val inputStream = modelUrl.openStream()
            val outputStream = FileOutputStream(outputFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        }
    }
}