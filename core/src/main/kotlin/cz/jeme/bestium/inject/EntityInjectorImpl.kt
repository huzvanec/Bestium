package cz.jeme.bestium.inject

import cz.jeme.bestium.PluginSupportImpl
import cz.jeme.bestium.api.inject.EntityInjection
import cz.jeme.bestium.api.inject.EntityInjector
import cz.jeme.bestium.util.flushLoggingAndCrashJvm
import kr.toxicity.model.api.BetterModel
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.util.function.Supplier


internal object EntityInjectorImpl : EntityInjector {
    private val logger = ComponentLogger.logger(javaClass)

    private val registrations = mutableSetOf<Supplier<EntityInjection<*, *>>>()
    private val injections = hashMapOf<Class<out Entity>, EntityInjection<*, *>>()
    private val keyedInjections = hashMapOf<Key, EntityInjection<*, *>>()

    private var phase: EntityInjector.Phase = EntityInjector.Phase.REGISTRATION

    override fun getPhase() = phase

    override fun getInjections(): Map<Class<out Entity>, EntityInjection<*, *>> = when (phase) {
        EntityInjector.Phase.REGISTRATION, EntityInjector.Phase.PRE_INJECTION -> throw IllegalStateException(
            "Injection registrations not resolved yet"
        )

        else -> @Suppress("UNCHECKED_CAST")
        injections
    }

    override fun getKeyedInjections(): Map<Key, EntityInjection<*, *>> = when (phase) {
        EntityInjector.Phase.REGISTRATION, EntityInjector.Phase.PRE_INJECTION -> throw IllegalStateException(
            "Injection registrations not resolved yet"
        )

        else -> keyedInjections
    }

    override fun getTypes(): Map<Class<out Entity>, EntityType<*>> = when (phase) {
        EntityInjector.Phase.INJECTED -> @Suppress("UNCHECKED_CAST")
        unit.types

        else -> throw IllegalStateException("Injection is not complete yet")
    }

    override fun getKeyedTypes(): Map<Key, EntityType<out Entity>> = when (phase) {
        EntityInjector.Phase.INJECTED -> unit.keyedTypes

        else -> throw IllegalStateException("Injection is not complete yet")
    }

    override fun register(injectionSupplier: Supplier<EntityInjection<*, *>>) {
        registrations.add(injectionSupplier)
    }

    private lateinit var unit: InjectionUnit

    private fun loadInjections() {
        phase = EntityInjector.Phase.PRE_INJECTION
        registrations
            .map(Supplier<EntityInjection<*, *>>::get)
            .forEach { injection ->
                val clazz = injection.entityClass
                val key = injection.key
                if (injections.containsKey(clazz))
                    throw IllegalArgumentException("Duplicate registered injection with entity class: '${clazz.name}'")
                if (keyedInjections.containsKey(key))
                    throw IllegalArgumentException("Duplicate registered injection with key: '${injection.getKey()}'")
                injections[clazz] = injection
                keyedInjections[key] = injection
            }
        unit = InjectionUnit(injections.values)
    }

    fun injectMinecraft(): Boolean {
        if (phase != EntityInjector.Phase.REGISTRATION) throw IllegalStateException("Invalid phase for phase 1 injection: $phase")

        // load injections from suppliers
        loadInjections()

        phase = EntityInjector.Phase.INJECTION_PHASE_1

        if (injections.isEmpty()) {
            logger.info("There are no entities to inject")
            return false
        }

        return tryInject(unit::injectBootstrap)
    }

    fun injectBukkit(): Boolean {
        if (phase != EntityInjector.Phase.INJECTION_PHASE_1) throw IllegalStateException("Invalid phase for phase 2 injection: $phase")
        if (injections.isEmpty()) {
            phase = EntityInjector.Phase.INJECTED
            return false
        }
        phase = EntityInjector.Phase.INJECTION_PHASE_2
        return tryInject {
            unit.injectLoad()
            phase = EntityInjector.Phase.INJECTED
        }
    }

    private fun tryInject(injectFun: () -> Unit): Boolean {
        try {
            injectFun()
            return true
        } catch (t: Throwable) {
            logger.error("A fatal exception occurred during injection phase: ", t)
            logger.error("The server will now be terminated to prevent further data corruption")

            flushLoggingAndCrashJvm()
            return false
        }
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
        if (phase != EntityInjector.Phase.INJECTED)
            throw IllegalStateException("Models can be copied only after all entities are injected")
        if (!PluginSupportImpl.isBetterModelLoaded())
            throw IllegalStateException("Models can be copied only when BetterModel is loaded")

        val modelsDir = File((BetterModel.plugin() as JavaPlugin).dataFolder, "models/.bestium")
        modelsDir.mkdirs()

        injections.values
            .flatMap { inj -> inj.variants.values }
            .forEach { variant ->
                variant.modelUrl.openStream().use { input ->
                    File(
                        modelsDir,
                        variant.modelName + ".bbmodel"
                    ).outputStream().use { output ->
                        input.copyTo(output, bufferSize = 2 shl 13)
                    }
                }
            }
    }
}