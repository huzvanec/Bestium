package cz.jeme.bestium

import cz.jeme.bestium.api.inject.EntityInjection
import cz.jeme.bestium.api.inject.EntityInjector
import cz.jeme.bestium.api.inject.Injectable
import cz.jeme.bestium.util.flushLoggingAndCrashJvm
import kr.toxicity.model.api.BetterModel
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.function.Supplier


internal object EntityInjectorImpl : EntityInjector {
    private val logger = ComponentLogger.logger(javaClass)

    private val registrations = mutableSetOf<Supplier<EntityInjection<*, *>>>()
    private val injections = hashMapOf<Class<out Entity>, EntityInjection<*, *>>()

    private var phase: EntityInjector.Phase = EntityInjector.Phase.REGISTRATION

    override fun phase() = phase

    override fun <T> injections(): Map<Class<T>, EntityInjection<T, *>> where T : Entity, T : Injectable {
        when (phase) {
            EntityInjector.Phase.REGISTRATION, EntityInjector.Phase.PRE_INJECTION -> {
                throw IllegalStateException("Injection registrations not resolved yet")
            }

            else -> {
                @Suppress("UNCHECKED_CAST")
                return injections as Map<Class<T>, EntityInjection<T, *>>
            }
        }
    }

    override fun <T> types(): Map<Class<T>, EntityType<T>> where T : Entity, T : Injectable {
        when (phase) {
            EntityInjector.Phase.INJECTED -> {
                @Suppress("UNCHECKED_CAST")
                return unit.types as Map<Class<T>, EntityType<T>>
            }

            else -> {
                throw IllegalStateException("Injection is not complete yet")
            }
        }
    }

    private val keys = mutableSetOf<Key>()

    override fun register(injectionSupplier: Supplier<EntityInjection<*, *>>) {
        registrations.add(injectionSupplier)
    }

    private lateinit var unit: InjectionUnit

    private fun loadInjections() {
        phase = EntityInjector.Phase.PRE_INJECTION
        registrations
            .map(Supplier<EntityInjection<*, *>>::get)
            .forEach { injection ->
                val clazz = injection.entityClass()
                if (injections.containsKey(clazz))
                    throw IllegalArgumentException("Duplicate registered injection with entity class: '${clazz.name}'")
                if (keys.contains(injection.key()))
                    throw IllegalArgumentException("Duplicate registered injection with key: '${injection.key()}'")
                keys.add(injection.key())
                injections[clazz] = injection
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

        return tryInject(unit::minecraftInjection)
    }

    fun injectBukkit(): Boolean {
        if (phase != EntityInjector.Phase.INJECTION_PHASE_1) throw IllegalStateException("Invalid phase for phase 2 injection: $phase")
        if (injections.isEmpty()) {
            phase = EntityInjector.Phase.INJECTED
            return false
        }
        phase = EntityInjector.Phase.INJECTION_PHASE_2
        return tryInject {
            unit.bukkitInjection()
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
        if (!PluginSupportImpl.betterModel())
            throw IllegalStateException("Models can be copied only when BetterModel is loaded")

        val modelsDir = File((BetterModel.plugin() as JavaPlugin).dataFolder, "models/.bestium")
        modelsDir.mkdirs()

        for (inj in injections.values) {
            val modelUrl = inj.modelUrl() ?: continue
            val outputFile = File(modelsDir, inj.modelName()!! + ".bbmodel")
            modelUrl.openStream().use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output, bufferSize = 2 shl 13)
                }
            }
        }
    }
}